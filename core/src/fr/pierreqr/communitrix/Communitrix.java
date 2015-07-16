package fr.pierreqr.communitrix;

import java.util.EnumMap;
import java.util.Random;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.UBJsonReader;
import com.bitfire.utils.ShaderLoader;
import fr.pierreqr.communitrix.Constants.CubeFace;
import fr.pierreqr.communitrix.Constants.SkinSize;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.networking.NetworkDelegate;
import fr.pierreqr.communitrix.networking.NetworkingManager;
import fr.pierreqr.communitrix.networking.cmd.rx.*;
import fr.pierreqr.communitrix.screens.BaseScreen;
import fr.pierreqr.communitrix.screens.MainScreen;
import fr.pierreqr.communitrix.screens.MainScreen.State;
import fr.pierreqr.communitrix.tweeners.CameraAccessor;
import fr.pierreqr.communitrix.tweeners.GameObjectAccessor;

public class Communitrix extends Game implements ErrorResponder, NetworkDelegate {
  public                enum        State                 { Unknown, Startup, Game };
  // Common materials.
  public final static   Material[]  faceMaterials         = new Material[12];
  // Various constants.
  private static final  String      LogTag                = "Communitrix";
  private static final  String      host                  = "localhost";

  // Shared members.
  public          ApplicationType   applicationType;
  public          int               viewWidth, viewHeight;
  public          EnumMap<SkinSize, Skin>
                                    skins                 = new EnumMap<SkinSize, Skin>(SkinSize.class);
  public          TweenManager      tweener               = new TweenManager();
  public          ModelBuilder      modelBuilder;
  public          ModelBatch        modelBatch;
  public          G3dModelLoader    modelLoader;
  public          Material          defaultMaterial;
  public          Model             dummyModel;
  // Random generator.
  public final    Random            rand                  = new Random();
  // Network-related objects.
  public          NetworkingManager networkingManager;
  public          boolean           connected             = false;
  public          Timer             networkTimer;
  // The current error responder if any.
  private         ErrorResponder    errorResponder;
  private         NetworkDelegate   networkDelegate;
  
  // The singleton instance.
  private static  Communitrix       instance;
  
  // All our different screens.
  private         MainScreen        mainScreen;

  public static Communitrix getInstance() {
    return instance;
  }
  
  public Communitrix () {
    // Store singleton instance.
    setErrorResponder       (instance = this);
    // Configure assets etc.
    ShaderLoader.BasePath   = "shaders/";
    // Register motion tweening accessors.
    Tween.setCombinedAttributesLimit  (6);
    Tween.registerAccessor            (GameObject.class,        new GameObjectAccessor());
    Tween.registerAccessor            (PerspectiveCamera.class, new CameraAccessor());
  }
  // Getters / Setters.
  public void setErrorResponder (final ErrorResponder newErrorResponder) {
    errorResponder          = newErrorResponder;
  }
  public void setNetworkDelegate (final NetworkDelegate newNetworkDelegate) {
    networkDelegate         = newNetworkDelegate;
  }
  
  @Override public void create () {
    // Cache application type.
    applicationType     = Gdx.app.getType();

    // After starting the application, we can query for the desktop dimensions
    boolean fullScreen = false;
    if (fullScreen && applicationType==ApplicationType.Desktop)
      Gdx.graphics.setDisplayMode (Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);
    
    // Prepare face materials.
    if (faceMaterials[0]==null) {
      final TextureAtlas  atlas       = new TextureAtlas(Gdx.files.internal("atlases/Main.atlas"));
      final Attribute     blend       = new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.95f);
      for (final CubeFace face : Constants.CubeFace.values())
        faceMaterials[face.ordinal()]   = new Material(TextureAttribute.createDiffuse(atlas.findRegion(face.toString())), blend);
    }

    // Force cache viewport size.
    resize                  (Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    
    // Instantiate shared members.
    skins.put               (SkinSize.Mini,     new Skin(Gdx.files.internal("skins/uiskin-mini.json")));
    skins.put               (SkinSize.Medium,   new Skin(Gdx.files.internal("skins/uiskin-medium.json")));
    skins.put               (SkinSize.Large,    new Skin(Gdx.files.internal("skins/uiskin-large.json")));
    
    modelBuilder            = new ModelBuilder();
    modelBatch              = new ModelBatch();
    defaultMaterial         = new Material(ColorAttribute.createDiffuse(Color.WHITE));
    dummyModel              = new Model();
    
    // Instantiate networking manager.
    networkTimer            = new Timer();
    networkingManager       = new NetworkingManager(host, 9003, this);
    networkingManager.start ();
    
    // Set up our default error responder.
    errorResponder          = this;
    // Prepare our shared model loader.
    modelLoader             = new G3dModelLoader(new UBJsonReader());
    // Set default screen.
    setScreen               (getLazyMainScreen());
  }
  
