package fr.pierreqr.communitrix.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Bounce;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.bitfire.postprocessing.PostProcessor;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.Camera;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.Piece;
import fr.pierreqr.communitrix.networking.commands.tx.TXCombatPlayTurn;
import fr.pierreqr.communitrix.networking.shared.SHCombat;
import fr.pierreqr.communitrix.networking.shared.SHPiece;
import fr.pierreqr.communitrix.networking.shared.SHPlayer;
import fr.pierreqr.communitrix.screens.inputControllers.ICLobby;
import fr.pierreqr.communitrix.screens.inputControllers.ICLobby.ICLobbyDelegate;
import fr.pierreqr.communitrix.screens.ui.UILobby;
import fr.pierreqr.communitrix.screens.ui.UILobby.UILobbyDelegate;
import fr.pierreqr.communitrix.screens.util.PiecesDock;
import fr.pierreqr.communitrix.screens.util.PiecesDock.PiecesDockDelegate;
import fr.pierreqr.communitrix.tweeners.CameraAccessor;
import fr.pierreqr.communitrix.tweeners.GameObjectAccessor;

public class SCLobby implements Screen, UILobbyDelegate, ICLobbyDelegate, PiecesDockDelegate {
  public                enum          State         { Unknown, Global, Joined, NewTurn }
  public                enum          CameraState   { Unknown, Lobby, Pieces, Unit, Observe }
  
  // Possible POV / Targets for camera.
  public final static HashMap<CameraState, float[]> CameraPOVs  = new HashMap<CameraState, float[]>();
  static {
    CameraPOVs.put(CameraState.Lobby,   new float[]{});
    CameraPOVs.put(CameraState.Pieces,  new float[]{ 0, 5,-10,  0, 0, 0 });
    CameraPOVs.put(CameraState.Unit,    new float[]{ 0, 5, -6,  0, 3, 0 });
  }
  // Various locations for objects.
  public  final static  HashMap<String, float[]> Locations   = new HashMap<String, float[]>();

  private final static  Vector3       tmpVec        = new Vector3();
  private final static  Quaternion    tmpQuat       = new Quaternion();
  private final static  Matrix4       tmpMat4       = new Matrix4();
  private static final  String        LogTag        = "LobbyScreen";
  
  // Game instance cache.
  private final Communitrix           ctx;
  // Flat UI.
  private       UILobby               ui;
  // Scene setup related objects.
  private final TweenManager          tweener;
  private final Environment           envMain;
  private final Camera                camMain, camTarget;
  private final ICLobby               combCtrlMain;
  private final PostProcessor         postProMain;

  // State related members.
  private       State                 state         = State.Unknown;
  private       CameraState           cameraState   = CameraState.Unknown;
  // The last loaded list of combats.
  private final Array<SHCombat>       combats       = new Array<SHCombat>();
  // The current combat we've joined.
  private       SHCombat              combat        = null;
  // The list of players.
  private final Array<SHPlayer>       players       = new Array<SHPlayer>();
  // The currently selected piece.
  private       Piece                 selectedPiece = null;
  // The piece being played / that was played this turn.
  private       Piece                 playedPiece   = null;

  // Various object instances.
  private final Model                 characterModel;
  private final Array<GameObject>     instances     = new Array<GameObject>();
  private final Map<String,GameObject>characters    = new HashMap<String,GameObject>();
  // Model instances.
  private final Piece                 target;
  private final Array<SHPiece>        units;
  private final Piece                 unit;
  private final Array<Piece>          pieces;
  private final Array<Piece>          availablePieces;
  private final PiecesDock            piecesDock;

  private static        Vector3       relXAxis, relYAxis, relZAxis;

  public SCLobby (final Communitrix communitrix) {
    Gdx.app.log           (LogTag, "Constructing.");
    // Cache our game instance.
    ctx                   = communitrix;
    // Initialize our UI manager.
    ui                    = new UILobby(this);
    ctx.setErrorResponder (ui);
    // Initialize animation engine.
    tweener               = new TweenManager();
    
    // Prepare our relative axis if needed.
    if (relXAxis==null) {
      relZAxis  = new Vector3(Vector3.Z).scl(-1);
      relXAxis  = new Vector3(relZAxis).crs(Vector3.Y);
      relYAxis  = new Vector3(relXAxis).crs(relZAxis);
    }
    
    // Set up the scene environment.
    envMain               = new Environment();
    envMain.set           (new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1.0f));
    envMain.set           (new ColorAttribute(ColorAttribute.Fog, 0.01f, 0.01f, 0.01f, 1.0f));
    envMain.add           (new DirectionalLight().set(new Color(0.6f, 0.6f, 0.6f, 1.0f), -1f, -0.8f, -0.2f));
    
