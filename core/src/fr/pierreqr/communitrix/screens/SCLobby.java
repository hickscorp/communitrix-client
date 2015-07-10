package fr.pierreqr.communitrix.screens;

import java.util.EnumMap;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.MotionBlur;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.Constants;
import fr.pierreqr.communitrix.Constants.Key;
import fr.pierreqr.communitrix.gameObjects.Camera;
import fr.pierreqr.communitrix.gameObjects.FacetedObject;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.Piece;
import fr.pierreqr.communitrix.networking.commands.tx.TXCombatPlayTurn;
import fr.pierreqr.communitrix.networking.shared.SHCombat;
import fr.pierreqr.communitrix.networking.shared.SHPiece;
import fr.pierreqr.communitrix.networking.shared.SHPlayer;
import fr.pierreqr.communitrix.networking.shared.SHUnit;
import fr.pierreqr.communitrix.screens.inputControllers.ICLobby;
import fr.pierreqr.communitrix.screens.inputControllers.ICLobby.ICLobbyDelegate;
import fr.pierreqr.communitrix.screens.ui.UILobby;
import fr.pierreqr.communitrix.screens.util.PiecesDock;
import fr.pierreqr.communitrix.screens.util.PiecesDock.PiecesDockDelegate;
import fr.pierreqr.communitrix.tweeners.CameraAccessor;

public class SCLobby implements Screen, ICLobbyDelegate, PiecesDockDelegate {
  public                enum          State         { Unknown, Settings, Global, Joined, Gaming, EndGame }
  public                enum          CameraState   { Unknown, Lobby, Pieces, Unit, Observe }
  
  // Possible POV / Targets for camera.
  public final static EnumMap<CameraState, float[]> CameraPOVs  = new EnumMap<CameraState, float[]>(CameraState.class);
  static {
    CameraPOVs.put(CameraState.Lobby,   new float[]{ 0, 2, -6,  0, 0, 0 });
    CameraPOVs.put(CameraState.Pieces,  new float[]{ 0, 4,-10,  0, 0, 0 });
    CameraPOVs.put(CameraState.Unit,    new float[]{ 0, 5, -6,  0, 3, 0 });
    CameraPOVs.put(CameraState.Observe, new float[]{ 0, 5, -6,  0, 3, 0 });
  }
  private static final  Matrix4       tmpMat4       = new Matrix4();
  private static final  String        LogTag        = "LobbyScreen";
  
  private static final  Vector3       tmpVec        = new Vector3();
  private static final  Quaternion    tmpRot        = new Quaternion();
  
  // Game instance cache.
  private final Communitrix           ctx;

  // State related members.
  private       State                 state         = State.Unknown;
  private       CameraState           cameraState   = CameraState.Unknown;
  // Shared screen data object.
  private       SCData                data          = new SCData();

  // Flat UI.
  private       UILobby               ui            = new UILobby(data);
  // Scene setup related objects.
  private final Environment           envMain;
  private final Camera                camMain, camTarget;
  private final ICLobby               inputCtrl;
  private final PostProcessor         postProMain;
  // Object which need to be rendered with the camMain.
  private final Array<GameObject>     instances     = new Array<GameObject>();
  // Model instances.
  private final Piece                 target;
  private final Array<SHUnit>         units;
  private final Piece                 unit;
  private final Array<Piece>          pieces;
  private final Array<Piece>          availablePieces;
  private final PiecesDock            piecesDock;

  private static        Vector3       relXAxis, relYAxis, relZAxis;
  static {
    relZAxis  = new Vector3(Vector3.Z).scl(-1);
    relXAxis  = new Vector3(relZAxis).crs(Vector3.Y);
    relYAxis  = new Vector3(relXAxis).crs(relZAxis);
  };

