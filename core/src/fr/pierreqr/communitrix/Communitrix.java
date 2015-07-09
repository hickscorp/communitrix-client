package fr.pierreqr.communitrix;

import java.util.Random;
import aurelienribon.tweenengine.Tween;
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
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
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
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.networking.NetworkingManager;
import fr.pierreqr.communitrix.networking.commands.rx.*;
import fr.pierreqr.communitrix.screens.SCLobby;
import fr.pierreqr.communitrix.tweeners.CameraAccessor;
import fr.pierreqr.communitrix.tweeners.GameObjectAccessor;
import fr.pierreqr.communitrix.tweeners.PointLightAccessor;

public class Communitrix extends Game implements ErrorResponder, NetworkingManager.NetworkDelegate {
  // Common materials.
  public final static   Material[]  faceMaterials         = new Material[12];
  // Various constants.
  private static final  String      LogTag                = "Communitrix";
  private static final  String      host                  = "localhost";

  // Shared members.
  public          ApplicationType   applicationType;
  public          int               viewWidth, viewHeight;
  public          Skin              uiSkin;
  public          ModelBuilder      modelBuilder;
  public          ModelBatch        modelBatch;
  public          Material          defaultMaterial;
  public          G3dModelLoader    modelLoader;
  public          Model             dummyModel;
  // Random generator.
  public          Random            rand;
  // Network-related objects.
  public          NetworkingManager networkingManager;
  public          Timer             networkTimer;
  // The current error responder if any.
  private         ErrorResponder    errorResponder;
  
  // The singleton isntance.
  private static  Communitrix       instance;
  
  // All our different screens.
  private         SCLobby           lobbyScreen;

  public static Communitrix getInstance() {
    return instance;
  }
  
  public Communitrix () {
    // Store singleton instance.
    setErrorResponder       (instance = this);
    // Configure assets etc.
    ShaderLoader.BasePath   = "shaders/";
    // Prepare our random generator instance.
    rand                    = new Random();
    // Register motion tweening accessors.
    Tween.setCombinedAttributesLimit  (6);
    Tween.registerAccessor  (GameObject.class,        new GameObjectAccessor());
    Tween.registerAccessor  (PointLight.class,        new PointLightAccessor());
    Tween.registerAccessor  (PerspectiveCamera.class, new CameraAccessor());
  }
  // Getters / Setters.
  public void setErrorResponder (final ErrorResponder newErrorResponder) {
    errorResponder          = newErrorResponder;
  }
  
  @Override public void create () {
    // Cache application type.
    applicationType     = Gdx.app.getType();

    // After starting the application, we can query for the desktop dimensions
    boolean fullScreen = true;
    if (fullScreen && applicationType==ApplicationType.Desktop)
      Gdx.graphics.setDisplayMode (Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);
    
    // Prepare face materials.
    if (faceMaterials[0]==null) {
      final TextureAtlas  atlas       = new TextureAtlas(Gdx.files.internal("atlases/game.atlas"));
      final Attribute     blend       = new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.95f);
      for (final CubeFace face : Constants.CubeFace.values())
        faceMaterials[face.ordinal()]   = new Material(TextureAttribute.createDiffuse(atlas.findRegion(face.toString())), blend);
    }

    // Force cache viewport size.
    resize                  (Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    // Instantiate shared members.
    uiSkin                  = new Skin(Gdx.files.internal("skins/uiskin.json"));
    modelBuilder            = new ModelBuilder();
    modelBatch              = new ModelBatch();
    dummyModel              = new Model();
    defaultMaterial         = new Material(ColorAttribute.createDiffuse(Color.WHITE));
    
    // Instantiate networking manager.
    networkTimer            = new Timer();
    networkingManager       = new NetworkingManager(host, 9003, this);
    networkingManager.start ();
    
    // Set up our default error responder.
    errorResponder          = this;
    // Prepare our shared model loader.
    modelLoader             = new G3dModelLoader(new UBJsonReader());
    // Set default screen.
    setScreen               (getLazyLobbyScreen());
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
    if (lobbyScreen!=null)    lobbyScreen.dispose();
    modelBatch.dispose      ();
  }

