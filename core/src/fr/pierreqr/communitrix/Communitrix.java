package fr.pierreqr.communitrix;

import java.util.Random;
import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.GameObjectAccessor;
import fr.pierreqr.communitrix.networking.NetworkingManager;
import fr.pierreqr.communitrix.networking.commands.rx.*;
import fr.pierreqr.communitrix.networking.commands.tx.*;
import fr.pierreqr.communitrix.screens.CombatScreen;
import fr.pierreqr.communitrix.screens.LobbyScreen;

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
  public          Model             dummyModel        = null;
  // Random generator.
  public          Random            rand              = null;

  public          NetworkingManager networkingManager = null;
  public          Timer             networkTimer      = null;
  public          FPSLogger         fpsLogger         = null;

  private static  Communitrix       instance          = null;

  // All our different screens.
  private         LobbyScreen       lobbyScreen       = null;
  private         CombatScreen      combatScreen      = null;

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
    ShaderLoader.BasePath   = "shaders/";

    // Force cache viewport size.
    resize                  (Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    // Instantiate shared members.
    uiSkin                  = new Skin(Gdx.files.internal("skins/uiskin.json"));
    modelBuilder            = new ModelBuilder();
    modelBatch              = new ModelBatch();
    dummyModel              = new Model();
    rand                    = new Random();
    defaultMaterial         = new Material(ColorAttribute.createDiffuse(Color.WHITE));
    networkingManager       = new NetworkingManager("www.pierreqr.fr", 9003, this);
    networkTimer            = new Timer();
    networkingManager.start ();
    // Prepare our shared model loader.
    UBJsonReader reader     = new UBJsonReader();
    modelLoader             = new G3dModelLoader(reader);

    // Set default screen.
    setScreen               (getLazyLobbyScreen());

    // Prepare our FPS logging object.
    fpsLogger               = new FPSLogger();

    // Register motion tweening accessors.
    Tween.registerAccessor  (GameObject.class, new GameObjectAccessor());
  }
  @Override public void setScreen(Screen screen) {
    if (getScreen()!=screen)  super.setScreen(screen);
  }

  // Occurs when the game exits.
  @Override public void dispose () {
    if (networkingManager!=null)  networkingManager.stop();
    if (combatScreen!=null)       combatScreen.dispose();
    if (lobbyScreen!=null)        lobbyScreen.dispose();
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

  @Override public void onServerConnected () {
    networkingManager.send  (new TXRegister("Doodloo"));
  }
  @Override public void onServerDisconnected () {
    networkTimer.scheduleTask(new Timer.Task() { @Override public void run() { networkingManager.start(); } }, 3.0f);
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
        Gdx.app.error     (LogTag, "Server just notified us of an error #" + cmd.code + ": " + cmd.reason);
        break;
      }
      case Welcome: {
        Gdx.app.log         (LogTag, "Server is welcoming us: " + ((RXWelcome)baseCmd).message);
        getLazyLobbyScreen()
          .setState         (LobbyScreen.State.Global);
        setScreen           (lobbyScreen);
        break;
      }
      case Registered: {
        networkingManager.send(new TXCombatList());
        break;
      }
      case CombatList: {
        final RXCombatList cmd = (RXCombatList)baseCmd;
        if (cmd.combats.isEmpty()) {
          Gdx.app.log(LogTag, "No combat found on server!");
        } else {
          networkingManager.send(new fr.pierreqr.communitrix.networking.commands.tx.TXCombatJoin(cmd.combats.get(0)));
        }
        break;
      }
      case CombatJoin: {
        setScreen(
          getLazyLobbyScreen()
            .setState         (LobbyScreen.State.Global)
            .setPlayers       (((RXCombatJoin)baseCmd).combat.players)
        );
        break;
      }
      case CombatPlayerJoined: {
        getLazyLobbyScreen()
          .addPlayer        (((RXCombatPlayerJoined)baseCmd).player);
        break;
      }
      case CombatPlayerLeft: {
        final RXCombatPlayerLeft cmd = (RXCombatPlayerLeft)baseCmd;
        Gdx.app.log(LogTag, "Player " + cmd.uuid + " has left the lobby.");
        getLazyLobbyScreen()
          .removePlayer     (cmd.uuid);
        break;
      }
      case CombatStart: {
        final RXCombatStart cmd   = (RXCombatStart)baseCmd;
        Gdx.app.log(LogTag, "Server is sending us into combat (" +
                              "UUID: " + cmd.uuid + ", " +
                              "Target blocks: " + cmd.target.size.volume() + ", " +
                              "Cells: " + cmd.cells.length + ", " +
                              "Pieces: " + cmd.pieces.length + ").");
        setScreen(
            getLazyLobbyScreen()
              .setRemotePiece(cmd.target)
              .setPieces(cmd.pieces)
          );
        // Should in fact be this one.
        getLazyCombatScreen()
          .setUp            (combatScreen.new Configuration(cmd, lobbyScreen.players));
        break;
      }
      case CombatNewTurn: {
      }
      case CombatPlayerTurn: {
        RXCombatPlayerTurn  cmd = (RXCombatPlayerTurn)baseCmd;
        setScreen(
          getLazyLobbyScreen()
            .setRemotePiece(cmd.piece)
        );
         break;
      }
      case CombatEnd: {
      }
      default:
        Gdx.app.log         (LogTag, "Unhandled command type: " + baseCmd.type + ".");
    }
  }

  private LobbyScreen getLazyLobbyScreen    () { return lobbyScreen==null   ? lobbyScreen   = new LobbyScreen(this)   : lobbyScreen; }
  private CombatScreen getLazyCombatScreen  () { return combatScreen==null  ? combatScreen  = new CombatScreen(this)  : combatScreen; }
}