  public void setScreen (final BaseScreen screen) {
    super.setScreen               (screen);
    setErrorResponder       (screen);
    setNetworkDelegate      (screen);
  }
  @Override public void render () {
    // Clear viewport etc.
    Gdx.gl.glClear        (GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    // Enable alpha blending.
    Gdx.gl.glEnable       (GL20.GL_BLEND);
    Gdx.gl.glBlendFunc    (GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    // Enable back-face culling.
    Gdx.gl.glEnable       (GL20.GL_CULL_FACE);
    Gdx.gl.glCullFace     (GL20.GL_FRONT);
    
    super.render          ();
  }
  
  // ErrorResponder implementation.
  public void setLastError (final int code, final String reason) {
    if (errorResponder==this)
      Gdx.app.log                 (LogTag, "An error has occured: #" + code + " - " + reason);
    else
      errorResponder.setLastError (code, reason);
  };
  
  // Occurs when the game exits.
  @Override public void dispose () {
    if (networkingManager!=null) {
      networkTimer.stop       ();
      networkingManager.stop  ();
    }
    if (dummyModel!=null)     dummyModel.dispose();
    if (mainScreen!=null)    mainScreen.dispose();
    modelBatch.dispose      ();
  }

  @Override public void resize (final int w, final int h) { super.resize(viewWidth = w, viewHeight = h); }
  
  public void setState (final State newState) {
  }
  
  // This method runs after rendering.
  @Override public boolean onServerMessage (final RXBase baseCmd) {
    if (baseCmd==null) {
      Gdx.app.error         (LogTag, "Received a NULL command!");
      return true;
    }
    else if (networkDelegate!=null && networkDelegate.onServerMessage(baseCmd))
      return true;

    switch (baseCmd.type) {
      case Connected:
        break;
      case Disconnected:
        networkTimer.scheduleTask(new Timer.Task() { @Override public void run() { networkingManager.start(); } }, 1.0f);
        break;
      case Error: {
        final RXError cmd = (RXError)baseCmd;
        setLastError(cmd.code, cmd.reason);
        break;
      }
      case Acknowledgment: {
        final RXAcknowledgment  cmd   = (RXAcknowledgment)baseCmd;
        getLazyMainScreen()
          .handleAcknowledgment(cmd.serial, cmd.valid, cmd.errorMessage);
        break;
      }
      case Welcome: {
        getLazyMainScreen()
          .setState         (MainScreen.State.Global);
        setScreen           (mainScreen);
        break;
      }
      case Registered: {
        break;
      }
      case CombatList: {
        final RXCombatList cmd = (RXCombatList)baseCmd;
        getLazyMainScreen()
          .setCombats  (cmd.combats);
        break;
      }
      case CombatJoin: {
        final RXCombatJoin cmd = (RXCombatJoin)baseCmd;
        getLazyMainScreen()
          .setCombat        (cmd.combat)
          .setState         (MainScreen.State.Joined);
        break;
      }
      case CombatPlayerJoined: {
        getLazyMainScreen()
          .addPlayer          (((RXCombatPlayerJoined)baseCmd).player);
        break;
      }
      case CombatPlayerLeft: {
        final RXCombatPlayerLeft cmd = (RXCombatPlayerLeft)baseCmd;
        getLazyMainScreen()
          .removePlayer       (cmd.uuid);
        break;
      }
      case CombatStart: {
        final RXCombatStart cmd   = (RXCombatStart)baseCmd;
        getLazyMainScreen()
          .prepare        (cmd.target, cmd.units, cmd.pieces)
          .setState       (MainScreen.State.Gaming);
        break;
      }
      case CombatNewTurn: {
        RXCombatNewTurn     cmd = (RXCombatNewTurn)baseCmd;
        getLazyMainScreen()
          .setTurn        (cmd.turnId, cmd.unitId)
          .setState       (MainScreen.State.Gaming);
        break;
      }
      case CombatPlayerTurn: {
        RXCombatPlayerTurn  cmd = (RXCombatPlayerTurn)baseCmd;
        getLazyMainScreen()
          .registerPlayerTurn   (cmd.playerUUID, cmd.unitId, cmd.unit);
         break;
      }
      case CombatEnd: {
        getLazyMainScreen()
          .endGame        ()
          .setState       (MainScreen.State.EndGame);
        break;
      }
      default:
        setLastError      (-1, String.format("Unhandled command of type %s.", baseCmd.type.toString()));
    }
    return true;
  }

  private MainScreen getLazyMainScreen    () {
    return mainScreen==null ? mainScreen = new MainScreen() : mainScreen;
  }

  public static float round (final float v, final float precision) {
    return    Math.round(v * precision) / precision;
  }
  public static Vector3 round (final Vector3 v, final float precision) {
    v.x     = round(v.x, precision);
    v.y     = round(v.y, precision);
    v.z     = round(v.z, precision);
    return  v;
  }
  public static Quaternion round (final Quaternion q, final float precision) {
    q.x     = round(q.x, precision);
    q.y     = round(q.y, precision);
    q.z     = round(q.z, precision);
    q.w     = round(q.w, precision);
    return  q.nor();
  }
  public static BoundingBox round (final BoundingBox b, final float precision) {
    round   (b.min, precision);
    round   (b.max, precision);
    return  b;
  }
}
