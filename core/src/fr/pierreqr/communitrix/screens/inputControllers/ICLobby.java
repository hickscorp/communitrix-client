package fr.pierreqr.communitrix.screens.inputControllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.Piece;
import fr.pierreqr.communitrix.screens.SCLobby.CameraState;

public class ICLobby extends InputAdapter {
  public interface ICLobbyDelegate {
    int           getTurn         ();
    Camera        getCamera       ();
    CameraState   getCameraState  ();
    void          setCameraState  (final CameraState state);
    Piece         getUnit         ();
    Piece         getTarget       ();
    Array<Piece>  getPieces       ();
    Array<Piece>  getClickables   ();
    void          cyclePieces     (final int pieceIndex);
    void          selectPiece     (final Piece piece);
    void          translatePiece  (final Piece piece, final Vector3 axis);
    void          rotatePiece     (final Piece piece, final Vector3 axis, final int angle);
    void          playPiece       (final Piece piece);
  };
  
  private final         ICLobbyDelegate     delegate;
  private               Piece               selection         = null;
  private               int                 firstPieceIndex   = 0;
  private final         Vector3             position          = new Vector3();
  
  public ICLobby (final ICLobbyDelegate delegate) {
    this.delegate   = delegate;
  }
  
  @Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
    // Reset selection.
    Piece       clicked     = null;
    // Pick a ray from the cam.
    final Ray   ray         = delegate.getCamera().getPickRay(screenX, screenY);
    // Dist will be a temp, while sDist will be the shortest found distance.
    float       dist, sDist = Float.MAX_VALUE;
    BoundingBox bounds      = null;
    // Iterate through all instances.
    final Array<Piece> all  = delegate.getClickables();
    for (final Piece obj : all) {
      obj.transform.getTranslation (position);
      dist              = ray.origin.dst2(position);
      //if (dist<sDist && Intersector.intersectRaySphere(ray, position, obj.radius, null)) {
      if (dist<sDist) {
        bounds          = new BoundingBox(obj.bounds);
        bounds.min.add  (position);
        bounds.max.add  (position);
        if (Intersector.intersectRayBounds(ray, bounds, null)) {
          sDist         = dist;
          clicked       = obj;
          position
            .set        (ray.direction)
            .scl        (-ray.origin.y / ray.direction.y)
            .add        (ray.origin);
        }
      }
    }

    // Cache some variables.
    final Piece         target    = delegate.getTarget();
    final Piece         unit      = delegate.getUnit();
    // Player clicked on the target unit.
    if (clicked==target)
      delegate.setCameraState   (CameraState.Target);
    // Player clicked on the working unit block, or on a piece.
    else if (clicked!=null) {
      if (clicked==unit) {
        selection                 = null;
      } else {
        delegate.selectPiece      (clicked);
        selection                 = clicked;
      }
      delegate.setCameraState   (CameraState.Unit);
    }
    return clicked!=null;
  }
  
  public void update () {
    // Cache some variables.
    final CameraState   camState  = delegate.getCameraState();
    
    // Toggle between current view and target view.
    if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
      if (camState==CameraState.Target)
        delegate.setCameraState (selection==null ? CameraState.Pieces : CameraState.Unit);
      else
        delegate.setCameraState (CameraState.Target);
    }

    // There is no selection made.
    else if (camState==CameraState.Pieces) {
      // Cache some members.
      final Array<Piece>  pieces    = delegate.getPieces();
      // Cycle pieces left.
      if (Gdx.input.isKeyJustPressed(Keys.A)) {
        if (++firstPieceIndex>=pieces.size)
          firstPieceIndex     = 0;
        delegate.cyclePieces  (firstPieceIndex);
      }
      // Cycle pieces right.
      else if (Gdx.input.isKeyJustPressed(Keys.D)) {
        if (--firstPieceIndex<0)
          firstPieceIndex     = pieces.size-1;
        delegate.cyclePieces  (firstPieceIndex);
      }
    }
    
    else {
      // Whenever ESCAPE is pressed...
      if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
        if (camState==CameraState.Target && selection!=null)
          delegate.setCameraState   (CameraState.Unit);
        else {
          selection                 = null;
          //delegate.cyclePieces      (firstPieceIndex);
          delegate.setCameraState   (CameraState.Pieces);
        }
        return;
      }
      
      // Choose the target to act on.
      final Piece         moveable;
      // We're in target mode, the target will rotate / move.
      if (camState==CameraState.Target)
        moveable    = delegate.getTarget();
      // We're in unit mode and either the CTRL key is pressed, or there is no selection. Act on the unit.
      else if (camState==CameraState.Unit)
        moveable    = (selection==null || Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) ? delegate.getUnit() : selection;
      else
        moveable    = selection;
      // Nothing to be manipulated.
      if (moveable==null)
        return;
      
      if (Gdx.input.isKeyJustPressed(Keys.UP))          delegate.rotatePiece    (moveable, Vector3.X,  90);
      else if (Gdx.input.isKeyJustPressed(Keys.DOWN))   delegate.rotatePiece    (moveable, Vector3.X, -90);
      if (Gdx.input.isKeyJustPressed(Keys.RIGHT))       delegate.rotatePiece    (moveable, Vector3.Y,  90);
      else if (Gdx.input.isKeyJustPressed(Keys.LEFT))   delegate.rotatePiece    (moveable, Vector3.Y, -90);
      
      if (moveable!=delegate.getTarget() && moveable!=delegate.getUnit()) {
        if (delegate.getTurn()!=1) {
          if (Gdx.input.isKeyJustPressed(Keys.W))       delegate.translatePiece (moveable, Communitrix.PositiveZ);
          else if(Gdx.input.isKeyJustPressed(Keys.S))   delegate.translatePiece (moveable, Communitrix.NegativeZ);
          if (Gdx.input.isKeyJustPressed(Keys.A))       delegate.translatePiece (moveable, Communitrix.PositiveX);
          else if (Gdx.input.isKeyJustPressed(Keys.D))  delegate.translatePiece (moveable, Communitrix.NegativeX);
          if (Gdx.input.isKeyJustPressed(Keys.O))       delegate.translatePiece (moveable, Communitrix.PositiveY);
          else if (Gdx.input.isKeyJustPressed(Keys.L))  delegate.translatePiece (moveable, Communitrix.NegativeY);
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
          delegate.playPiece      (selection);
          selection               = null;
          delegate.setCameraState (CameraState.Pieces);
//          if (!pieceLocked) {
//            pieceLocked = true;
//            if (delegate.getTurn()!=1) {
//              final Piece unit = delegate.getUnit();
//              // Adds selected piece to the node of the unit
//              unit.nodes.addAll(moveable.nodes);
//              // Calculates the quaternion to transform the unit rotation into the idt saves it to tmpQuat.
//              unit.transform.getRotation(tmpQuat).conjugate().mul(tmpQuat2.idt());
//              // Saves the selected piece translation and rotation in the world.
//              selection.transform.getRotation(tmpQuat2);
//              selection
//                .transform
//                .getTranslation(position)
//                .sub(unit.transform.getTranslation(tmpVec3));
//              // Transforms the world rotation and position of the piece to local coords in the unit.
//              unit
//                .nodes
//                .get(unit.nodes.size -1)
//                .globalTransform
//                .set(position.mul(tmpQuat), tmpQuat.mul(tmpQuat2));
//              unit.recomputeBounds();
//              delegate.cyclePieces  (firstPieceIndex);
//              selection             = unit;
//            }
//          }
//          else {
//            delegate.playPiece  (selection);
//            selection           = null;
//          }
        }
      }
    }
  }
}