    // Set up the main post-processor.
    postProMain           = new PostProcessor(true, true, true);
    if (ctx.applicationType!=ApplicationType.WebGL) {
//      // Add bloom to post-processor.
//      Bloom blm             = new Bloom(ctx.viewWidth/3, ctx.viewHeight/3);
//      blm.setEnabled        (true);
//      blm.setBloomIntesity  (0.9f);
//      blm.setBloomSaturation(0.6f);
//      postProMain.addEffect (blm);
//      // Add motion blur to post-processor.
//      MotionBlur blur       = new MotionBlur();
//      blur.setEnabled       (true);
//      blur.setBlurOpacity   (0.65f);
//      postProMain.addEffect (blur);
    }

    // Set up our target camera.
    camTarget             = new Camera(60, ctx.viewWidth/2.5f, ctx.viewHeight/2.5f);
    camTarget.position.set(0, 2, -6);
    camTarget.lookAt      (0, 0, 0);
    camTarget.near        = 1f;
    camTarget.far         = 32f;
    camTarget.update      ();
    
    // Create objective piece. It is not added to the instances list, as it is rendered on the side.
    target                = new Piece();

    // Set up our scene.
    camMain               = new Camera(60, ctx.viewWidth, ctx.viewHeight);
    camMain.position.set  (0f, 5f, -10f);
    camMain.lookAt        (0, 0, 0);
    camMain.near          = 1f;
    camMain.far           = 32f;
    camMain.update        ();
    
    // Prepare character model.
    characterModel        = ctx.modelBuilder.createBox(2, 2, 2, ctx.defaultMaterial, Usage.Position | Usage.Normal);
    
    // Create unit piece.
    units                 = new Array<SHPiece>();
    unit                  = new Piece();
    unit.transform
      .translate          (unit.targetPosition.set(0, 3, 0));
    instances.add          (unit);
    // Create various arrays..
    pieces                = new Array<Piece>();
    availablePieces       = new Array<Piece>();
    
    piecesDock            = new PiecesDock(this);
    