  public SCLobby (final Communitrix communitrix) {
    Gdx.app.log           (LogTag, "Constructing.");
    // Cache our game instance.
    ctx                   = communitrix;
    ctx.setErrorResponder (ui);

    // Set up the scene environment.
    envMain               = new Environment();
    envMain.set           (new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1.0f));
    envMain.set           (new ColorAttribute(ColorAttribute.Fog, 0.01f, 0.01f, 0.01f, 1.0f));
    envMain.add           (new DirectionalLight().set(new Color(0.6f, 0.6f, 0.6f, 1.0f), -1f, -0.8f, -0.2f));
    
    // Set up the main post-processor.
    postProMain           = new PostProcessor(true, true, true);
    
    boolean enablePostPro = false;
    if (enablePostPro && ctx.applicationType!=ApplicationType.WebGL) {
      // Add bloom to post-processor.
      Bloom blm             = new Bloom(ctx.viewWidth/4, ctx.viewHeight/4);
      blm.setEnabled        (true);
      blm.setBloomIntensity (0.60f);
      blm.setBloomSaturation(0.90f);
      postProMain.addEffect (blm);
      // Add motion blur to post-processor.
      MotionBlur blur       = new MotionBlur();
      blur.setEnabled       (true);
      blur.setBlurOpacity   (0.60f);
      postProMain.addEffect (blur);
    }

    // Set up our target camera.
    camTarget             = new Camera(55, ctx.viewWidth/2.5f, ctx.viewHeight/2.5f);
    camTarget.position.set(0, 2, -6);
    camTarget.lookAt      (0, 0, 0);
    camTarget.near        = 1f;
    camTarget.far         = 32f;
    camTarget.update      ();
    
    // Set up our scene.
    camMain               = new Camera(70, ctx.viewWidth, ctx.viewHeight);
    camMain.near          = 1f;
    camMain.far           = 64f;
    setCameraState        (CameraState.Lobby, false);
    
    // Create various arrays..
    units                 = new Array<SHUnit>();
    pieces                = new Array<Piece>();
    availablePieces       = new Array<Piece>();

    // Create objective piece. It is not added to the instances list, as it is rendered on the side.
    target                = new Piece();
    // Create unit piece.
    unit                  = new Piece();
    unit.transform
      .translate          (0, 3, 0);
    unit.reset            ();
    instances.add         (unit);
    // Pieces dock.
    piecesDock            = new PiecesDock(this);
    instances.add         (piecesDock);
    
