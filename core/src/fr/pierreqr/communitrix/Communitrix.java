package fr.pierreqr.communitrix;

import java.util.Random;
import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.GameObjectAccessor;
import fr.pierreqr.communitrix.networking.NetworkingManager;
import fr.pierreqr.communitrix.networking.commands.rx.RXBase;
import fr.pierreqr.communitrix.networking.commands.rx.RXCombatJoin;
import fr.pierreqr.communitrix.networking.commands.rx.RXCombatList;
import fr.pierreqr.communitrix.networking.commands.rx.RXCombatNewTurn;
import fr.pierreqr.communitrix.networking.commands.rx.RXCombatPlayerJoined;
import fr.pierreqr.communitrix.networking.commands.rx.RXCombatPlayerLeft;
import fr.pierreqr.communitrix.networking.commands.rx.RXCombatPlayerTurn;
import fr.pierreqr.communitrix.networking.commands.rx.RXCombatStart;
import fr.pierreqr.communitrix.networking.commands.rx.RXError;
import fr.pierreqr.communitrix.networking.commands.rx.RXWelcome;
import fr.pierreqr.communitrix.screens.SCCombat;
import fr.pierreqr.communitrix.screens.SCLobby;

public class Communitrix extends Game implements ErrorResponder, NetworkingManager.NetworkDelegate {
  // Possible directions around a cube to check for.
  public final static   int         Left                  = 0;
  public final static   int         Right                 = Left+1;
  public final static   int         Bottom                = Right+1;
  public final static   int         Top                   = Bottom+1;
  public final static   int         Backward              = Top+1;
  public final static   int         Forward               = Backward+1;
  // Some rotation constants.
  public final static   Vector3     PositiveX             = new Vector3( 1,  0,  0);
  public final static   Vector3     NegativeX             = new Vector3(-1,  0,  0);
  public final static   Vector3     PositiveY             = new Vector3( 0,  1,  0);
  public final static   Vector3     NegativeY             = new Vector3( 0, -1,  0);
  public final static   Vector3     PositiveZ             = new Vector3( 0,  0,  1);
  public final static   Vector3     NegativeZ             = new Vector3( 0,  0, -1);
  // Common materials.
  public  static final  Material[]  faceMaterials         = new Material[6];
  // Various constants.
  private static final  String      LogTag                = "Communitrix";
  public  static final  float       CellComponentRadius   = 0.5f;

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
  private         SCLobby       lobbyScreen;
  private         SCCombat      combatScreen;

  public static Communitrix getInstance() {
    return instance;
  }
  
  public Communitrix () {
    // Store singleton instance.
    instance                = this;
    // Configure assets etc.
    ShaderLoader.BasePath   = "shaders/";
    // Prepare our random generator instance.
    rand                    = new Random();
    // Register motion tweening accessors.
    Tween.registerAccessor  (GameObject.class, new GameObjectAccessor());
  }
  // Getters / Setters.
  public void setErrorResponder (final ErrorResponder er) {
    errorResponder          = er;
  }
  
