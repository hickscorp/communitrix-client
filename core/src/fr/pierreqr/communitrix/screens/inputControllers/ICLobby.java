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
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.Piece;
import fr.pierreqr.communitrix.screens.SCLobby.CameraState;
import fr.pierreqr.communitrix.screens.util.PiecesDock;

public class ICLobby extends InputAdapter {
  public interface ICLobbyDelegate {
    int           getTurn               ();
    Camera        getCamera             ();
    CameraState   getCameraState        ();
    void          setCameraState        (final CameraState state);
    void          zoom                  (int amount);
    Piece         getUnit               ();
    Piece         getTarget             ();
    PiecesDock    getPiecesDock         ();
    Piece         getPlayedPiece        ();
    Array<Piece>  getPieces             ();
    Array<Piece>  getAvailablePieces    ();
    void          cyclePieces           (final int pieceIndex);
    void          selectPiece           (final Piece piece);
    void          translateWithinView   (final GameObject obj, final Vector3 axis);
    void          rotateWithinView      (final GameObject obj, final Vector3 axis, final int angle);
    void          resetRotation         (final GameObject obj);
    void          playPiece             (final Piece piece);
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
    if (delegate.getPlayedPiece()!=null) {
      Communitrix.getInstance()
        .setLastError       (0, "Please wait for all players to complete this turn.");
      return false;
    }
    // Reset selection.
    Piece       clicked     = null;
    // Pick a ray from the cam.
    final Ray   ray         = delegate.getCamera().getPickRay(screenX, screenY);
    // Variable dist will be a temp, while sDist will be the shortest found distance.
    float       dist, sDist = Float.MAX_VALUE;
    BoundingBox bounds      = null;
    // Iterate through all instances.
    for (final Piece obj : delegate.getAvailablePieces()) {
      tmpVec3.set       (obj.targetPosition);
      dist              = ray.origin.dst2(tmpVec3);
      if (dist<sDist) {
        bounds          = new BoundingBox(obj.fakeBounds);
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
            delegate.selectPiece    (null);
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
  
  @Override public boolean scrolled (int amount) {
    delegate.zoom   (amount);
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
        final Piece unit = delegate.getUnit();
        for (final Piece piece : delegate.getAvailablePieces())
          if (piece.parent!=unit)
            handleMovement  (piece, true, false);
      }
      return;
    }

    // There is no selection made.
    if (camState==CameraState.Pieces) {
      // Cache some members.
      final Array<Piece>  pieces    = delegate.getAvailablePieces();
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
    // Player is asking to reset target and unit.
    if (Gdx.input.isKeyJustPressed(Keys.R)) {
      delegate.resetRotation (delegate.getTarget());
      delegate.resetRotation (delegate.getUnit());
      for (final Piece piece : delegate.getAvailablePieces())
        delegate.resetRotation (piece);
    }
    // Player is willing to go back.
    if (camState!=CameraState.Pieces) {
      if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
        delegate.setCameraState   (CameraState.Pieces);
        if (selection!=null)
          delegate.selectPiece    (selection = null);
      }
    }
  }
  // Checks whether the user is to translating / rotating.
  private void handleMovement (final Piece moveable, final boolean rotate, final boolean translate) {
    if (translate) {
      if (Gdx.input.isKeyJustPressed(Keys.W))         delegate.translateWithinView (moveable, Communitrix.PositiveZ);
      else if(Gdx.input.isKeyJustPressed(Keys.S))     delegate.translateWithinView (moveable, Communitrix.NegativeZ);
      if (Gdx.input.isKeyJustPressed(Keys.A))         delegate.translateWithinView (moveable, Communitrix.PositiveX);
      else if (Gdx.input.isKeyJustPressed(Keys.D))    delegate.translateWithinView (moveable, Communitrix.NegativeX);
      if (Gdx.input.isKeyJustPressed(Keys.O))         delegate.translateWithinView (moveable, Communitrix.PositiveY);
      else if (Gdx.input.isKeyJustPressed(Keys.L))    delegate.translateWithinView (moveable, Communitrix.NegativeY);
    }
    if (rotate) {
      if (Gdx.input.isKeyJustPressed(Keys.UP))        delegate.rotateWithinView    (moveable, Vector3.X,  90);
      else if (Gdx.input.isKeyJustPressed(Keys.DOWN)) delegate.rotateWithinView    (moveable, Vector3.X, -90);
      if (Gdx.input.isKeyJustPressed(Keys.RIGHT))     delegate.rotateWithinView    (moveable, Vector3.Y,  90);
      else if (Gdx.input.isKeyJustPressed(Keys.LEFT)) delegate.rotateWithinView    (moveable, Vector3.Y, -90);
    }
  }
}
