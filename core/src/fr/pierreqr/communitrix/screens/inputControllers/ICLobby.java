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
import fr.pierreqr.communitrix.screens.SCLobby.State;
import fr.pierreqr.communitrix.screens.util.PiecesDock;

public class ICLobby extends InputAdapter {
  public interface ICLobbyDelegate {
    State         getState              ();
    void          setState              (final State state);
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
    void          cyclePieces           (final int increment);
    void          hoverPiece            (final Piece piece);
    void          selectPiece           (final Piece piece);
    void          translateWithinView   (final GameObject obj, final Vector3 axis, final boolean checkCollisions);
    void          rotateWithinView      (final GameObject obj, final Vector3 axis, final int angle, final boolean checkCollisions);
    void          resetRotation         (final GameObject obj);
    void          playPiece             (final Piece piece);
  };
  
  private final         ICLobbyDelegate     delegate;
  private               Piece               selectedPiece     = null;
  private               Piece               hoveredPiece      = null;
  private               boolean             inAltMode         = false;
  
  private final static  Vector3             tmpVec3           = new Vector3();
  private static        CameraState         camState;
  
  public ICLobby (final ICLobbyDelegate delegate) {
    this.delegate   = delegate;
  }
  
  @Override public boolean mouseMoved(int screenX, int screenY) {
    if (delegate.getCameraState()!=CameraState.Pieces)
      return false;
    final Piece hovered     = rayPickTest(screenX, screenY);
    if (hoveredPiece!=hovered) {
      if (hoveredPiece!=null)
        delegate.hoverPiece (hovered);
      hoveredPiece          = hovered;
      if (hoveredPiece!=null) {
        delegate.hoverPiece (hoveredPiece);
        return true;
      }
    }
    return false;
  }
  
  @Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
    if (delegate.getPlayedPiece()!=null) {
      Communitrix.getInstance()
        .setLastError       (0, "Please wait for all players to complete this turn.");
      return false;
    }
    else if (delegate.getState()!=State.NewTurn || delegate.getCameraState()!=CameraState.Pieces)
      return false;
    final Piece clicked   = rayPickTest(screenX, screenY);
    // We have a new selection.
    if (clicked!=null) {
      // Player clicked the unit.
      if (clicked==delegate.getUnit())
        delegate.setCameraState(CameraState.Unit);
      // Player clicked on the working unit block, or on a piece.
      else {
        // User clicked something different.
        if (selectedPiece!=clicked) {
          // We had a previous selection.
          if (selectedPiece!=null)
            delegate.selectPiece    (null);
          delegate.selectPiece      (selectedPiece = clicked);
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
  private Piece rayPickTest (final int screenX, final int screenY) {
    // Reset selection.
    Piece       result     = null;
    // Pick a ray from the cam.
    final Ray   ray        = delegate.getCamera().getPickRay(screenX, screenY);
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
          result       = obj;
        }
      }
    }
    return result;
  }
  
  @Override public boolean scrolled (int amount) {
    if (delegate.getState()!=State.NewTurn && delegate.getState()!=State.EndGame)
      return false;
    delegate.zoom   (amount);
    return true;
  }
  