  // Occurs whenever the viewport needs to render.
  @Override public void render () {
    super.render  ();
  }

  @Override public void resize (final int width, final int height) {
    viewWidth = width; viewHeight = height;
    // Propagate change to current screen instance.
    super.resize(width, height);
  }

  @Override public void onServerConnected () {
    Gdx.app.log             (LogTag, "Connected to the server.");
  }
  @Override public void onServerDisconnected () {
    networkTimer.scheduleTask(new Timer.Task() { @Override public void run() { networkingManager.start(); } }, 1.0f);
  }
  // This method runs after rendering.
  @Override public void onServerMessage (final RXBase baseCmd) {
    if (baseCmd==null) {
      Gdx.app.error         (LogTag, "Received a NULL command!");
      return;
    }
    switch (baseCmd.type) {
      case Error: {
        final RXError cmd = (RXError)baseCmd;
        setLastError(cmd.code, cmd.reason);
        break;
      }
      case Acknowledgment: {
        final RXAcknowledgment  cmd   = (RXAcknowledgment)baseCmd;
        getLazyLobbyScreen()
          .handleAcknowledgment(cmd.serial, cmd.valid, cmd.errorMessage);
        break;
      }
      case Welcome: {
        getLazyLobbyScreen()
          .setState         (SCLobby.State.Global);
        setScreen           (lobbyScreen);
        break;
      }
      case Registered: {
        break;
      }
      case CombatList: {
        final RXCombatList cmd = (RXCombatList)baseCmd;
        lobbyScreen.setCombats  (cmd.combats);
        break;
      }
      case CombatJoin: {
        final RXCombatJoin cmd = (RXCombatJoin)baseCmd;
        getLazyLobbyScreen()
          .setCombat        (cmd.combat)
          .setPlayers       (((RXCombatJoin)baseCmd).combat.players);
        lobbyScreen
          .setState         (SCLobby.State.Joined);
        break;
      }
      case CombatPlayerJoined: {
        getLazyLobbyScreen  ()
          .addPlayer          (((RXCombatPlayerJoined)baseCmd).player);
        break;
      }
      case CombatPlayerLeft: {
        final RXCombatPlayerLeft cmd = (RXCombatPlayerLeft)baseCmd;
        getLazyLobbyScreen  ()
          .removePlayer       (cmd.uuid);
        break;
      }
      case CombatStart: {
        final RXCombatStart cmd   = (RXCombatStart)baseCmd;
        Gdx.app.log(LogTag, "Server is sending us into combat (" +
                              "UUID: " + cmd.uuid + ", " +
                              "Target blocks: " + cmd.target.content.length + ", " +
                              "Cells: " + cmd.units.length + ", " +
                              "Pieces: " + cmd.pieces.length + ").");
        getLazyLobbyScreen()
          .prepare        (cmd.target, cmd.units, cmd.pieces)
          .setState       (SCLobby.State.Gaming);
        break;
      }
      case CombatNewTurn: {
        RXCombatNewTurn     cmd = (RXCombatNewTurn)baseCmd;
        Gdx.app.log         (LogTag, "Server is telling us to move to new turn " + cmd.turnId + ".");
        getLazyLobbyScreen()
          .setTurn        (cmd.turnId, cmd.unitId)
          .setState       (SCLobby.State.Gaming);
        break;
      }
      case CombatPlayerTurn: {
        RXCombatPlayerTurn  cmd = (RXCombatPlayerTurn)baseCmd;
        getLazyLobbyScreen()
          .registerPlayerTurn   (cmd.playerUUID, cmd.unitId, cmd.unit);
         break;
      }
      case CombatEnd: {
        // TODO: Display results in current screen.
        break;
      }
      default:
        Gdx.app.log         (LogTag, "Unhandled command type: " + baseCmd.type + ".");
    }
  }

  private SCLobby getLazyLobbyScreen    () {
    return lobbyScreen==null ? lobbyScreen = new SCLobby(this) : lobbyScreen;
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
