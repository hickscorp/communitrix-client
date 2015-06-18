package fr.pierreqr.communitrix.screens.util;

import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import fr.pierreqr.communitrix.gameObjects.Piece;

public class PiecesDock {
  public interface PiecesDockDelegate {
    Array<Piece>  getPieces       ();
    void          translatePiece  (final Piece piece, final Vector3 axis);
    void          rotatePiece     (final Piece piece, final Vector3 axis, final int angle);
  };
  
  private final         PiecesDockDelegate  delegate;
  private               int                 firstPieceIndex = 0;
  
  private final static  Vector3                   tmpVec3         = new Vector3();
  private static        CatmullRomSpline<Vector3> path            = null;
  
  public PiecesDock (final PiecesDockDelegate delegate) {
    this.delegate   = delegate;
    if (path==null)
      path          = new CatmullRomSpline<Vector3>(
        new Vector3[]{
            new Vector3(-10,  0, -10),
            new Vector3(  0,  0,  -5),
            new Vector3( 10,  0, -10),
        }, true);
  }
  
  public int getFirstPieceIndex () {
    return firstPieceIndex;
  }
  public void setFirstPieceIndex (final int firstPieceIndex) {
    this.firstPieceIndex        = firstPieceIndex;
    final Array<Piece>  pieces  = delegate.getPieces();
    final Vector3       shift   = new Vector3();
    final float         factor  = 1.0f / pieces.size;
    for (int i=0; i<pieces.size; i++) {
      final int   index   = ( i + firstPieceIndex ) % pieces.size;
      final Piece piece   = pieces.get(index);
      piece.transform
        .getTranslation   (tmpVec3);
      // Get the target point from the path.
      path.valueAt(shift, factor * i);
      shift.sub           (tmpVec3);
      delegate
        .translatePiece   (piece, shift);
    }
  }
}
