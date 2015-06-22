package fr.pierreqr.communitrix.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Bounce;
import aurelienribon.tweenengine.equations.Expo;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.MotionBlur;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.GameObjectAccessor;
import fr.pierreqr.communitrix.gameObjects.Piece;
import fr.pierreqr.communitrix.networking.commands.tx.TXCombatPlayTurn;
import fr.pierreqr.communitrix.networking.shared.SHPiece;
import fr.pierreqr.communitrix.networking.shared.SHPlayer;
import fr.pierreqr.communitrix.screens.inputControllers.ICLobby;
import fr.pierreqr.communitrix.screens.inputControllers.ICLobby.ICLobbyDelegate;
import fr.pierreqr.communitrix.screens.ui.UILobby;
import fr.pierreqr.communitrix.screens.util.PiecesDock;
import fr.pierreqr.communitrix.screens.util.PiecesDock.PiecesDockDelegate;

public class SCLobby implements Screen, ICLobbyDelegate, PiecesDockDelegate {
  public                enum          State         { Unknown, Global, Joined, Starting, NewTurn }
  public                enum          CameraState   { Unknown, Lobby, Pieces, Target, Unit, Observe }
  private final static  Quaternion    tmpQuat       = new Quaternion();
  private final static  Vector3       tmpVec3       = new Vector3();
  private static final  String        LogTag        = "LobbyScreen";
  
  // Game instance cache.
  private final Communitrix           ctx;
  // Flat UI.
  private       UILobby               ui;
  // Scene setup related objects.
  private final TweenManager          tweener;
  private final Environment           envMain;
  private final PerspectiveCamera     camMain;
  private final ICLobby               combCtrlMain;
  private final PostProcessor         postProMain;
  // State related members.
  private       State                 state         = State.Unknown;
  private       CameraState           cameraState   = CameraState.Unknown;
  public final  Array<SHPlayer>       players       = new Array<SHPlayer>();
  private       int                   currentTurn   = 0;
  // Various object instances.
  private final Model                 characterModel;
  private final Array<GameObject>     instances     = new Array<GameObject>();
  private final Map<String,GameObject>characters    = new HashMap<String,GameObject>();
  // Model instances.
  private final Piece                 target;
  private final Piece                 unit;
  private final Array<Piece>          pieces;
  private final Array<Piece>          clickables;
  private final PiecesDock            piecesDock;
  
  public SCLobby (final Communitrix communitrix) {
    Gdx.app.log           (LogTag, "Constructing.");
    // Cache our game instance.
    ctx                   = communitrix;
    // Initialize our UI manager.
    ui                    = new UILobby();
    ctx.setErrorResponder (ui);
    // Initialize tweening engine.
    tweener               = new TweenManager();
    
    // Set up the scene environment.
    envMain               = new Environment();
    envMain.set           (new ColorAttribute(ColorAttribute.AmbientLight, 0.7f, 0.7f, 0.7f, 1.0f));
    envMain.set           (new ColorAttribute(ColorAttribute.Fog, 0.01f, 0.01f, 0.01f, 1.0f));
    envMain.add           (new DirectionalLight().set(new Color(0.3f, 0.3f, 0.3f, 1.0f), -1f, -0.8f, -0.2f));
    
    // Set up the main post-processor.
    postProMain           = new PostProcessor(true, true, true);
    if (ctx.applicationType!=ApplicationType.WebGL) {
      // Add bloom to post-processor.
      Bloom blm             = new Bloom(ctx.viewWidth/3, ctx.viewHeight/3);
      blm.setBloomIntesity  (0.6f);
      blm.setBloomSaturation(0.7f);
      postProMain.addEffect (blm);
      // Add motion blur to post-processor.
      MotionBlur blur       = new MotionBlur();
      blur.setBlurOpacity   (0.70f);
      postProMain.addEffect (blur);
    }

    // Set up our main camera, and position it.
    camMain               = new PerspectiveCamera(90, ctx.viewWidth, ctx.viewHeight);
    camMain.position.set  (0f, 5f, -10f);
    camMain.near          = 1f;
    camMain.far           = 150f;
    camMain.lookAt        (0, 0, 0);
    camMain.update        ();
    
    // Prepare character model.
    characterModel        = ctx.modelBuilder.createBox(2, 2, 2, ctx.defaultMaterial, Usage.Position | Usage.Normal);

    // Create main fuel cell.
    target                = new Piece();
    target.transform
      .translate          (5, 5, 0);
    instances.add         (target);
    unit                  = new Piece();
    unit.transform.translate(-5,5,0);
    instances.add          (unit);
    pieces                = new Array<Piece>();
    clickables            = new Array<Piece>();
    
    piecesDock            = new PiecesDock(this);
    
    // Instantiate our interaction controller.
    combCtrlMain          = new ICLobby(this);
  }
  // Setter on state, handles transitions.
  public SCLobby setState (final State state) {
    if (this.state!=state) {
      ui.setState         (this.state = state);
      switch (state) {
        case Global:
          ui.loadCombatList();
          Gdx.input.setInputProcessor (ui.getStage());
          break;
        case Joined:
          setCameraState(CameraState.Lobby);
          Gdx.input.setInputProcessor (combCtrlMain);
          break;
        case Starting:
          setState(State.NewTurn);
          currentTurn = 1;
          break;
        case NewTurn:
          setCameraState(CameraState.Pieces);
          break;
        default :
          break;
      }
    }
    return this;
  }
  
