package fr.pierreqr.communitrix.screens;

import java.util.EnumMap;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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
import fr.pierreqr.communitrix.Constants;
import fr.pierreqr.communitrix.Constants.Key;
import fr.pierreqr.communitrix.gameObjects.Camera;
import fr.pierreqr.communitrix.gameObjects.FacetedObject;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.Piece;
import fr.pierreqr.communitrix.networking.cmd.beans.CombatBean;
import fr.pierreqr.communitrix.networking.cmd.beans.PlayerBean;
import fr.pierreqr.communitrix.networking.cmd.beans.UnitBean;
import fr.pierreqr.communitrix.networking.cmd.rx.*;
import fr.pierreqr.communitrix.networking.cmd.tx.*;
import fr.pierreqr.communitrix.screens.util.PiecesDock;
import fr.pierreqr.communitrix.screens.util.PiecesDock.PiecesDockDelegate;
import fr.pierreqr.communitrix.tweeners.CameraAccessor;

public class MainScreen extends BaseScreen implements PiecesDockDelegate {
  public                enum          State         { Settings, Global, Joined, Gaming, EndGame }
  public                enum          CameraState   { Lobby, Pieces, Unit, Observe }
  
  // Possible POV / Targets for camera.
  public final static EnumMap<CameraState, float[]> CameraPOVs  = new EnumMap<CameraState, float[]>(CameraState.class); static {
    CameraPOVs.put(CameraState.Lobby,   new float[]{ 0, 2, -6,  0, 0, 0 });
    CameraPOVs.put(CameraState.Pieces,  new float[]{ 0, 4,-10,  0, 0, 0 });
    CameraPOVs.put(CameraState.Unit,    new float[]{ 0, 5, -6,  0, 3, 0 });
    CameraPOVs.put(CameraState.Observe, new float[]{ 0, 5, -6,  0, 3, 0 });
  };
  
  private static final  String        LogTag        = "LobbyScreen";
  private static final  Vector3       tmpVec        = new Vector3();
  private static final  Quaternion    tmpRot        = new Quaternion();
  private static final  Matrix4       tmpMat4       = new Matrix4();
  
  private static final  Vector3       relXAxis, relYAxis, relZAxis; static {
    relZAxis  = new Vector3(Vector3.Z).scl(-1);
    relXAxis  = new Vector3(relZAxis).crs(Vector3.Y);
    relYAxis  = new Vector3(relXAxis).crs(relZAxis);
  };
  
  // State related members.
  private       State                 state         = null;
  private       CameraState           cameraState   = CameraState.Lobby;
  // Flat UI.
  private final GameScreenOverlay     ui;
  // Scene setup related objects.
  private final Environment           envMain;
  private final Camera                camMain, camTarget;
  private final PostProcessor         postProMain;
  // Model instances.
  private final Array<UnitBean>       units;
  private final Array<Piece>          pieces, availablePieces;
  private final Piece                 target, unit;
  private final PiecesDock            piecesDock;
  
  // The last loaded list of combats.
  public final  Array<CombatBean>     combats;
  // The current combat we've joined.
  public        CombatBean            combat;
  
  // The piece being played / that was played this turn.
  public        Piece                 playedPiece;
  // The currently selected piece and informations about it.
  public        Piece                 selectedPiece;
  public        boolean               isColliding;