    // Instantiate our interaction controller.
    combCtrlMain          = new ICLobby(this);
  }
  // Setter on state, handles transitions.
  public SCLobby setState (final State state) {
    if (this.state!=state) {
      switch (state) {
        case Global:
          Gdx.input
            .setInputProcessor  (ui.getStage());
          break;
        case Joined:
          Gdx.input
            .setInputProcessor  (combCtrlMain);
          setCameraState        (CameraState.Lobby);
          break;
        case NewTurn:
          setCameraState        (CameraState.Pieces);
          break;
        default :
          break;
      }
    }
    ui.setState         (this.state = state);
    return this;
  }
  
  public Array<SHCombat> getCombats () { return combats; }
  public SCLobby setCombats (final SHCombat[] newCombats) {
    combats.clear     ();
    combats.addAll    (newCombats);
    ui.updateCombats  ();
    return            this;
  }
  public SCLobby setCombat (final SHCombat newCombat) {
    combat      = newCombat;
    for (final SHCombat c : combats) {
      if (combat.uuid.equals(c.uuid)) {
        combat    = c;
        break;
      }
    }
    return      this;
  }
  public SHCombat getCombat () { return combat; }
  // Setter on players, handles list population.
  public Array<SHPlayer> getPlayers () {
    return players;
  }
  public SCLobby setPlayers (final ArrayList<SHPlayer> players) {
    Gdx.app.log         (LogTag, "Setting new player list (" + players.size() + ").");
    // Remove all character instances.
    for (final SHPlayer player : this.players)
      instances.removeValue(
        characters.remove(player.uuid)
      , true);
    // Clear character list.
    this.players.clear  ();
    // Create our players.
    for (final SHPlayer player : players)
      addPlayer         (player);
    return this;
  }
  public SCLobby addPlayer (final SHPlayer player) {
    Gdx.app.log           (LogTag, "Adding player (" + player.uuid + ").");
    final GameObject obj  = new GameObject(characterModel);
    obj.transform.setTranslation(players.size * 2.5f, 30, 10);
    characters.put        (player.uuid, obj);
    instances.add         (obj);
    players.add           (player);
    // Schedule animation.
    obj.targetPosition.y  = 0;
    obj.prepareTweening   (tweener);
    Tween
      .to                 (obj, GameObjectAccessor.TransY, 1.2f)
      .target             (obj.targetPosition.y)
      .ease               (Bounce.OUT)
      .start              (tweener);
    return this;
  }
  public SCLobby removePlayer (final String uuid) {
    Gdx.app.log       (LogTag, "Removing player (" + uuid + ").");
    boolean shift     = false;
    int     idx       = 0;
    SHPlayer  remove    = null;
    for (final SHPlayer player : players) {
      if (shift) {
        final GameObject obj  = characters.get(player.uuid);
        obj.targetPosition.x  = (idx-1) * 2.5f;
        Tween
          .to             (obj, GameObjectAccessor.TransX, 0.5f)
          .target         (obj.targetPosition.x)
          .delay          (0.3f)
          .ease           (Bounce.OUT)
          .start          (tweener);
      }
      else if (player.uuid.equals(uuid)) {
        final GameObject obj  = characters.remove(player.uuid);
        remove                = player;
        shift                 = true;
        final Matrix4     mat = new Matrix4(obj.targetRotation);
        mat.rotate            (Vector3.Y, 180);
        mat.getRotation       (obj.targetRotation);
        obj.targetPosition.y  = -30;
        obj.prepareTweening   (tweener);
        Tween
          .to                 (obj, GameObjectAccessor.AllAuto, 0.5f)
          .target             (1)
          .ease               (aurelienribon.tweenengine.equations.Expo.IN)
          .setCallback        (new TweenCallback() { @Override public void onEvent(int arg0, BaseTween<?> arg1) { instances.removeValue (obj, true); }})
          .start              (tweener);
      }
      ++idx;
    }
    players.removeValue   (remove, true);
    return this;
  }
  
  public int          getTurn         ()  { return combat.currentTurn; }
  public Camera       getCamera       ()  { return camMain; }
  public CameraState  getCameraState  ()  { return cameraState; }
  public void setCameraState (final CameraState cameraState) {
    final float[] pov = CameraPOVs.get(cameraState);
    Tween
      .to             (camMain, CameraAccessor.Trans, 0.3f)
      .target         (pov)
      .ease           (Quad.INOUT)
      .start          (tweener);
    this.cameraState  = cameraState;
  }
  public Piece        getUnit           () { return unit; }
  public Piece        getTarget         () { return target; }
  public Array<Piece> getPieces         () { return pieces; }
  public Array<Piece> getAvailablePieces() { return availablePieces; }
  public void cyclePieces (final int firstPieceIndex) {
    piecesDock.setFirstPieceIndex(firstPieceIndex);
  }
  public void selectPiece (final Piece piece) {
    if (selectedPiece!=piece) {
      selectedPiece   = piece;
      // This is a selection.
      if (selectedPiece!=null) {
        piece.targetPosition
          .set        (unit.targetPosition);
        Tween
          .to         (piece, GameObjectAccessor.TransXYZ, 0.3f)
          .target     (piece.targetPosition.x, piece.targetPosition.y, piece.targetPosition.z)
          .ease       (Quad.INOUT)
          .start      (tweener);
      }
      // This is a deselection.
      else {
        piecesDock.refresh    ();
      }
    }
  }
  public void translatePiece (final Piece piece, final Vector3 translation) {
    piece.targetPosition
      .add        (translation);
    piece.prepareTweening(tweener);
    Tween
      .to         (piece, GameObjectAccessor.AllAuto, 0.3f)
      .target     (1.0f)
      .ease       (Quad.INOUT)
      .start      (tweener);
  }
  public void rotatePiece (final Piece piece, final Vector3 axis, final int angle) {
    tmpMat4.idt();
    // Asked to rotate around X.
    if (axis==Vector3.X)      tmpMat4.rotate(relXAxis, angle);
    // Asked to rotate around Y.
    else if (axis==Vector3.Y) tmpMat4.rotate(relYAxis, angle);
    // Asked to rotate around Z, should never happen.
    else                      tmpMat4.rotate(relZAxis, angle);
    // Rotate by previous rotation and translation.
    tmpMat4
      .rotate       (piece.targetRotation)
      .getRotation  (piece.targetRotation);
    // Start animating.
    piece.prepareTweening (tweener);
    Tween
      .to                 (piece, GameObjectAccessor.AllAuto, 0.2f)
      .target             (1.0f)
      .start              (tweener);
  }
  public void resetPieceRotation (final Piece piece) {
    piece.targetRotation.idt();
    // Start animating.
    piece.prepareTweening (tweener);
    Tween
      .to                 (piece, GameObjectAccessor.AllAuto, 0.5f)
      .target             (1.0f)
      .ease               (Quad.INOUT)
      .start              (tweener);
  }
  public void playPiece (final Piece piece) {
    if (playedPiece!=null) {
      ctx.setLastError  (0, "You already have played this turn, please wait.");
      return;
    }
    else if (!availablePieces.contains(piece, true)) {
      ctx.setLastError  (0, "You cannot play this piece twice.");
      return;
    }
    // Get the index of the piece to be played.
    final int idx     = pieces.indexOf(piece, true);
    // Store the piece that is being played so we can rollback / validate upon server ack.
    playedPiece       = piece;
    // Get current unit rotation, invert it.
    tmpQuat
      .set      (unit.targetRotation)
      .conjugate();
    // Get piece location, and rotate it by the inverse of the unit rotation. Then calculate the delta.
    tmpVec
      .set      (piece.targetPosition.x, piece.targetPosition.y, piece.targetPosition.z)
      .sub      (unit.targetPosition.x, unit.targetPosition.y, unit.targetPosition.z)
      .mul      (tmpQuat);
    // Post-rotate the unit rotation by the piece rotation. The result is a translation / rotation relative to the unit.
    tmpQuat
      .mul      (piece.targetRotation);
    // Give the order!
    final TXCombatPlayTurn cmd  = new TXCombatPlayTurn(idx, tmpQuat, tmpVec);
    cmd.serial                  = "PlayTurn";
    ctx.networkingManager.send  (cmd);
  }
  public SCLobby setTurn (final int turn, final int unitId) {
    // Update our combat object.
    combat.currentTurn        = turn;
    // Register that no piece was played this turn.
    playedPiece               = null;
    final SHPiece currentUnit = units.get(unitId);
    if (currentUnit.size.volume()!=0)
      unit.setFromSharedPiece (currentUnit);
    return this;
  }
  
  public void handleAcknowledgment (final String serial, final boolean valid) {
    Gdx.app.log(LogTag, String.format("Serial: %s, Valid: %b", serial, valid));
    if (serial.equals("PlayTurn")) {
      if (valid) {
        availablePieces.removeValue (playedPiece, true);
        instances.removeValue       (playedPiece, true);
      }
      else {
        ctx.setLastError      (0, "This piece cannot be played there at this time.");
        playedPiece           = null;
      }
      piecesDock.refresh      ();
    }
  }
  
  public SCLobby prepare (final SHPiece target, final SHPiece[] newUnits, final SHPiece[] newPieces) {
    this.target
      .setFromSharedPiece       (target);
    // Place all my pieces.
    if (newPieces!=null) {
      tmpVec.set               (0, 0, -5);
      pieces.clear              ();
      availablePieces.clear     ();
      for (int i=0; i<newPieces.length; i++) {
        final Piece   obj       = new Piece();
        obj.transform
          .setTranslation(tmpVec);
        obj.targetPosition
          .set(tmpVec);
        obj.setFromSharedPiece  (newPieces[i]);
        pieces.add              (obj);
      }
      availablePieces.addAll    (pieces);
      instances.addAll          (pieces);
      units.clear               ();
      units.addAll              (newUnits);
      cyclePieces               (0);
    }
    return this;
  }
  public void registerPlayerTurn (final String playerUUID, final int unitId, final SHPiece unit) {
    final SHPiece oldUnit  = units.get(unitId);
    oldUnit.size    = unit.size;
    oldUnit.min     = unit.min;
    oldUnit.max     = unit.max;
    oldUnit.content = unit.content;
  }
  
  @Override public void show ()   {}
  @Override public void hide ()   {}
  @Override public void pause ()  {}
  @Override public void resume () {}
  
  @Override public void dispose () {
    characterModel.dispose  ();
    postProMain.dispose     ();
  }
  
  @Override public void render (final float delta) {
    // Clear viewport etc.
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    // Enable alpha blending.
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    // Enable back-face culling.
    Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    Gdx.gl.glCullFace(GL20.GL_BACK);
    
    // Update any pending animation.
    tweener.update      (delta);
    // Update camera controller.
    combCtrlMain.update ();

    // Capture FBO for post-processing.
    postProMain.capture ();
    
    // Mark the beginning of our rendering phase.
    Gdx.gl.glViewport(0, 0, ctx.viewWidth, ctx.viewHeight);
    ctx.modelBatch.begin(camMain);
    // Render all instances in our batch array.
    for (final GameObject instance : instances)
      if (instance.isVisible(camMain))
        ctx.modelBatch.render(instance, envMain);
    // Rendering is over.
    ctx.modelBatch.end  ();

    Gdx.gl.glViewport(0, (int)(ctx.viewHeight-(ctx.viewHeight/2.5f)), (int)(ctx.viewWidth/2.5f), (int)(ctx.viewHeight/2.5f));
    ctx.modelBatch.begin  (camTarget);
    ctx.modelBatch.render (target, envMain);
    ctx.modelBatch.end    ();

    Gdx.gl.glViewport(0, 0, ctx.viewWidth, ctx.viewHeight);
    // Apply post-processing.
    postProMain.render  ();

    // Update flat UI.
    ui.actAndDraw       (delta);
}

  @Override public void resize (final int width, final int height) {
    // Update flat UI.
    ui.resize           (width, height);
    // If a post-processor exists, update it.
    postProMain.rebind  ();
  }
}
