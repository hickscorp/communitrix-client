package fr.pierreqr.communitrix.screens.inputControllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.Camera;
import fr.pierreqr.communitrix.gameObjects.Piece;
import fr.pierreqr.communitrix.screens.SCLobby.CameraState;

public class ICLobby extends InputAdapter {
  public interface ICLobbyDelegate {
    int           getTurn             ();
    Camera        getCamera           ();
    CameraState   getCameraState      ();
    void          setCameraState      (final CameraState state);
    Piece         getUnit             ();
    Piece         getTarget           ();
    Array<Piece>  getPieces           ();
    Array<Piece>  getAvailablePieces  ();
    void          cyclePieces         (final int pieceIndex);
    void          selectPiece         (final Piece piece);
    void          deselectPiece       (final Piece piece);
    void          translatePiece      (final Piece piece, final Vector3 axis);
    void          rotatePiece         (final Piece piece, final Vector3 axis, final int angle);
    void          resetPieceRotation  (final Piece piece);
    void          playPiece           (final Piece piece);
  };
  
  private final         ICLobbyDelegate     delegate;
  private               Piece               selection         = null;
  private               int                 firstPieceIndex   = 0;
  private               boolean             inAltMode         = false;
  
  private final static  Vector3             tmpVec3           = new Vector3();
  private static        CameraState         camState;
  
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
    final Array<Piece> all  = delegate.getPieces();
    for (final Piece obj : all) {
      obj.transform.getTranslation (tmpVec3);
      dist              = ray.origin.dst2(tmpVec3);
      if (dist<sDist) {
        bounds          = new BoundingBox(obj.bounds);
        bounds.min.add  (tmpVec3);
        bounds.max.add  (tmpVec3);
        if (Intersector.intersectRayBounds(ray, bounds, null)) {
          sDist         = dist;
          clicked       = obj;
        }
      }
    }
    // We have a new selection.
    if (clicked!=null) {
      // Player clicked the unit.
      if (clicked==delegate.getUnit())
        delegate.setCameraState(CameraState.Unit);
      // Player clicked on the working unit block, or on a piece.
      else {
        // User clicked something different.
        if (selection!=clicked) {
          // We had a previous selection.
          if (selection!=null)
            delegate.deselectPiece    (selection);
          delegate.selectPiece      (selection = clicked);
          delegate.setCameraState   (CameraState.Unit);
        }
      }
    }
    // No selection.
    else
      return false;
    // Something has happened, propagate the event.
    return true;
  }
  
  public void update () {
    // Cache some variables.
    camState        = delegate.getCameraState();
    // Check for camera toggling.
    handleNavigation();
    
    boolean altMode = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT);
    // We just switched to ALT mode.
    if (altMode && !inAltMode)
      inAltMode     = true;
    // We were in ALT mode...
    else if (inAltMode) {
      // ... But we're leaving ALT mode.
      if (!altMode)
        inAltMode     = false;
      // We are staying in ALT mode.
      else {
        handleMovement    (delegate.getTarget(), true, false);
        handleMovement    (delegate.getUnit(), true, false);
        for (final Piece piece : delegate.getPieces())
          handleMovement  (piece, true, false);
      }
      return;
    }

    // There is no selection made.
    if (camState==CameraState.Pieces) {
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

    else if (camState==CameraState.Unit) {
      // We are not in alt mode, and there is a selection.
      if (selection!=null) {
        // Only allow piece translation if not within first turn.
        handleMovement    (selection, true, delegate.getTurn()!=1);
        // Send turn to server.
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
          delegate.playPiece      (selection);
          selection               = null;
          delegate.setCameraState (CameraState.Pieces);
        }
      }
    }
  }
  
  // Checks whether the user has pushed the view toggling button.
  private void handleNavigation () {
    // Toggle between unit view and target view.
    if (Gdx.input.isKeyJustPressed(Keys.SPACE))
      delegate.setCameraState(camState==CameraState.Pieces ? CameraState.Unit : CameraState.Pieces);
    // Player is willing to go back.
    else if (camState!=CameraState.Pieces) {
      if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
        delegate.setCameraState(CameraState.Pieces);
        if (selection!=null) {
          delegate.deselectPiece  (selection);
          selection               = null;
        }
      }
    }
    // Player is asking to reset target and unit.
    else if (Gdx.input.isKeyJustPressed(Keys.R)) {
      delegate.resetPieceRotation (delegate.getTarget());
      delegate.resetPieceRotation (delegate.getUnit());
      for (final Piece p : delegate.getPieces())
        delegate.resetPieceRotation (p);
    }
  }
  // Checks whether the user is to translating / rotating.
  private void handleMovement (final Piece moveable, final boolean rotate, final boolean translate) {
    if (translate) {
      if (Gdx.input.isKeyJustPressed(Keys.W))         delegate.translatePiece (moveable, Communitrix.PositiveZ);
      else if(Gdx.input.isKeyJustPressed(Keys.S))     delegate.translatePiece (moveable, Communitrix.NegativeZ);
      if (Gdx.input.isKeyJustPressed(Keys.A))         delegate.translatePiece (moveable, Communitrix.PositiveX);
      else if (Gdx.input.isKeyJustPressed(Keys.D))    delegate.translatePiece (moveable, Communitrix.NegativeX);
      if (Gdx.input.isKeyJustPressed(Keys.O))         delegate.translatePiece (moveable, Communitrix.PositiveY);
      else if (Gdx.input.isKeyJustPressed(Keys.L))    delegate.translatePiece (moveable, Communitrix.NegativeY);
    }
    if (rotate) {
      if (Gdx.input.isKeyJustPressed(Keys.UP))        delegate.rotatePiece    (moveable, Vector3.X,  90);
      else if (Gdx.input.isKeyJustPressed(Keys.DOWN)) delegate.rotatePiece    (moveable, Vector3.X, -90);
      if (Gdx.input.isKeyJustPressed(Keys.RIGHT))     delegate.rotatePiece    (moveable, Vector3.Y,  90);
      else if (Gdx.input.isKeyJustPressed(Keys.LEFT)) delegate.rotatePiece    (moveable, Vector3.Y, -90);
    }
  }
}
