package fr.pierreqr.communitrix;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import fr.pierreqr.communitrix.gameScreens.CombatGameScreen;
import fr.pierreqr.communitrix.gameScreens.GameScreen;
import fr.pierreqr.communitrix.modelTemplaters.CubeModelTemplater;

public class Communitrix extends ApplicationAdapter {
  // Main logic manager instance is cached here.
  private       LogicManager          logicManager;
  private       GameScreen            currentScreen;

  @Override public void create () {
    // After starting the application, we can query for the desktop dimensions
    if (Gdx.app.getType()==ApplicationType.Desktop) {
      final DisplayMode dm    = Gdx.graphics.getDesktopDisplayMode();
      Gdx.graphics.setDisplayMode   (dm.width, dm.height, true);
    }
    // Prepare global logic manager.
    logicManager                        = LogicManager.getInstance();
    logicManager.registerModelTemplater ("Cube", new CubeModelTemplater());
    // Instanciate first game screen.
    currentScreen                       = new CombatGameScreen();
    currentScreen.create                (logicManager);
  }

  @Override public void dispose () {
    currentScreen.dispose();
    logicManager.dispose();
  }

  // Occurs whenever the viewport size changes.
  @Override public void resize (int width, int height) {
    currentScreen.resize    (width, height);
  }
  // Occurs whenever the viewport needs to render.
  @Override public void render () {
    currentScreen.render();
  };
  
  // Occurs whenever the application is paused (Eg enters background, etc).
  @Override public void pause () {
    currentScreen.pause ();
  }
  // Transitions between pause and normal mode.
  @Override public void resume () {
    currentScreen.resume  ();
  }
}