  @Override public void create () {
    // Cache application type.
    applicationType         = Gdx.app.getType();

    // After starting the application, we can query for the desktop dimensions
    if (applicationType==ApplicationType.Desktop) {
      //final DisplayMode     dm    = Gdx.graphics.getDesktopDisplayMode();
      //Gdx.graphics.setDisplayMode (dm.width, dm.height, true);
    }
    
    // Prepare face materials.
    if (faceMaterials[0]==null) {
      faceMaterials[0]  = new Material(ColorAttribute.createDiffuse(0.9f, 0.6f, 0.6f, 1.0f));
      faceMaterials[1]  = new Material(ColorAttribute.createDiffuse(0.8f, 0.8f, 0.4f, 1.0f));
      faceMaterials[2]  = new Material(ColorAttribute.createDiffuse(0.6f, 0.9f, 0.6f, 1.0f));
      faceMaterials[3]  = new Material(ColorAttribute.createDiffuse(0.8f, 0.4f, 0.8f, 1.0f));
      faceMaterials[4]  = new Material(ColorAttribute.createDiffuse(0.6f, 0.6f, 0.9f, 1.0f));
      faceMaterials[5]  = new Material(ColorAttribute.createDiffuse(0.4f, 0.8f, 0.8f, 1.0f));
    }

    // Force cache viewport size.
    resize                  (Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    // Instantiate shared members.
    uiSkin                  = new Skin(Gdx.files.internal("skins/uiskin.json"));
    modelBuilder            = new ModelBuilder();
    modelBatch              = new ModelBatch();
    dummyModel              = new Model();
    defaultMaterial         = new Material(ColorAttribute.createDiffuse(Color.WHITE));
    // Start talking with the server.
    // Instantiate networking manager.
    networkTimer            = new Timer();
    networkingManager       = new NetworkingManager("localhost", 9003, this);
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
    Gdx.app.log     (LogTag, "An error has occured: #" + code + " - " + reason);
  };
  
  // Occurs when the game exits.
  @Override public void dispose () {
    if (networkingManager!=null) {
      networkTimer.stop       ();
      networkingManager.stop  ();
    }
    if (dummyModel!=null)     dummyModel.dispose();
    if (combatScreen!=null)   combatScreen.dispose();
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
        if (errorResponder==null)
          errorResponder  = this;
        errorResponder.setLastError(cmd.code, cmd.reason);
        break;
      }
      case Welcome: {
        Gdx.app.log         (LogTag, "Server is welcoming us: " + ((RXWelcome)baseCmd).message);
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
        setScreen(
          getLazyLobbyScreen()
            .setState         (SCLobby.State.Joined)
            .setPlayers       (((RXCombatJoin)baseCmd).combat.players)
        );
        break;
      }
      case CombatPlayerJoined: {
        getLazyLobbyScreen  ()
          .addPlayer          (((RXCombatPlayerJoined)baseCmd).player);
        break;
      }
      case CombatPlayerLeft: {
        final RXCombatPlayerLeft cmd = (RXCombatPlayerLeft)baseCmd;
        Gdx.app.log(LogTag, "Player " + cmd.uuid + " has left the lobby.");
        getLazyLobbyScreen  ()
          .removePlayer       (cmd.uuid);
        break;
      }
      case CombatStart: {
        final RXCombatStart cmd   = (RXCombatStart)baseCmd;
        Gdx.app.log(LogTag, "Server is sending us into combat (" +
                              "UUID: " + cmd.uuid + ", " +
                              "Target blocks: " + cmd.target.content.length + ", " +
                              "Cells: " + cmd.cells.length + ", " +
                              "Pieces: " + cmd.pieces.length + ").");
        setScreen(
            getLazyLobbyScreen()
              .prepare        (cmd.target, cmd.pieces)
          );
        // Should in fact be this one.
        getLazyCombatScreen()
          .setUp            (combatScreen.new Configuration(cmd, lobbyScreen.players));
        break;
      }
      case CombatNewTurn: {
        RXCombatNewTurn     cmd = (RXCombatNewTurn)baseCmd;
        Gdx.app.log         (LogTag, "Server is telling us to move to new turn " + cmd.turnId + ".");
        break;
      }
      case CombatPlayerTurn: {
        RXCombatPlayerTurn  cmd = (RXCombatPlayerTurn)baseCmd;
        setScreen(
          getLazyLobbyScreen()
            .prepare        (cmd.piece, null)
        );
         break;
      }
      case CombatEnd: {
      }
      default:
        Gdx.app.log         (LogTag, "Unhandled command type: " + baseCmd.type + ".");
    }
  }

  private SCLobby getLazyLobbyScreen    () { return lobbyScreen==null   ? lobbyScreen   = new SCLobby(this)   : lobbyScreen; }
  private SCCombat getLazyCombatScreen  () { return combatScreen==null  ? combatScreen  = new SCCombat(this)  : combatScreen; }
}
