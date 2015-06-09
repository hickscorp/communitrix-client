package fr.pierreqr.communitrix.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.NetJavaSocketImpl;
import com.badlogic.gdx.net.SocketHints;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.pierreqr.communitrix.networking.commands.rx.RXBase;

public class NetworkingManager implements Runnable {
  // Constants.
  private final static  String        LogTag                = "Networking";
  
  public interface NetworkDelegate {
    void  onServerConnected     ();
    void  onServerMessage       (final RXBase cmd);
    void  onServerDisconnected  ();
  }
  
  // Network related members.
  private final     String            host;
  private final     int               port;
  private           Thread            thread          = null;
  private           NetJavaSocketImpl socket          = null;
  private           InputStream       netInput        = null;
  private           OutputStream      netOutput       = null;
  private           ObjectMapper      mapper          = new ObjectMapper();
  private           StringBuilder     sb              = new StringBuilder(2048);
  private final     NetworkDelegate   delegate;
  private volatile  boolean           shouldRun       = true;
  
  public NetworkingManager (final String h, final int p, final NetworkDelegate d) {
    // Initialize our members.
    host              = h;
    port              = p;
    delegate          = d;
    JsonFactory f     = mapper.getFactory();
    f.configure       (com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
    f.configure       (com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
  }
  
  @Override public void run() {
    boolean   ok        = false;
    try {
      SocketHints hints = new SocketHints();
      hints.keepAlive   = true;
      socket            = new NetJavaSocketImpl(Protocol.TCP, host, port, null);
      ok                = socket.isConnected();
    }
    catch (Exception e) {
      Gdx.app.error     (LogTag, "Failed to connect to server: " + e.getMessage());
    }
    // Socket is connected, run our loop.
    if (ok) {
      String      type  = null;
      netInput          = socket.getInputStream();
      netOutput         = socket.getOutputStream();
      // Signal our delegate.
      Gdx.app.postRunnable( new Runnable() { @Override public void run() { delegate.onServerConnected(); }});
      // Read forever.
      int         buff  = 0;
      while (shouldRun && socket.isConnected()) {
        try {
          if (( buff = netInput.read() )<0) {
            Gdx.app.log (LogTag, "Disconnected from server.");
            break;
          }
        } catch (IOException e) {
          e.printStackTrace ();
          break;
        }
        switch (buff) {
          case '\r': {
            type          = sb.toString();
            sb.setLength  (0);
            break;
          }
          case '\n': {
            // We just got the type for the next payload.
            if (type!=null && sb.length()>0) {
              try {
                Gdx.app.log(LogTag, type + " -> " + sb.toString());
                final RXBase      cmd     = mapper.readValue(sb.toString(), RXBase.Rx.valueOf(type).toTypeReference());
                Gdx.app.postRunnable( new Runnable() { @Override public void run() { delegate.onServerMessage(cmd); }});
              }
              catch (Exception ex) {
                ex.printStackTrace();
                break;
              }
            }
            sb.setLength      (0);
            type              = null;
            break;
          }
          default:
            sb.append     ((char)buff);
        }
      }
    }
    // Signal our delegate.
    Gdx.app.postRunnable( new Runnable() { @Override public void run() { delegate.onServerDisconnected();; }});
    // Clean all resources.
    dispose         ();
    if (shouldRun)  start();
  }
  
  // If the networking thread isn't running, start it.
  public void start () {
    synchronized (delegate) {
      shouldRun = true;
      if (thread==null) {
        ( thread = new Thread(this) ).start();
      }
    }
  }
  // Stop the networking thread if it is running.
  public void stop () {
    synchronized (delegate) {
      shouldRun = false;
      if (thread!=null) {
        thread.interrupt  ();
        thread    = null;
      }
    }
  }
  // Send data to the server. TODO: This should be asynchroneous.
  public void send (final fr.pierreqr.communitrix.networking.commands.tx.TXBase command) {
    synchronized (delegate) {
      // Don't send anything if we scheduled the thread for stopping.
      if (thread==null || netOutput==null || !shouldRun)
        return;
      // Send data to the server.
      try {
        mapper.writeValue   (netOutput, command);
        netOutput.write     ('\n');
      } catch (IOException e) {
        e.printStackTrace   ();
      }
    }
  }
  
  public void dispose () {
    stop            ();
    synchronized (delegate) {
      if (socket!=null) {
        socket.dispose  ();
        socket          = null;
        try { netInput.close  (); }
        catch (IOException e) {}
        try { netOutput.close  (); }
        catch (IOException e) {}
        netInput        = null;
        netOutput       = null;
        thread          = null;
      }
      sb.setLength      (0);
    }
  }
}