  public void setCameraState (final CameraState cameraState) {
    switch (cameraState) {
      case Lobby:
        break;
      case Pieces: // Tween was not working as intended
//        Tween
//        .to             (camMain, PerspectiveCameraAccessor.TransZ, 0.5f)
//        .target         (-10f)
//        .start          (tweener);
        camMain.position.set(0f, 5f, -10f);
        camMain.lookAt  (0,0,0);
        camMain.update  ();
        break;
      case Target:
//        Tween
//        .to             (camMain, PerspectiveCameraAccessor.TransZ, 0.5f)
//        .target         (-5f)
//        .start          (tweener);
        target.transform.getTranslation(tmpVec3);
        camMain.position.set(tmpVec3.x, tmpVec3.y + 3, tmpVec3.z - 3);
        camMain.lookAt  (tmpVec3.x, tmpVec3.y, tmpVec3.z);
        camMain.update  ();
        break;
      case Unit:
        unit.transform.getTranslation(tmpVec3);
        camMain.position.set(tmpVec3.x, tmpVec3.y + 2, tmpVec3.z - 5);
        camMain.lookAt  (tmpVec3.x, tmpVec3.y, tmpVec3.z);
        camMain.update  ();
        break;
      case Observe:
        break;
      default:
        Communitrix.getInstance().setLastError(0, "Unknown Lobby Camera state: " + cameraState);
        break;
    }
    this.cameraState    = cameraState;
  }
  
  public CameraState getCameraState(){
    return cameraState;
  }
  
  public int getTurn(){
    return currentTurn;
  }
  