  public void update () {
    // Cache some variables.
    camState        = delegate.getCameraState();
    // Check for camera toggling.
    handleNavigation();
    if (delegate.getState()==State.Global)
      return;
    
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
        handleMovement    (delegate.getTarget(), true, false, false);
        handleMovement    (delegate.getUnit(), true, false, false);
        final Piece unit = delegate.getUnit();
        for (final Piece piece : delegate.getAvailablePieces())
          if (piece.parent!=unit)
            handleMovement  (piece, true, false, false);
      }
      return;
    }

    // There is no selection made.
    if (camState==CameraState.Pieces) {
      // Cycle pieces left.
      if (Gdx.input.isKeyJustPressed(Communitrix.Keys[Communitrix.MoveLeft]))
        delegate.cyclePieces  (-1);
      // Cycle pieces right.
      else if (Gdx.input.isKeyJustPressed(Communitrix.Keys[Communitrix.MoveRight]))
        delegate.cyclePieces  (1);
    }

    else if (camState==CameraState.Unit) {
      // We are not in alt mode, and there is a selection.
      if (selectedPiece!=null) {
        // Only allow piece translation if not within first turn.
        handleMovement    (selectedPiece, true, delegate.getTurn()!=1, true);
        // Send turn to server.
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
          delegate.playPiece      (selectedPiece);
          selectedPiece               = null;
          delegate.setCameraState (CameraState.Pieces);
        }
      }
    }
  }
  
  // Checks whether the user has pushed the view toggling button.
  private void handleNavigation () {
    final State state = delegate.getState();
    // Toggle between unit view and target view.
    if ((state==State.NewTurn || state==State.EndGame)) {
      if (Gdx.input.isKeyJustPressed(Communitrix.Keys[Communitrix.CycleView]))
        delegate.setCameraState(camState==CameraState.Pieces ? CameraState.Unit : CameraState.Pieces);
      // Player is asking to reset target and unit.
      if (Gdx.input.isKeyJustPressed(Communitrix.Keys[Communitrix.Reset])) {
        delegate.resetRotation (delegate.getTarget());
        delegate.resetRotation (delegate.getUnit());
        for (final Piece piece : delegate.getAvailablePieces())
          delegate.resetRotation (piece);
      }
    }
    // Player is willing to go back.
    if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
      if (state==State.Global)
        delegate.setState         (State.Settings);
      else if (state==State.Settings)
        delegate.setState         (State.Global);
      else if (state==State.EndGame)
        delegate.setState         (State.Global);
      else {
        delegate.setCameraState   (CameraState.Pieces);
        if (selectedPiece!=null)
          delegate.selectPiece    (selectedPiece = null);
      }
    }
  }
  // Checks whether the user is to translating / rotating.
  private void handleMovement (final Piece moveable, final boolean rotate, final boolean translate, final boolean checkCollisions) {
    if (translate) {
      if (Gdx.input.isKeyJustPressed(Communitrix.Keys[Communitrix.MoveForward]))
        delegate.translateWithinView (moveable, Communitrix.PositiveZ, checkCollisions);
      else if(Gdx.input.isKeyJustPressed(Communitrix.Keys[Communitrix.MoveBackward]))
        delegate.translateWithinView (moveable, Communitrix.NegativeZ, checkCollisions);
      if (Gdx.input.isKeyJustPressed(Communitrix.Keys[Communitrix.MoveLeft]))
        delegate.translateWithinView (moveable, Communitrix.PositiveX, checkCollisions);
      else if (Gdx.input.isKeyJustPressed(Communitrix.Keys[Communitrix.MoveRight]))
        delegate.translateWithinView (moveable, Communitrix.NegativeX, checkCollisions);
      if (Gdx.input.isKeyJustPressed(Communitrix.Keys[Communitrix.MoveUp]))
        delegate.translateWithinView (moveable, Communitrix.PositiveY, checkCollisions);
      else if (Gdx.input.isKeyJustPressed(Communitrix.Keys[Communitrix.MoveDown]))
        delegate.translateWithinView (moveable, Communitrix.NegativeY, checkCollisions);
    }
    if (rotate) {
      if (Gdx.input.isKeyJustPressed(Communitrix.Keys[Communitrix.RotateUp]))
        delegate.rotateWithinView    (moveable, Vector3.X,  90, checkCollisions);
      else if (Gdx.input.isKeyJustPressed(Communitrix.Keys[Communitrix.RotateDown]))
        delegate.rotateWithinView    (moveable, Vector3.X, -90, checkCollisions);
      if (Gdx.input.isKeyJustPressed(Communitrix.Keys[Communitrix.RotateRight]))
        delegate.rotateWithinView    (moveable, Vector3.Y,  90, checkCollisions);
      else if (Gdx.input.isKeyJustPressed(Communitrix.Keys[Communitrix.RotateLeft]))
        delegate.rotateWithinView    (moveable, Vector3.Y, -90, checkCollisions);
    }
  }
}
