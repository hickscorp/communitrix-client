package fr.pierreqr.communitrix;

import java.util.HashMap;

import aurelienribon.tweenengine.Tween;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
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
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.UBJsonReader;
import com.bitfire.utils.ShaderLoader;

import fr.pierreqr.communitrix.gameObjects.CameraAccessor;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.GameObjectAccessor;
import fr.pierreqr.communitrix.modelTemplaters.CubeModelTemplater;
import fr.pierreqr.communitrix.modelTemplaters.ModelTemplater;
import fr.pierreqr.communitrix.networking.NetworkingManager;
import fr.pierreqr.communitrix.screens.CombatScreen;
import fr.pierreqr.communitrix.screens.LobbyScreen;
import fr.pierreqr.communitrix.screens.LobbyScreen.State;
import fr.pierreqr.communitrix.networking.commands.rx.*;
import fr.pierreqr.communitrix.networking.commands.tx.TXCombatList;
import fr.pierreqr.communitrix.networking.commands.tx.TXRegister;

public class Communitrix extends Game implements NetworkingManager.NetworkDelegate {  
  // Constants.
  public  static final  float     TranslationSpeed      = 20.0f;
  public  static final  float     RotationSpeed         = 120.0f;
  public  static final  Vector3   CellDimensions        = new Vector3(5, 5, 5);
  public  static final  float     CellComponentRadius   = 0.5f;
  private static final  String    LogTag                = "Communitrix";

  // Shared members.
  public          ApplicationType   applicationType;
  public          int               viewWidth, viewHeight;
  public          Skin              uiSkin            = null;
  public          ModelBuilder      modelBuilder      = null;
  public          ModelBatch        modelBatch        = null;
  public          Material          defaultMaterial   = null;
  public          G3dModelLoader    modelLoader       = null;

  public          NetworkingManager networkingManager = null;
  public          Timer             networkTimer      = null;
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
    networkTimer            = new Timer();
    networkingManager.start ();
    // Prepare our shared model loader.
    UBJsonReader reader     = new UBJsonReader();
    modelLoader             = new G3dModelLoader(reader);
    
    // Set default screen.
    setScreen               (getLobbyScreen());
    
    // Prepare our FPS logging object.
    fpsLogger               = new FPSLogger();
    
    // Register motion tweening accessors.
    Tween.registerAccessor(GameObject.class, new GameObjectAccessor());
    Tween.registerAccessor(Camera.class, new CameraAccessor());
  }
  @Override public void setScreen(Screen screen) {
    if (getScreen()!=screen)  super.setScreen(screen);
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
    //fpsLogger.log ();
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
  
  @Override public void onServerConnected () {
    Gdx.app.log             (LogTag, "We are connected to the server.");
    networkingManager.send  (new TXRegister("Doodloo"));
  }
  @Override public void onServerDisconnected () {
    Gdx.app.log             (LogTag, "We are disconnected from the server. Reconnection scheduled in 3 seconds.");
    networkTimer.scheduleTask(new Timer.Task() { @Override public void run() { networkingManager.start(); } }, 3.0f);
  }
  // This method runs after rendering.
  @Override public void onServerMessage (final RXBase cmd) {
    if (cmd==null) {
      Gdx.app.error         (LogTag, "Received a NULL command!");
      return;
    }
    switch (cmd.type) {
      case Error: {
        final RXError spec      = (RXError)cmd;
        Gdx.app.error           (LogTag, "Server just notified us of an error #" + spec.code + ": " + spec.reason);
        break;
      }
      case Welcome: {
        final RXWelcome spec    = (RXWelcome)cmd;
        Gdx.app.log             (LogTag, "Server is welcoming us: " + spec.message);
        getLobbyScreen()
          .setState             (State.Global);
        setScreen               (lobbyScreen);
        break;
      }
      case Registered: {
        networkingManager.send  (new TXCombatList());
        break;
      }
      case CombatList: {
        final RXCombatList spec = (RXCombatList)cmd;
        Gdx.app.log             (LogTag, "Combat list: " + spec.combats.toString());
        networkingManager.send  (new fr.pierreqr.communitrix.networking.commands.tx.TXCombatJoin("CBT1"));
        break;
      }
      case CombatJoin: {
        final RXCombatJoin spec = (RXCombatJoin)cmd;
        Gdx.app.log             (LogTag, "We joined a combat, it has " + spec.combat.players.size() + " people.");
        setScreen(
          getLobbyScreen()
            .setState             (State.Global)
            .setPlayers           (spec.combat.players)
        );
        break;
      }
      case CombatPlayerJoined: {
        final RXCombatPlayerJoined spec = (RXCombatPlayerJoined)cmd;
        Gdx.app.log             (LogTag, "Player " + spec.player + " has joined.");
        getLobbyScreen()
          .addPlayer            (spec.player);
        break;
      }
      case CombatPlayerLeft: {
        final RXCombatPlayerLeft spec = (RXCombatPlayerLeft)cmd;
        Gdx.app.log             (LogTag, "Player " + spec.uuid + " has left.");
        getLobbyScreen()
          .removePlayer         (spec.uuid);
        break;
      }
      case CombatStart: {
        final RXCombatStart spec = (RXCombatStart)cmd;
        Gdx.app.log             (LogTag, "Server is ordering us to start combat.");
        setScreen(
          getCombatScreen()
            .setUp              (combatScreen.new Configuration(spec))
        );
        break;
      }
      case CombatNewTurn: {
        break;
      }
      case CombatPlayerTurn: {
        break;
      }
      case CombatEnd: {
        break;
      }
      default:
        Gdx.app.log         (LogTag, "Unhandled command type: " + cmd.type + ".");
    }
  }
  
  private LobbyScreen getLobbyScreen () {
    return lobbyScreen==null ? lobbyScreen = new LobbyScreen(this) : lobbyScreen;
  }
  private CombatScreen getCombatScreen () {
    return combatScreen==null ? combatScreen = new CombatScreen(this) : combatScreen;
  }
}
