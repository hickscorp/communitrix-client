package fr.pierreqr.communitrix.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import fr.pierreqr.communitrix.Communitrix;

public class DesktopLauncher {
  public static void main (String[] arg) {
    // Configure everything.
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    // Run the game.
    new LwjglApplication(new Communitrix(), config);
  }
}
