package fr.pierreqr.communitrix.screens.inputControllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.Piece;

public class ICLobby extends InputAdapter{
  public interface ICLobbyDelegate {
    Camera        getCamera       ();
    Array<Piece>  getPieces       ();
    void          cyclePieces     (final int pieceIndex);
    void          translatePiece  (final Piece piece, final Vector3 axis);
    void          rotatePiece     (final Piece piece, final Vector3 axis, final int angle);
    void          playPiece       (final Piece piece);
  };
  
  private final         ICLobbyDelegate     delegate;
  private               Piece               selection       = null;
  private               int                 firstPieceIndex = 0;
  private final         Vector3             dragOffset;
  private final         Vector3             position;
  
  public ICLobby (final ICLobbyDelegate delegate) {
    dragOffset      = new Vector3();
    position        = new Vector3();
    this.delegate   = delegate;
  }
  
  @Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
    // Reset selection.
    selection               = null;
    // Pick a ray from the cam.
    final Ray   ray         = delegate.getCamera().getPickRay(screenX, screenY);
    // Dist will be a temp, while sDist will be the shortest found distance.
    float       dist, sDist = Float.MAX_VALUE;
    BoundingBox bounds      = null;
    // Iterate through all instances.
    for (final Piece obj : delegate.getPieces()) {
      obj.transform
        .getTranslation (position);
      dist              = ray.origin.dst2(position);
      //if (dist<sDist && Intersector.intersectRaySphere(ray, position, obj.radius, null)) {
      if (dist<sDist) {
        bounds          = new BoundingBox(obj.bounds);
        bounds.min.add  (position);
        bounds.max.add  (position);
        if (Intersector.intersectRayBounds(ray, bounds, null)) {
          sDist         = dist;
          selection     = obj;
          position
            .set            (ray.direction)
            .scl            (-ray.origin.y / ray.direction.y)
            .add            (ray.origin);
          selection
            .transform
            .getTranslation (dragOffset)
            .sub            (position);
        }
      }
    }
    return selection!=null;
  }
  
  public void update () {
    if (selection==null) {
      if (Gdx.input.isKeyJustPressed(Keys.A)) {
        firstPieceIndex++;
        if (firstPieceIndex>=delegate.getPieces().size)
          firstPieceIndex  = 0;
        delegate.cyclePieces(firstPieceIndex);
      }
      else if (Gdx.input.isKeyJustPressed(Keys.D)) {
        firstPieceIndex--;
        if (firstPieceIndex<0)
          firstPieceIndex  = delegate.getPieces().size-1;
        delegate.cyclePieces(firstPieceIndex);
      }
    }
    // There is a selection, behave accordingly.
    else {
      // Handle relative rotation.
      if (Gdx.input.isKeyJustPressed(Keys.RIGHT))
        delegate.rotatePiece(selection, Vector3.Y, 90);
      else if (Gdx.input.isKeyJustPressed(Keys.LEFT))
        delegate.rotatePiece(selection, Vector3.Y, -90);
      if (Gdx.input.isKeyJustPressed(Keys.UP))
        delegate.rotatePiece(selection, Vector3.X, 90);
      else if (Gdx.input.isKeyJustPressed(Keys.DOWN))
        delegate.rotatePiece(selection, Vector3.X, -90);
      
      // Handle translation.
      if (Gdx.input.isKeyJustPressed(Keys.W))
        delegate.translatePiece(selection, Communitrix.PositiveZ);
      else if(Gdx.input.isKeyJustPressed(Keys.S))
        delegate.translatePiece(selection, Communitrix.NegativeZ);
      if (Gdx.input.isKeyJustPressed(Keys.A))
        delegate.translatePiece(selection, Communitrix.PositiveX);
      else if (Gdx.input.isKeyJustPressed(Keys.D))
        delegate.translatePiece(selection, Communitrix.NegativeX);
      if (Gdx.input.isKeyJustPressed(Keys.O))
        delegate.translatePiece(selection, Communitrix.PositiveY);
      else if (Gdx.input.isKeyJustPressed(Keys.L))
        delegate.translatePiece(selection, Communitrix.NegativeY);
      
      // Other actions.
      if (Gdx.input.isKeyJustPressed(Keys.ENTER))
        delegate.playPiece(selection);
    }
  }
}
