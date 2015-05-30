package fr.pierreqr.communitrix.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.NetJavaSocketImpl;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import fr.pierreqr.communitrix.networking.commands.in.ICBase;
import fr.pierreqr.communitrix.networking.commands.out.OCBase;

public class NetworkingManager implements Runnable {
  public interface NetworkDelegate {
    void  onServerMessage   (final ICBase command);
  }
  
  // Network related members.
  private final     String            host;
  private final     int               port;
  private           Thread            thread        = null;
  private           NetJavaSocketImpl socket        = null;
  private           InputStream       netInput      = null;
  private           OutputStream      netOutput     = null;
  private final     NetworkDelegate   delegate;
  private volatile  boolean           shouldRun     = true;
  
  public NetworkingManager (final String h, final int p, final NetworkDelegate d) {
    // Set up decoder.
    ICBase.decoder.setOutputType   (OutputType.json);
    ICBase.decoder.addClassTag     ("ICPosition", fr.pierreqr.communitrix.networking.commands.in.ICPosition.class);
    // Set up encoder.
    OCBase.encoder.setOutputType   (OutputType.json);

    // Initialize our members.
    host        = h;
    port        = p;
    delegate    = d;
  }
  
  @Override
  public void run() {
    SocketHints hints       = new SocketHints();
    hints.keepAlive         = true;
    socket                  = new NetJavaSocketImpl(Protocol.TCP, host, port, hints);
    netInput                = socket.getInputStream();
    netOutput               = socket.getOutputStream();
    
    StringBuilder sb        = new StringBuilder(4096);
    int           enclosed  = 0;
    char        b;
    while (shouldRun) {
      try {
        sb.append( b = (char)netInput.read() );
      } catch (IOException e) {
        e.printStackTrace ();
        continue;
      }
      if      (b=='{' || b=='[')  ++enclosed;
      else if (b=='}' || b==']')  --enclosed;
      if (enclosed==0) {
        try {
          final ICBase  cmd     = ICBase.fromJson(sb.toString());
          Gdx.app.postRunnable  (new Runnable() { @Override public void run() { delegate.onServerMessage(cmd); } });
        }
        finally { sb.setLength(0); }
      }
    }
    // Clean all resources.
    dispose         ();
    if (shouldRun)  start();
  }
  public void start () {
    synchronized (delegate) {
      shouldRun = true;
      if (thread==null) {
        ( thread = new Thread(this) ).start();
      }
    }
  }
  public void stop () {
    synchronized (delegate) {
      shouldRun = false;
      if (thread!=null) {
        thread.interrupt  ();
        thread    = null;
      }
    }
  }
  
  public void send (final OCBase command) {
    synchronized (delegate) {
      // Don't send anything if we scheduled the thread for stopping.
      if (thread==null || netOutput==null || !shouldRun)
        return;
      // Send data to the server.
      try {
        netOutput.write   (command.toJson());
        netOutput.flush   ();
      } catch (IOException e) {
        e.printStackTrace ();
      }
    }
  }
  
  public void dispose () {
    stop            ();
    synchronized (delegate) {
      if (socket!=null) {
        socket.dispose  ();
        socket          = null;
        netInput        = null;
        netOutput       = null;
        thread          = null;
      }
    }
  }
}