  public MainScreen () {
    super                 ();
    
    cameraState           = CameraState.Lobby;
    ui                    = new GameScreenOverlay(this);
    
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
    camTarget             = new Camera(70, ctx.viewWidth/2.5f, ctx.viewHeight/2.5f);
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
    combats               = new Array<CombatBean>();
    units                 = new Array<UnitBean>();
    pieces                = new Array<Piece>();
    availablePieces       = new Array<Piece>();
    
    // Create objective piece. It is not added to the instances list, as it is rendered on the side.
    target                = new Piece();
    // Create unit piece.
    unit                  = new Piece();
    unit.transform
      .translate          (0, 3, 0);
    unit.reset            ();
    // Pieces dock.
    piecesDock            = new PiecesDock(this, pieces, availablePieces);
    
    setState              (State.Global);
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
          .setInputProcessor  (ui.stage);
        break;
      
      case Joined:
        Gdx.input
          .setInputProcessor  (this);
        setCameraState        (CameraState.Lobby, true);
        break;
      
      case Gaming:
        setCameraState        (CameraState.Pieces, true);
        break;
      
      case EndGame:
        pieces.clear          ();
        availablePieces.clear ();
        piecesDock.refresh    ();
        playedPiece           = null;
        selectedPiece         = null;
        break;
      
      default :
        break;
    }
    ui.setState  (state);
  }
  
  public        boolean     alt   = false;
  public final  boolean[]   keys  = new boolean[Key.Count];
  @Override public boolean keyDown (final int keycode) { return false; }
  @Override public boolean keyUp (final int keycode) { return false; }
  @Override public boolean keyTyped (final char character)  { return false; }
  @Override public boolean touchDown (final int x, final int y, final int ptr, final int btn) {
    return handleSelection(getClickableAt(x, y));
  }
  @Override public boolean touchUp (final int x, final int y, final int ptr, final int btn) { return false; }
  @Override public boolean touchDragged (final int x, final int y, final int ptr) { return false; }
  @Override public boolean mouseMoved (int x, int y) { return false; }
  @Override public boolean scrolled (int amount) {
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
  
  @Override public void showMessage (final MessageType type, final String message) {
    ui.showMessage  (type, message);
  }
  
  @Override  public void onServerMessage (final RXBase baseCmd) {
    switch (baseCmd.type) {
      // We received an ack.
      case Acknowledgment: {
        final RXAcknowledgment    cmd = (RXAcknowledgment)baseCmd;
        if (cmd.serial.equals("PlayTurn")) {
          if (cmd.valid)
            availablePieces
              .removeValue    (playedPiece, true);
          else {
            ctx.showMessage   (MessageType.Error, cmd.errorMessage);
            selectedPiece       = playedPiece;
            playedPiece         = null;
            selectPiece         (null);
          }
          piecesDock.refresh  ();
        }
        break;
      }
      // We received a list of combats.
      case CombatList: {
        final RXCombatList        cmd = (RXCombatList)baseCmd;
        combats.clear                 ();
        combats.addAll                (cmd.combats);
        ui.updateCombatList           ();
        break;
      }
      // We received an order to join a combat.
      case CombatJoin: {
        final RXCombatJoin        cmd = (RXCombatJoin)baseCmd;
        combat                        = cmd.combat;
        for (final CombatBean c : combats)
          if (combat.uuid.equals(c.uuid)) {
            combat                    = c.set(cmd.combat);
            break;
          }
        setState                      (State.Joined);
        break;
      }
      // We received a player info who just joined.
      case CombatPlayerJoined: {
        final RXCombatPlayerJoined cmd = (RXCombatPlayerJoined)baseCmd;
        Gdx.app.log                   (LogTag, "Adding player (" + cmd.player.uuid + ").");
        combat.players.add            (cmd.player);
        break;
      }
      // A player has just left the combat.
      case CombatPlayerLeft: {
        final RXCombatPlayerLeft  cmd = (RXCombatPlayerLeft)baseCmd;
        Gdx.app.log                   (LogTag, "Removing player (" + cmd.uuid + ").");
        for (final PlayerBean player : combat.players)
          if (player.uuid.equals(cmd.uuid)) {
            combat.players.remove     (player);
            break;
          }
        break;
      }
      // We should move on to the main game state.
      case CombatStart: {
        final RXCombatStart       cmd = (RXCombatStart)baseCmd;
        this.target
          .setFromSharedPiece       (cmd.target);
        // Place all my pieces.
        if (cmd.pieces!=null) {
          for (int i=0; i<cmd.pieces.length; i++) {
            final Piece   obj       = new Piece();
            obj.transform
              .setTranslation       (0, 0, -5);
            obj.reset               ();
            obj.setFromSharedPiece  (cmd.pieces[i]);
            pieces.add              (obj);
            piecesDock.addChild     (obj);
          }
          availablePieces.addAll    (pieces);
          units.addAll              (cmd.units);
          piecesDock.refresh        ();
        }
        
        setState                      (MainScreen.State.Gaming);
        break;
      }
      // A new turn is being initiated.
      case CombatNewTurn: {
        final RXCombatNewTurn     cmd = (RXCombatNewTurn)baseCmd;
        // Update our combat object.
        combat.currentTurn            = cmd.turnId;
        // If a piece was played this turn, remove it from the scene.
        if (playedPiece!=null) {
          // Unparent the played piece from the unit.
          playedPiece.unparent        ();
          // The played unit should not be visible anymore.
          playedPiece                 = null;
        }
        final UnitBean currentUnit = units.get(cmd.unitId);
        if (currentUnit.size.volume()!=0)
          unit.setFromSharedPiece (currentUnit);
        
        setState                      (MainScreen.State.Gaming);
        break;
      }
      // Another player has played his turn.
      case CombatPlayerTurn: {
        final RXCombatPlayerTurn  cmd = (RXCombatPlayerTurn)baseCmd;
        units.get(cmd.unitId).set     (cmd.unit);
         break;
      }
      // That's the end of the game.
      case CombatEnd: {
        // TODO: Get end-game data as parameters and display them.
        setState                      (MainScreen.State.EndGame);
        break;
      }
      default:
    }
    return;
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
  
  public void selectPiece (final Piece piece) {
    if (selectedPiece==piece)
      return;
      // De-select old selection.
    else if (selectedPiece!=null) {
      // Scale back up.
      selectedPiece.targetScale
        .set        (1, 1, 1);
      // Re-parent the piece to the pieces dock.
      selectedPiece
        .setParent  (piecesDock);
      // As the pieces dock itself isn't rotating, pre-rotate the piece.
      selectedPiece.targetRotation
        .set        (unit.targetRotation);
      selectedPiece
        .switchToRegularMaterials();
      piecesDock
        .refresh      ();
    }
    
    // Nothing selected, just return.
    if ((selectedPiece = piece)==null)
      return;
    
    // Compute status of selected piece.
    final UnitBean  sharedUnit  = (UnitBean)unit.sharedPiece;
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
    if (count==0)     ctx.showMessage (MessageType.Success, "This piece has not been played in this unit yet.");
    else              ctx.showMessage (MessageType.Warning, String.format("This piece has been played %d times in the current unit.", count));

    // To prevent face clipping, let's scale down a little bit.
    selectedPiece.targetScale
      .set        (0.97f, 0.97f, 0.97f);
    // Re-parent the piece inside the unit.
    selectedPiece
      .setParent  (unit);
    // The piece needs to be at the unit's origin.
    selectedPiece.targetPosition
      .set        (0, 0, 0);
    checkCollisionsWithUnit(selectedPiece);
    selectedPiece
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
    if (collides==isColliding)
      return;
    isColliding    = collides;
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
    if (playedPiece!=null) {
      selectPiece         (null);
      ctx.showMessage     (MessageType.Warning, "You already have played this turn, please wait.");
      return;
    }
    // Get the index of the piece to be played.
    final int idx         = pieces.indexOf(piece, true);
    // Store the piece that is being played so we can rollback / validate upon server ack.
    playedPiece           = piece;
    // Make an empty selection, but don't reset / animate the piece.
    selectedPiece         = null;
    // Give the order!
    ctx.net
      .send               (new TXCombatPlayTurn(idx, piece.targetPosition, piece.targetRotation));
  }

  public Piece getClickableAt (final int x, final int y) {
    if (playedPiece!=null) {
      ctx.showMessage       (MessageType.Warning, "Please wait for all players to complete this turn.");
      return                null;
    }
    else if (state!=State.Gaming || cameraState!=CameraState.Pieces)
      return                null;
    else
      return                rayPickTest(x, y, availablePieces);
  }
  public boolean handleSelection (final Piece clicked) {
    if (playedPiece!=null) {
      ctx.showMessage (MessageType.Warning, "Please wait for all players to complete this turn.");
      return false;
    }
    else if (state!=State.Gaming || cameraState!=CameraState.Pieces)
      return          false;
    else if (selectedPiece==clicked)
      return          false;
    // We had a previous selection.
    selectPiece       (clicked);
    setCameraState    (CameraState.Unit, true);
    return            true;
  }
  
  private <T extends GameObject> T rayPickTest (final int x, final int y, final Array<T> list) {
    // Reset selection.
    T           result     = null;
    // Pick a ray from the cam.
    final Ray   ray        = camMain.getPickRay(x, y);
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
  
  @Override public void show ()   {}
  @Override public void hide ()   {}
  @Override public void pause ()  {}
  @Override public void resume () {}
  
  @Override public void dispose () {
    postProMain.dispose     ();
  }
  
  @Override public void render (final float delta) {
    alt = Gdx.input.isKeyPressed(Constants.Keys[Constants.Key.Alt]);
    for (int i=0; i<Key.Count; ++i)
      keys[i]     = Gdx.input.isKeyJustPressed(Constants.Keys[i]);
    
    // Update any pending animation.
    ctx.tweener.update    (delta);
    // We're in ALT mode.
    if (alt) {
      if (state==State.Gaming) {
        handleMovement (target, keys, true, false, false);
        handleMovement (unit, keys, true, false, false);
        for (final Piece piece : availablePieces)
          if (piece.parent!=unit)
            handleMovement  (piece, keys, true, false, false);
      }
    }
    // We're NOT in alt mode.
    else {
      // Player is willing to go back.
      if (keys[Key.Cancel]) {
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
        if (keys[Key.Reset]) {
          resetRotation       (target);
          resetRotation       (unit);
          for (final Piece piece : availablePieces)
            resetRotation     (piece);
        }
        // We're in unit mode...
        if (cameraState==CameraState.Unit) {
          // There is a selection.
          if (selectedPiece!=null) {
            // Only allow piece translation if not within first turn.
            handleMovement                      (selectedPiece, keys, true, combat.currentTurn>1, true);
            // Send turn to server.
            if (keys[Key.Validate]) {
              playPiece                         (selectedPiece);
              setCameraState                    (CameraState.Pieces, true);
            }
          }
        }
        // There is no selection made.
        else if (cameraState==CameraState.Pieces) {
          if (keys[Key.MoveLeft])        piecesDock.cycle  (-1);
          else if (keys[Key.MoveRight])  piecesDock.cycle  (1);
        }
        // Toggle between unit view and target view.
        if (keys[Key.CycleView])
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
    ctx.modelBatch.render(unit, envMain);
    ctx.modelBatch.render(piecesDock, envMain);
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
      if (keys[Key.MoveForward])        translateWithinView (moveable, Constants.Directions.get(Key.MoveForward),   checkCollisions);
      else if (keys[Key.MoveBackward])  translateWithinView (moveable, Constants.Directions.get(Key.MoveBackward),  checkCollisions);
      if (keys[Key.MoveLeft])           translateWithinView (moveable, Constants.Directions.get(Key.MoveLeft),      checkCollisions);
      else if (keys[Key.MoveRight])     translateWithinView (moveable, Constants.Directions.get(Key.MoveRight),     checkCollisions);
      if (keys[Key.MoveUp])             translateWithinView (moveable, Constants.Directions.get(Key.MoveUp),        checkCollisions);
      else if (keys[Key.MoveDown])      translateWithinView (moveable, Constants.Directions.get(Key.MoveDown),      checkCollisions);
    }
    if (rotate) {
      if (keys[Key.RotateUp])           rotateWithinView    (moveable, Vector3.X,  90, checkCollisions);
      else if (keys[Key.RotateDown])    rotateWithinView    (moveable, Vector3.X, -90, checkCollisions);
      if (keys[Key.RotateLeft])         rotateWithinView    (moveable, Vector3.Y, -90, checkCollisions);
      else if (keys[Key.RotateRight])   rotateWithinView    (moveable, Vector3.Y,  90, checkCollisions);
    }
  }

  
  @Override public void resize (final int w, final int h) {
    ui.resize           (w, h);
    postProMain.rebind  ();
  }
}