    // Instantiate our interaction controller.
    inputCtrl        = new ICLobby(this);
  }
  // Setter on state, handles transitions.
  public void setState (final State newState) {
    switch (state = newState) {
      case Settings:
        Gdx.input
          .setInputProcessor  (ui);
        break;
      case Global:
        target.clear          ();
        resetRotation         (target);
        unit.clear            ();
        resetRotation         (unit);
        units.clear           ();
        Gdx.input
          .setInputProcessor  (ui.getStage());
        break;
      case Joined:
        Gdx.input
          .setInputProcessor  (inputCtrl);
        setCameraState        (CameraState.Lobby, true);
        break;
      case Gaming:
        setCameraState        (CameraState.Pieces, true);
        break;
      case EndGame:
        pieces.clear          ();
        availablePieces.clear ();
        piecesDock.refresh    ();
        data.playedPiece      = null;
        data.selectedPiece    = null;
        break;
      default :
        break;
    }
    ui.setState  (state);
  }
  
  public SCLobby setCombats (final SHCombat[] newCombats) {
    data.combats.clear  ();
    data.combats.addAll (newCombats);
    ui.updateCombats    ();
    return              this;
  }
  public SCLobby setCombat (final SHCombat newCombat) {
    data.combat  = newCombat;
    for (final SHCombat c : data.combats)
      if (data.combat.uuid.equals(c.uuid)) {
        data.combat   = c.set(newCombat);
        break;
      }
    return      this;
  }

  public SCLobby addPlayer (final SHPlayer player) {
    Gdx.app.log             (LogTag, "Adding player (" + player.uuid + ").");
    data.combat.players.add (player);
    return this;
  }
  public SCLobby removePlayer (final String uuid) {
    Gdx.app.log       (LogTag, "Removing player (" + uuid + ").");
    for (final SHPlayer player : data.combat.players)
      if (player.uuid.equals(uuid)) {
        data.combat.players.remove(player);
        break;
      }
    return this;
  }
  
  public boolean isFirstTurn () {
    return data.combat.currentTurn==1;
  }
  public boolean isLastTurn () {
    return data.combat.currentTurn>pieces.size;
  }
  public void setCameraState (final CameraState cameraState, final boolean animate) {
    final float[] pov     = CameraPOVs.get(cameraState);
    this.cameraState      = cameraState;
    if (animate) {
      Tween
        .to             (camMain, CameraAccessor.Trans, 0.3f)
        .target         (pov)
        .ease           (Quad.INOUT)
        .start          (ctx.tweener);
    }
    else {
      camMain.position.set  (pov[0], pov[1], pov[2]);
      camMain.target.set    (pov[3], pov[4], pov[5]);
      camMain.update        ();
    }
  }
  public Array<Piece> getAvailablePieces() { return availablePieces; }
  public void selectPiece (final Piece piece) {
    if (data.selectedPiece==piece)
      return;
      // De-select old selection.
    else if (data.selectedPiece!=null) {
      // Scale back up.
      data.selectedPiece.targetScale
        .set        (1, 1, 1);
      // Re-parent the piece to the pieces dock.
      data.selectedPiece
        .setParent  (piecesDock);
      // As the pieces dock itself isn't rotating, pre-rotate the piece.
      data.selectedPiece.targetRotation
        .set        (unit.targetRotation);
      data.selectedPiece
        .switchToRegularMaterials();
      piecesDock
        .refresh      ();
    }
    
    // Nothing selected, just return.
    if ((data.selectedPiece = piece)==null)
      return;
    
    // Compute status of selected piece.
    final SHUnit  sharedUnit  = (SHUnit)unit.sharedPiece;
    if (piece==null)        return;
    final int     pieceId   = pieces.indexOf(piece, true);
    int           count     = 0;
    if (sharedUnit!=null && sharedUnit.moves!=null) {
      for (final int[] ids : sharedUnit.moves.values())
        for (final int id : ids)
          if (pieceId==id)
            count++;
    }
    // Build message.
    if (count==0)     ctx.setLastError(0, "This piece has not been played in this unit yet.");
    else              ctx.setLastError(-1, String.format("This piece has been played %d times in the current unit.", count));

    // To prevent face clipping, let's scale down a little bit.
    data.selectedPiece.targetScale
      .set        (0.97f, 0.97f, 0.97f);
    // Re-parent the piece inside the unit.
    data.selectedPiece
      .setParent  (unit);
    // The piece needs to be at the unit's origin.
    data.selectedPiece.targetPosition
      .set        (0, 0, 0);
    checkCollisionsWithUnit(data.selectedPiece);
    data.selectedPiece
      .start      (ctx.tweener, 0.3f, Quad.INOUT);
  }
  public void translateWithinView (final GameObject obj, final Vector3 translation, final boolean checkCollisions) {
    tmpVec
      .set            (translation);
    if (obj.parent!=null) {
      tmpRot
        .set          (obj.parent.currentRotation)
        .conjugate    ()
        .transform    (tmpVec);
    }
    obj.targetPosition
      .add            (tmpVec);
    obj
      .start          (ctx.tweener, 0.3f, Quad.INOUT);

    if (checkCollisions)
      checkCollisionsWithUnit((FacetedObject)obj);
  }
  public void rotateWithinView (final GameObject obj, final Vector3 axis, final int angle, final boolean checkCollisions) {
    tmpMat4.idt       ();
    if (obj.parent!=null)
      tmpMat4.rotate  (obj.parent.targetRotation.cpy().conjugate());
    // Asked to rotate around X.
    if (axis==Vector3.X)      tmpMat4.rotate(relXAxis, angle);
    // Asked to rotate around Y.
    else if (axis==Vector3.Y) tmpMat4.rotate(relYAxis, angle);
    if (obj.parent!=null)
      tmpMat4.rotate  (obj.parent.targetRotation);
    tmpMat4
      .rotate         (obj.targetRotation)
      .getRotation    (obj.targetRotation);
    obj
      .start          (ctx.tweener, 0.3f, Quad.INOUT);
    
    if (checkCollisions)
      checkCollisionsWithUnit((FacetedObject)obj);
  }
  public void checkCollisionsWithUnit (final FacetedObject obj) {
    final boolean     collides    = obj.collidesWith(unit);
    if (collides==data.isColliding)
      return;
    data.isColliding    = collides;
    if (collides)       obj.switchToCollisionMaterials();
    else                obj.switchToRegularMaterials();
  }
  public void resetRotation (final GameObject obj) {
    obj.targetRotation
      .idt            ();
    obj
      .start          (ctx.tweener, 0.3f, Quad.INOUT);
  }
  public void playPiece (final Piece piece) {
    // There already is a played piece for this turn...
    if (data.playedPiece!=null) {
      selectPiece         (null);
      ctx.setLastError    (0, "You already have played this turn, please wait.");
      return;
    }
    // Get the index of the piece to be played.
    final int idx         = pieces.indexOf(piece, true);
    // Store the piece that is being played so we can rollback / validate upon server ack.
    data.playedPiece      = piece;
    // Make an empty selection, but don't reset / animate the piece.
    data.selectedPiece    = null;
    // Give the order!
    ctx.networkingManager
      .send               (new TXCombatPlayTurn(idx, piece.targetPosition, piece.targetRotation));
  }
  public void handleAcknowledgment (final String serial, final boolean valid, final String errorMessage) {
    if (serial.equals("PlayTurn")) {
      if (valid)
        availablePieces
          .removeValue    (data.playedPiece, true);
      else {
        ctx.setLastError  (600, errorMessage);
        data.selectedPiece  = data.playedPiece;
        data.playedPiece    = null;
        selectPiece         (null);
      }
      piecesDock.refresh  ();
    }
  }
  
  public SCLobby prepare (final SHPiece target, final SHUnit[] newUnits, final SHPiece[] newPieces) {
    this.target
      .setFromSharedPiece       (target);
    // Place all my pieces.
    if (newPieces!=null) {
      for (int i=0; i<newPieces.length; i++) {
        final Piece   obj       = new Piece();
        obj.transform
          .setTranslation       (0, 0, -5);
        obj.reset               ();
        obj.setFromSharedPiece  (newPieces[i]);
        pieces.add              (obj);
        piecesDock.addChild     (obj);
      }
      availablePieces.addAll    (pieces);
      units.addAll              (newUnits);
      piecesDock.refresh        ();
    }
    return this;
  }
  public SCLobby setTurn (final int turn, final int unitId) {
    // Update our combat object.
    data.combat.currentTurn     = turn;
    // If a piece was played this turn, remove it from the scene.
    if (data.playedPiece!=null) {
      // Unparent the played piece from the unit.
      data.playedPiece.unparent      ();
      // The played unit should not be visible anymore.
      instances.removeValue     (data.playedPiece, true);
      data.playedPiece          = null;
    }
    final SHUnit currentUnit = units.get(unitId);
    if (currentUnit.size.volume()!=0)
      unit.setFromSharedPiece (currentUnit);
    return this;
  }
  public void registerPlayerTurn (final String playerUUID, final int unitId, final SHUnit newUnit) {
    units
      .get(unitId)
      .set(newUnit);
  }
  public SCLobby endGame () {
    // TODO: Get endgame data as parameters and display them.
    return this;
  }
  
  public Piece getClickableAt (final int screenX, final int screenY) {
    if (data.playedPiece!=null) {
      ctx
        .setLastError       (0, "Please wait for all players to complete this turn.");
      return                null;
    }
    else if (state!=State.Gaming || cameraState!=CameraState.Pieces)
      return                null;
    else
      return                rayPickTest(screenX, screenY, availablePieces);
  }
  public boolean handleSelection (final Piece clicked) {
    if (data.playedPiece!=null) {
      Communitrix.getInstance()
        .setLastError (0, "Please wait for all players to complete this turn.");
      return false;
    }
    else if (state!=State.Gaming || cameraState!=CameraState.Pieces)
      return          false;
    else if (data.selectedPiece==clicked)
      return          false;
    // We had a previous selection.
    selectPiece       (clicked);
    setCameraState    (CameraState.Unit, true);
    return            true;
  }
  
  private <T extends GameObject> T rayPickTest (final int screenX, final int screenY, final Array<T> list) {
    // Reset selection.
    T           result     = null;
    // Pick a ray from the cam.
    final Ray   ray        = camMain.getPickRay(screenX, screenY);
    // Variable dist will be a temp, while sDist will be the shortest found distance.
    float       dist, sDist = Float.MAX_VALUE;
    BoundingBox bounds      = null;
    // Iterate through all instances.
    for (T obj : list) {
      tmpVec.set        (obj.targetPosition);
      if (( dist = ray.origin.dst2(tmpVec) )<sDist) {
        bounds          = new BoundingBox(obj.fakeBounds);
        bounds.min.add  (tmpVec);
        bounds.max.add  (tmpVec);
        if (Intersector.intersectRayBounds(ray, bounds, null)) {
          sDist         = dist;
          result       = obj;
        }
      }
    }
    return result;
  }

  
  public boolean handleZoom (final int amount) {
    if (state!=State.Gaming)
      return false;
    camMain
    .translate(
      tmpVec
        .set(camMain.direction)
        .scl(amount)
    );
    camMain.update();
    return true;
  }
  
  @Override public void show ()   {}
  @Override public void hide ()   {}
  @Override public void pause ()  {}
  @Override public void resume () {}
  
  @Override public void dispose () {
    postProMain.dispose     ();
  }
  
  @Override public void render (final float delta) {
    // Clear viewport etc.
    Gdx.gl.glClear        (GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    // Enable alpha blending.
    Gdx.gl.glEnable       (GL20.GL_BLEND);
    Gdx.gl.glBlendFunc    (GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    // Enable back-face culling.
    Gdx.gl.glEnable       (GL20.GL_CULL_FACE);
    Gdx.gl.glCullFace     (GL20.GL_FRONT);
    
    // Update any pending animation.
    ctx.tweener.update    (delta);
    inputCtrl.updateKeys  ();
    // We're in ALT mode.
    if (inputCtrl.alt) {
      if (state==State.Gaming) {
        handleMovement (target, inputCtrl.keys, true, false, false);
        handleMovement (unit, inputCtrl.keys, true, false, false);
        for (final Piece piece : availablePieces)
          if (piece.parent!=unit)
            handleMovement  (piece, inputCtrl.keys, true, false, false);
      }
    }
    // We're NOT in alt mode.
    else {
      // Player is willing to go back.
      if (inputCtrl.keys[Key.Cancel.ordinal()]) {
        if (state==State.Global)
          setState        (State.Settings);
        else if (state==State.Settings)
          setState        (State.Global);
        else if (state==State.EndGame)
          setState        (State.Global);
        else {
          setCameraState  (CameraState.Pieces, true);
          selectPiece     (null);
        }
      }
      // We're in game mode.
      if (state==State.Gaming) {
        // Player is asking to reset target and unit.
        if (inputCtrl.keys[Key.Reset.ordinal()]) {
          resetRotation       (target);
          resetRotation       (unit);
          for (final Piece piece : availablePieces)
            resetRotation     (piece);
        }
        // We're in unit mode...
        if (cameraState==CameraState.Unit) {
          // There is a selection.
          if (data.selectedPiece!=null) {
            // Only allow piece translation if not within first turn.
            handleMovement                      (data.selectedPiece, inputCtrl.keys, true, !isFirstTurn(), true);
            // Send turn to server.
            if (inputCtrl.keys[Key.Validate.ordinal()]) {
              playPiece                         (data.selectedPiece);
              setCameraState                    (CameraState.Pieces, true);
            }
          }
        }
        // There is no selection made.
        else if (cameraState==CameraState.Pieces) {
          if (inputCtrl.keys[Key.MoveLeft.ordinal()])        piecesDock.cycle  (-1);
          else if (inputCtrl.keys[Key.MoveRight.ordinal()])  piecesDock.cycle  (1);
        }
        // Toggle between unit view and target view.
        if (inputCtrl.keys[Key.CycleView.ordinal()])
          if ((state==State.Gaming))
            setCameraState(cameraState==CameraState.Pieces ? CameraState.Unit : CameraState.Pieces, true);
      }
    }
    
    // Capture FBO for post-processing.
    postProMain.capture   ();
    
    // Mark the beginning of our rendering phase.
    Gdx.gl.glViewport     (0, 0, ctx.viewWidth, ctx.viewHeight);
    ctx.modelBatch.begin  (camMain);
    // Render all instances in our batch array.
    GameObject.renderCam  = camMain;
    for (final GameObject instance : instances)
      ctx.modelBatch.render(instance, envMain);
    // Rendering is over.
    ctx.modelBatch.end    ();

    Gdx.gl.glViewport     (0, (int)(ctx.viewHeight-(ctx.viewHeight/2.5f)), (int)(ctx.viewWidth/2.5f), (int)(ctx.viewHeight/2.5f));
    GameObject.renderCam  = camTarget;
    ctx.modelBatch.begin  (camTarget);
    ctx.modelBatch.render (target, envMain);
    ctx.modelBatch.end    ();

    Gdx.gl.glViewport     (0, 0, ctx.viewWidth, ctx.viewHeight);
    // Apply post-processing.
    postProMain.render    ();

    // Update flat UI.
    ui.actAndDraw         (delta);
  }

  // Checks whether the user is to translating / rotating.
  public void handleMovement (final Piece moveable, final boolean[] keys, final boolean rotate, final boolean translate, final boolean checkCollisions) {
    if (translate) {
      if (keys[Key.MoveForward.ordinal()])        translateWithinView (moveable, Constants.Directions.get(Key.MoveForward),   checkCollisions);
      else if (keys[Key.MoveBackward.ordinal()])  translateWithinView (moveable, Constants.Directions.get(Key.MoveBackward),  checkCollisions);
      if (keys[Key.MoveLeft.ordinal()])           translateWithinView (moveable, Constants.Directions.get(Key.MoveLeft),      checkCollisions);
      else if (keys[Key.MoveRight.ordinal()])     translateWithinView (moveable, Constants.Directions.get(Key.MoveRight),     checkCollisions);
      if (keys[Key.MoveUp.ordinal()])             translateWithinView (moveable, Constants.Directions.get(Key.MoveUp),        checkCollisions);
      else if (keys[Key.MoveDown.ordinal()])      translateWithinView (moveable, Constants.Directions.get(Key.MoveDown),      checkCollisions);
    }
    if (rotate) {
      if (keys[Key.RotateUp.ordinal()])           rotateWithinView    (moveable, Vector3.X,  90, checkCollisions);
      else if (keys[Key.RotateDown.ordinal()])    rotateWithinView    (moveable, Vector3.X, -90, checkCollisions);
      if (keys[Key.RotateLeft.ordinal()])         rotateWithinView    (moveable, Vector3.Y, -90, checkCollisions);
      else if (keys[Key.RotateRight.ordinal()])   rotateWithinView    (moveable, Vector3.Y,  90, checkCollisions);
    }
  }

  
  @Override public void resize (final int width, final int height) {
    // Update flat UI.
    ui.resize           (width, height);
    // If a post-processor exists, update it.
    postProMain.rebind  ();
  }
}
