package fr.pierreqr.communitrix;

import java.util.HashMap;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.UBJsonReader;
import com.bitfire.utils.ShaderLoader;

import fr.pierreqr.communitrix.modelTemplaters.CubeModelTemplater;
import fr.pierreqr.communitrix.modelTemplaters.ModelTemplater;
import fr.pierreqr.communitrix.networking.NetworkingManager;
import fr.pierreqr.communitrix.networking.commands.in.ICBase;
import fr.pierreqr.communitrix.networking.commands.in.ICError;
import fr.pierreqr.communitrix.networking.commands.in.ICJoinCombat;
import fr.pierreqr.communitrix.networking.commands.in.ICWelcome;
import fr.pierreqr.communitrix.networking.commands.out.OCJoinCombat;
import fr.pierreqr.communitrix.screens.CombatScreen;
import fr.pierreqr.communitrix.screens.LobbyScreen;

public class Communitrix extends Game implements NetworkingManager.NetworkDelegate {  
  // Constants.
  public  static final  Vector3   CELL_DIMENSIONS       = new Vector3(5, 5, 5);
  public  static final  float     TRANSLATION_SPEED     = 20.0f;
  public  static final  float     ROTATION_SPEED        = 120.0f;
  public  static final  float     CELL_COMPONENT_RADIUS = 0.5f;

  // Shared members.
  public          ApplicationType   applicationType;
  public          int               viewWidth, viewHeight;
  public          Skin              uiSkin            = null;
  public          ModelBuilder      modelBuilder      = null;
  public          ModelBatch        modelBatch        = null;
  public          Material          defaultMaterial   = null;
  public          G3dModelLoader    modelLoader       = null;

  public          NetworkingManager networkingManager = null;
  public          FPSLogger         fpsLogger         = null;
  
  // Where our models will be cached.
  private         HashMap<String, ModelTemplater> modelTemplaters = new HashMap<String, ModelTemplater>();
  private         HashMap<String, Model>          models          = new HashMap<String, Model>();
  private static  Communitrix                     instance;

  // All our different screens.
  private         LobbyScreen     lobbyScreen     = null;
  private         CombatScreen    combatScreen    = null;
  
  public static Communitrix getInstance() {
    return instance;
  }
  
  @Override public void create () {
    instance                = this;
    // Cache application type.
    applicationType         = Gdx.app.getType();
//    // After starting the application, we can query for the desktop dimensions
//    if (applicationType==ApplicationType.Desktop) {
//      final DisplayMode     dm    = Gdx.graphics.getDesktopDisplayMode();
//      Gdx.graphics.setDisplayMode (dm.width, dm.height, true);
//    }
    
    // Configure assets etc.
    ShaderLoader.BasePath     = "shaders/";

    // Register templaters.
    registerModelTemplater    ("Cube", new CubeModelTemplater());

    // Force cache viewport size.
    resize                    (Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    // Instantiate shared members.
    uiSkin                  = new Skin(Gdx.files.internal("skins/uiskin.json"));
    modelBuilder            = new ModelBuilder();
    modelBatch              = new ModelBatch();
    defaultMaterial         = new Material(ColorAttribute.createDiffuse(Color.WHITE));
    networkingManager       = new NetworkingManager("localhost", 8080, this);
    networkingManager.start ();
    // Prepare our shared model loader.
    UBJsonReader reader     = new UBJsonReader();
    modelLoader             = new G3dModelLoader(reader);
    
    // Prepare our FPS logging object.
    fpsLogger               = new FPSLogger();
  }

  // Occurs when the game exits.
  @Override public void dispose () {
    if (networkingManager!=null)  networkingManager.stop();
    if (combatScreen!=null)       combatScreen.dispose();
    if (lobbyScreen!=null)        lobbyScreen.dispose();
    clearCaches();
    modelBatch.dispose();
  }

  // Occurs whenever the viewport needs to render.
  @Override public void render () {
    super.render  ();
    fpsLogger.log ();
  }

  @Override public void resize (final int width, final int height) {
    viewWidth       = width;
    viewHeight      = height;
    // Propagate change to current screen instance.
    super.resize(width, height);
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
  
  @Override public void onServerMessage (final ICBase cmd) {
    if (cmd==null) {
      Gdx.app.error         ("Communitrix", "Received a NULL command!");
      return;
    }
    switch (cmd.type) {
      case NetworkingManager.ICError: {
        final ICError spec = (ICError)cmd;
        Gdx.app.error           ("Communitrix", "Server just notified us of an error #" + spec.code + ": " + spec.reason);
        break;
      }
      case NetworkingManager.ICWelcome: {
        final ICWelcome spec = (ICWelcome)cmd;
        Gdx.app.log             ("Communitrix", "Server is welcoming us: " + spec.message);
        networkingManager.send  (new OCJoinCombat("CBT1"));
        break;
      }
      case NetworkingManager.ICCombatList: {
        break;
      }
      case NetworkingManager.ICJoinCombat: {
        final ICJoinCombat spec = (ICJoinCombat)cmd;
        if (combatScreen==null)
          combatScreen      = new CombatScreen(this);
        Gdx.app.log         ("Communitrix", "Server is ordering us to start combat.");
        combatScreen.reconfigure(combatScreen.new Configuration(spec));
        setScreen           (combatScreen);
        break;
      }
      case NetworkingManager.ICStartCombat: {
        break;
      }
      case NetworkingManager.ICStartCombatTurn: {
        break;
      }
      case NetworkingManager.ICPlayCombatTurn: {
        break;
      }
      case NetworkingManager.ICCombatEnd: {
        break;
      }
      default:
        Gdx.app.log         ("Communitrix", "Unhandled command type: " + cmd.type + ".");
    }
  }
}
