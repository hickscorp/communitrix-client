package fr.pierreqr.communitrix.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.NetJavaSocketImpl;
import com.badlogic.gdx.net.SocketHints;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.pierreqr.communitrix.networking.commands.in.ICBase;
import fr.pierreqr.communitrix.networking.commands.out.OCBase;

public class NetworkingManager implements Runnable {
  // Inbound commands.
  public final static   String        ICError             = "Error";
  public final static   String        ICWelcome           = "Welcome";
  public final static   String        ICCombatList        = "CombatList";
  public final static   String        ICJoinCombat        = "JoinCombat";
  public final static   String        ICStartCombat       = "StartCombat";
  public final static   String        ICStartCombatTurn   = "StartCombatTurn";
  public final static   String        ICPlayCombatTurn    = "PlayCombatTurn";
  public final static   String        ICCombatEnd         = "CombatEnd";
  // Outbound commands.
  public final static   String        OCError             = "Error";
  public final static   String        OCCombatList        = "CombatList";
  public final static   String        OCJoinCombat        = "JoinCombat";
  public final static   String        OCPlayCombatTurn    = "PlayCombatTurn";
  
  public interface NetworkDelegate { void  onServerMessage (final ICBase cmd); }
  
  // Network related members.
  private final     String            host;
  private final     int               port;
  private           Thread            thread          = null;
  private           NetJavaSocketImpl socket          = null;
  private           InputStream       netInput        = null;
  private           OutputStream      netOutput       = null;
  private           ObjectMapper      mapper          = new ObjectMapper();
  private           StringBuilder     sb              = new StringBuilder(2048);
  private           String            type            = null;
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
    SocketHints hints = new SocketHints();
    hints.keepAlive   = true;
    socket            = new NetJavaSocketImpl(Protocol.TCP, host, port, null);
    netInput          = socket.getInputStream();
    netOutput         = socket.getOutputStream();
    char        buff  = 0;
    while (shouldRun) {
      try {
        buff          = (char)netInput.read();
      } catch (IOException e) {
        e.printStackTrace ();
        break;
      }
      switch (buff) {
        case '\r': {
          type          = sb.toString().substring(8);
          sb.setLength  (0);
          break;
        }
        case '\n': {
          // We just got the type for the next payload.
          Gdx.app.log       ("Net", "Type: " + type + ", Packet: " + sb.toString());
          if (type!=null && sb.length()>0) {
            final ICBase  cmd = readValue();
            Gdx.app.postRunnable( new Runnable() { @Override public void run() {
              delegate.onServerMessage(cmd);
            }});
          }
          sb.setLength      (0);
          type              = null;
          break;
        }
        default:
          sb.append     (buff);
      }
    }
    // Clean all resources.
    dispose         ();
    if (shouldRun)  start();
  }
  private ICBase readValue () {
    ICBase    cmd     = null;
    try {
      switch (type) {
        case ICError:
          cmd           = mapper.readValue(sb.toString(), new TypeReference<fr.pierreqr.communitrix.networking.commands.in.ICError>(){});
          break;
        case ICWelcome:
          cmd           = mapper.readValue(sb.toString(), new TypeReference<fr.pierreqr.communitrix.networking.commands.in.ICWelcome>(){});
          break;
        case ICCombatList:
          break;
        case ICJoinCombat:
          cmd           = mapper.readValue(sb.toString(), new TypeReference<fr.pierreqr.communitrix.networking.commands.in.ICJoinCombat>(){});
          break;
        case ICStartCombat:
          break;
        case ICStartCombatTurn:
          break;
        case ICPlayCombatTurn:
          break;
        case ICCombatEnd:
          break;
        default:
          return null;
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
    cmd.type  = type;
    return cmd;
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
  public void send (final OCBase command) {
    synchronized (delegate) {
      // Don't send anything if we scheduled the thread for stopping.
      if (thread==null || netOutput==null || !shouldRun)
        return;
      // Send data to the server.
      try {
        mapper.writeValue (netOutput, command);
        netOutput.write('\n');
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
