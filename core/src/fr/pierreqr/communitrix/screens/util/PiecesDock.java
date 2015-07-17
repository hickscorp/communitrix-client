package fr.pierreqr.communitrix.screens.util;

import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.Piece;

public class PiecesDock extends GameObject {
  public interface PiecesDockDelegate {
    void          selectPiece           (final Piece piece);
    void          translateWithinView   (final GameObject obj, final Vector3 axis, final boolean checkCollisions);
  };
  
  private final   PiecesDockDelegate        delegate;
  private final   Array<Piece>              availablePieces;
  public          int                       firstPieceIndex   = 0;
  private static  CatmullRomSpline<Vector3> path            = null;
  
  public PiecesDock (final PiecesDockDelegate _delegate, final Array<Piece> _pieces, final Array<Piece> _availablePieces) {
    super           (Communitrix.getInstance().dummyModel);
    delegate        = _delegate;
    availablePieces = _availablePieces;
    if (path==null)
      path          = new CatmullRomSpline<Vector3>(
        new Vector3[]{
            new Vector3( 0, 0, 1),
            new Vector3( 5, 0, 3),
            new Vector3( 4, 0, 3),
            new Vector3( 3, 0, 3),
            new Vector3( 2, 0, 3),
            new Vector3( 1, 0, 3),
            new Vector3( 0, 0, 3),
            new Vector3(-1, 0, 3),
            new Vector3(-2, 0, 3),
            new Vector3(-3, 0, 3),
            new Vector3(-4, 0, 3),
            new Vector3(-5, 0, 3),
        }, true);
  }
  
  public void refresh () {
    setFirstPieceIndex  (firstPieceIndex);
  }
  public void cycle (final int increment) {
    final int size        = availablePieces.size;
    if (size==0)          return;
    int       newIndex    = (firstPieceIndex + increment) % size;
    while (newIndex < 0)  newIndex += size;
    setFirstPieceIndex    (newIndex);
  }
  public void setFirstPieceIndex (final int firstPieceIndex) {
    this.firstPieceIndex        = firstPieceIndex;
    // Cache some variables.
    if (availablePieces.size==0)         return;
    // Compute largest piece size.
    int                 largest = Integer.MIN_VALUE;
    for (final Piece piece : availablePieces)
      largest   = Math.max(Math.max(Math.max(largest, piece.sharedPiece.size.x), piece.sharedPiece.size.y), piece.sharedPiece.size.z);
    largest     += 1.5f;
    
    final Vector3       tmpVec3 = new Vector3();
    final Vector3       shift   = new Vector3();
    final float         factor  = 1.0f / availablePieces.size;
    for (int i=0; i<availablePieces.size; i++) {
      final int   index   = ( i + firstPieceIndex ) % availablePieces.size;
      final Piece piece   = availablePieces.get(index);
      tmpVec3
        .set              (piece.targetPosition);
      // Get the target point from the path.
      path
        .valueAt          (shift, factor * i)
        .scl              (2)
        .add              (0, -3, -5);
      shift.sub           (tmpVec3);
      delegate
        .translateWithinView(piece, shift, false);
    }
  }
}