  // Whenever the server updates the client with a combat list, this gets called.
  public SCLobby setCombats (final String[] combats) {
    ui.setCombats (combats);
    return        this;
  }
  // Setter on players, handles list population.
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
    Tween
      .to                 (obj, GameObjectAccessor.TransY, 1.2f)
      .target             (0)
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
        Tween
          .to             (obj, GameObjectAccessor.TransX, 0.5f)
          .target         ((idx-1) * 2.5f)
          .delay          (0.3f)
          .ease           (Bounce.OUT)
          .start          (tweener);
      }
      else if (player.uuid.equals(uuid)) {
        final GameObject obj  = characters.remove(player.uuid);
        remove                = player;
        shift                 = true;
        Tween
          .to             (obj, GameObjectAccessor.TransYRotX, 0.5f)
          .target         (-30.0f, 180.0f)
          .ease           (aurelienribon.tweenengine.equations.Expo.IN)
          .start          (tweener)
          .setCallback    (new TweenCallback() { @Override public void onEvent(int arg0, BaseTween<?> arg1) {
            instances.removeValue (obj, true);
          } });
      }
      ++idx;
    }
    players.removeValue   (remove, true);
    return this;
  }
  
  public Camera       getCamera       () { return camMain; }
  public Array<Piece> getPieces       () { return pieces; }
  public Piece        getTarget       () { return target; }
  public Piece        getUnit         () { return unit; }
  public Array<Piece> getClickables   () { return clickables; }
  
  public void cyclePieces (final int firstPieceIndex) {
    piecesDock.setFirstPieceIndex(firstPieceIndex);
  }
  public void translatePiece (final Piece piece, final Vector3 translation) {
    piece.targetTransform
      .getTranslation     (tmpVec3)
      .add                (translation);
    piece.targetTransform
      .setTranslation     (tmpVec3);
    int       order       = 0;

    byte      reqSize     = 0;
    if (translation.x!=0.0f) {
      order     = order | GameObjectAccessor.TransX;
      reqSize   ++;
    }
    if (translation.y!=0.0f) {
      order     = order | GameObjectAccessor.TransY;
      reqSize   ++;
    }
    if (translation.z!=0.0f) {
      order     = order | GameObjectAccessor.TransZ;
      reqSize   ++;
    }
    final float[] targets     = new float[reqSize];
    reqSize                   = 0;
    if (translation.x!=0.0f)  targets[reqSize++]   = tmpVec3.x;
    if (translation.y!=0.0f)  targets[reqSize++]   = tmpVec3.y;
    if (translation.z!=0.0f)  targets[reqSize++]   = tmpVec3.z;
    
    Tween
      .to                 (piece, order, 0.6f)
      .target             (targets)
      .ease               (Expo.OUT)
      .start              (tweener);
  }
  public void rotatePiece (final Piece piece, final Vector3 axis, final int angle) {
    int         order   = 0;
    int         trgt  = 0;
    if (axis==Vector3.X) {
     order    = GameObjectAccessor.RotX;
     trgt     = piece.targetAngles.x += angle;
    }
    else if (axis==Vector3.Y) {
      order   = GameObjectAccessor.RotY; 
      trgt    = piece.targetAngles.y += angle;
    }
    else if (axis==Vector3.Z) {
      order   = GameObjectAccessor.RotZ;
      trgt    = piece.targetAngles.z += angle;
    }
    Tween
      .to                 (piece, order, 0.6f)
      .target             (trgt)
      .ease               (Expo.OUT)
      .start              (tweener);
  }
  public void playPiece (final Piece piece) {
      // XXX not working, removes the color of all models
//    ColorAttribute attr = ColorAttribute.createDiffuse(.5f, .5f, .5f, 1f);
//    for(int i = 0; i < piece.nodes.get(0).parts.size; i++){
//      piece.nodes.get(0).parts.get(i).material.set(attr);
//    }
    
    final int idx = pieces.indexOf(piece, true);
    // TODO: Find the correct relative translation / rotation to send when we'll have a nice Unit object to work with.
    piece.transform.getRotation     (tmpQuat);
    piece.transform.getTranslation  (tmpVec3);
    final TXCombatPlayTurn  cmdPlayTurn = new TXCombatPlayTurn(idx, tmpQuat, tmpVec3);
    Communitrix
      .getInstance()
      .networkingManager
      .send(cmdPlayTurn);
  }
  
  public void setTurn(final int turn){
    currentTurn = turn;
  }
  public void setTurnUnit (final SHPiece newUnit){
    unit.setFromSharedPiece(newUnit);
    clickables.add(unit);
  }

  public SCLobby prepare (final SHPiece target, final SHPiece[] newPieces) {
    // Set up the target.
    if (target!=null)
      this.target.setFromSharedPiece(target);
    // Place all my pieces.
    if (newPieces!=null) {
      tmpVec3.set               (0, 0, -5);
      pieces.clear              ();
      for (int i=0; i<newPieces.length; i++) {
        final Piece   obj       = new Piece();
        obj.transform
        .setTranslation(tmpVec3);
        obj.targetTransform
          .setTranslation(tmpVec3);
        obj.setFromSharedPiece  (newPieces[i]);
        pieces.add              (obj);
        instances.add           (obj);
      }
      clickables.clear();
      clickables.add(this.target);
      clickables.addAll(pieces);
      cyclePieces               (0);
    }
    return this;
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
    Gdx.gl.glViewport(0, 0, ctx.viewWidth, ctx.viewHeight);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    // Enable alpha blending.
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    // Enable back-face culling.
    Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    Gdx.gl.glCullFace(GL20.GL_BACK);
    
    // Update any pending tweening.
    tweener.update      (delta);

    // Update camera controller.
    combCtrlMain.update ();

    // Capture FBO for post-processing.
    postProMain.capture();
    
    // Mark the beginning of our rendering phase.
    ctx.modelBatch.begin(camMain);
    // Render all instances in our batch array.
    for (final GameObject instance : instances)
      if (instance.isVisible(camMain))
        ctx.modelBatch.render(instance, envMain);
    // Rendering is over.
    ctx.modelBatch.end();
    
    // Apply post-processing.
    postProMain.render();

    // Update flat UI.
    ui.actAndDraw             (delta);
  }

  @Override public void resize (final int width, final int height) {
    // Update flat UI.
    ui.resize           (width, height);
    // If a post-processor exists, update it.
    postProMain.rebind  ();
  }
}
