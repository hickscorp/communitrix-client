package fr.pierreqr.communitrix;

import java.util.HashMap;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.bitfire.utils.ShaderLoader;

import fr.pierreqr.communitrix.modelTemplaters.CubeModelTemplater;
import fr.pierreqr.communitrix.modelTemplaters.ModelTemplater;
import fr.pierreqr.communitrix.screens.CombatScreen;
import fr.pierreqr.communitrix.screens.LobbyScreen;

public class Communitrix extends Game {
  // Constants.
  public  static final  Vector3   CELL_DIMENSIONS   = new Vector3(5, 5, 5);
  public  static final  float     TRANSLATION_SPEED = 20.0f;
  public  static final  float     ROTATION_SPEED    = 120.0f;
  
  // Shared members.
  public          Stage           uiStage;
  public          Skin            uiSkin;
  public          ModelBuilder    modelBuilder;
  public          ModelBatch      modelBatch;
  public          int             viewWidth, viewHeight;
  
  // Where our models will be cached.
  private         HashMap<String, ModelTemplater> modelTemplaters = new HashMap<String, ModelTemplater>();
  private         HashMap<String, Model>          models          = new HashMap<String, Model>();
  
  private         Screen          combatScreen, lobbyScreen;
  
  @Override public void create () {

    // After starting the application, we can query for the desktop dimensions
    if (Gdx.app.getType()==ApplicationType.Desktop) {
      final DisplayMode dm  = Gdx.graphics.getDesktopDisplayMode();
      Gdx.graphics.setDisplayMode (dm.width, dm.height, true);
    }

    // Configure assets etc.
    ShaderLoader.BasePath = "../android/assets/shaders/";

    // Instantiate shared members.
    uiStage                 = new Stage();
    uiSkin                  = new Skin(Gdx.files.local("../android/assets/skins/uiskin.json"));
    modelBuilder            = new ModelBuilder();
    modelBatch              = new ModelBatch();
    
    // Cache viewport size.
    resize                    (Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    // Register templaters.
    registerModelTemplater    ("Cube", new CubeModelTemplater());
    // Instantiate first game screen.
    lobbyScreenRequestingExit ();
  }
  
  // Occurs when the game exits.
  @Override public void dispose () {
    final Screen screen  = getScreen();
    if (screen!=null)
      screen.dispose();
    clearCaches();
    modelBatch.dispose();
  }
  
  // Occurs whenever the viewport needs to render.
  @Override public void render () {
    super.render();
  }
  
  @Override public void resize (final int width, final int height) {
    viewWidth       = width;
    viewHeight      = height;
    // Update flat UI.
    uiStage.getViewport().update(viewWidth, viewHeight, true);
    // Propagate change to current screen instance.
    super.resize(width, height);
  }

  
  public void combatScreenRequestingExit () {
    uiStage.clear();
    if (lobbyScreen==null)
      lobbyScreen   = new LobbyScreen(this);
    setScreen(lobbyScreen);
  }
  public void lobbyScreenRequestingExit () {
    uiStage.clear();
    if (combatScreen==null)
      combatScreen  = new CombatScreen(this);
    setScreen(combatScreen);
  }
  
  // Cache clearing methods.
  public void clearCaches () {
    // Get rid of all cached models.
    for (final String identifier : models.keySet())
      models.remove(identifier).dispose();
    // Get rid of all model templaters.
    for (final String identifier : modelTemplaters.keySet())
      modelTemplaters.remove(identifier).dispose();
  }
  
  // Registers a templater into the engine.
  public void registerModelTemplater (final String identifier, final ModelTemplater modelTemplater) {
    modelTemplaters.put(identifier, modelTemplater);
  }
  // Gets a model based on its identifier.
  public Model getModel (final String identifier) {
    // Get the requested model from our cache.
    Model     mdl     = models.get(identifier);
    // The model was not found in our cache.
    if (mdl==null) {
      Gdx.app.debug("LogicManager", "Getting templater for model " + identifier + "...");
      // Let's find if we have a templater matching the identifier.
      final ModelTemplater templater   = modelTemplaters.get(identifier);
      // Templater found, use it.
      if ( templater!=null ) {
        Gdx.app.debug("LogicManager", "Templater found, building model " + identifier + ". This should happend only once!");
        models.put(identifier, mdl = templater.build(modelBuilder));
      }
      // No templater found, log this as an error.
      else
        Gdx.app.error("LogicManager", "A call to getModel was made with an unknown templater identifier: " + identifier + ".");
    }
    return mdl;
  }
}
