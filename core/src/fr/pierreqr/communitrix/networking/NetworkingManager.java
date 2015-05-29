package fr.pierreqr.communitrix.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.NetJavaSocketImpl;
import com.badlogic.gdx.net.SocketHints;

import fr.pierreqr.communitrix.commands.in.ICBase;
import fr.pierreqr.communitrix.commands.out.OCBase;
import fr.pierreqr.communitrix.commands.out.OCJoinCombat;

public class NetworkingManager implements Runnable {
  public interface Delegate {
    void  onServerMessage   (final ICBase command);
  }
  
  // Network related members.
  public final    String            host;
  public final    int               port;
  public          NetJavaSocketImpl socket;
  public          InputStream       netInput;
  public          OutputStream      netOutput;
  public          Delegate          delegate;
  private boolean shouldRun         = true;
  
  public NetworkingManager (final String h, final int p, final Delegate d) {
    host        = h;
    port        = p;
    delegate    = d;
  }
  public void stop () {
    synchronized (delegate) {
      shouldRun   = false;
      Thread.currentThread().interrupt();
    }
  }
  
  @Override
  public void run() {
    SocketHints   hints   = new SocketHints();
    hints.keepAlive       = true;
    socket                = new NetJavaSocketImpl(Protocol.TCP, host, port, hints);
    netInput              = socket.getInputStream();
    netOutput             = socket.getOutputStream();
    
    try {
      Gdx.app.log             ("NetworkingManager", "Sending...");
      netOutput.write         (new OCJoinCombat("CBT1").toJson());
    } catch (IOException e) {
      e.printStackTrace();
    }

    String          data       = "";
    int             enclosed   = 0;
    char            b;
    while (true) {
      synchronized (delegate) {
        if (!shouldRun) break;
      }
      try {
        data                      += ( b = (char)netInput.read() );
      } catch (IOException e) {
        e.printStackTrace();
        continue;
      }
      if      (b=='{' || b=='[')  ++enclosed;
      else if (b=='}' || b==']')  --enclosed;
      if (enclosed==0) {
        final ICBase  cmd   = ICBase.fromJson(data);
        data                      = "";
        Gdx.app.postRunnable(new Runnable() { @Override public void run() { delegate.onServerMessage(cmd); } });
      }
    }
    Gdx.app.error("Communitrix", "Disposing network!");
    socket.dispose();
  }
  
  public void send (final OCBase command) {
    synchronized (netOutput) {
      try {
        Gdx.app.log             ("NetworkingManager", "Sending...");
        netOutput.write         (command.toJson());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
