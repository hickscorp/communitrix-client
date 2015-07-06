package fr.pierreqr.communitrix.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
//import com.badlogic.gdx.tools.texturepacker.TexturePacker;
//import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import fr.pierreqr.communitrix.Communitrix;

public class DesktopLauncher {
  public static void main (String[] arg) {
    // In development mode, we need to pack our textures.
    Settings settings = new TexturePacker.Settings();
    settings.maxWidth     = 512;
    settings.maxHeight    = 512;
    TexturePacker.process(settings, "../artwork/exports", "../android/assets/atlases", "game");
    
    // Configure everything.
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    // Run the game.
    new LwjglApplication(new Communitrix(), config);
  }
}
