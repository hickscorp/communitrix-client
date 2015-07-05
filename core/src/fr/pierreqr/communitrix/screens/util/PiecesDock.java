package fr.pierreqr.communitrix.screens.util;

import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.Piece;

public class PiecesDock extends GameObject {
  public interface PiecesDockDelegate {
    Array<Piece>  getAvailablePieces    ();
    void          selectPiece           (final Piece piece);
    void          translateWithinView   (final GameObject obj, final Vector3 axis);
    void          rotateWithinView      (final GameObject obj, final Vector3 axis, final int angle);
  };
  
  private final   PiecesDockDelegate        delegate;
  public          int                       firstPieceIndex   = 0;
  private static  CatmullRomSpline<Vector3> path            = null;
  
  public PiecesDock (final PiecesDockDelegate newDelegate) {
    super           (Communitrix.getInstance().dummyModel);
    delegate        = newDelegate;
    if (path==null)
      path          = new CatmullRomSpline<Vector3>(
        new Vector3[]{
            new Vector3( 0, 0, 0),
            new Vector3( 5, 0, 2),
            new Vector3( 4, 0, 2),
            new Vector3( 3, 0, 2),
            new Vector3( 2, 0, 2),
            new Vector3( 1, 0, 2),
            new Vector3( 0, 0, 2),
            new Vector3(-1, 0, 2),
            new Vector3(-2, 0, 2),
            new Vector3(-3, 0, 2),
            new Vector3(-4, 0, 2),
            new Vector3(-5, 0, 2),
        }, true);
  }
  
  public void refresh () {
    setFirstPieceIndex  (firstPieceIndex);
  }
  public void cycle (final int increment) {
    final int size        = delegate.getAvailablePieces().size;
    if (size==0)          return;
    int       newIndex    = (firstPieceIndex + increment) % size;
    while (newIndex < 0)  newIndex += size;
    setFirstPieceIndex    (newIndex);
  }
  public void setFirstPieceIndex (final int firstPieceIndex) {
    this.firstPieceIndex        = firstPieceIndex;
    // Cache some variables.
    final Array<Piece>  pieces  = delegate.getAvailablePieces();
    if (pieces.size==0)         return;
    // Compute largest piece size.
    int                 largest = Integer.MIN_VALUE;
    for (final Piece piece : pieces)
      largest   = Math.max(Math.max(Math.max(largest, piece.sharedPiece.size.x), piece.sharedPiece.size.y), piece.sharedPiece.size.z);
    largest     += 1.5f;
    
    final Vector3       tmpVec3 = new Vector3();
    final Vector3       shift   = new Vector3();
    final float         factor  = 1.0f / pieces.size;
    for (int i=0; i<pieces.size; i++) {
      final int   index   = ( i + firstPieceIndex ) % pieces.size;
      final Piece piece   = pieces.get(index);
      tmpVec3
        .set              (piece.targetPosition);
      // Get the target point from the path.
      path
        .valueAt          (shift, factor * i)
        .scl              (2)
        .add              (0, -3, -3);
      shift.sub           (tmpVec3);
      delegate
        .translateWithinView(piece, shift);
    }
  }
}
