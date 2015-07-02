package fr.pierreqr.communitrix.screens.util;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import fr.pierreqr.communitrix.gameObjects.Piece;

public class PiecesDock {
  public interface PiecesDockDelegate {
    Array<Piece>  getAvailablePieces  ();
    void          selectPiece         (final Piece piece);
    void          translatePiece      (final Piece piece, final Vector3 axis);
    void          rotatePiece         (final Piece piece, final Vector3 axis, final int angle);
  };
  
  private final         PiecesDockDelegate  delegate;
  private               int                 firstPieceIndex = 0;
  
  public PiecesDock (final PiecesDockDelegate delegate) {
    this.delegate   = delegate;
  }
  
  public void refresh () {
    setFirstPieceIndex  (firstPieceIndex);
  }
  public int getFirstPieceIndex () {
    return firstPieceIndex;
  }
  public void setFirstPieceIndex (final int firstPieceIndex) {
    this.firstPieceIndex        = firstPieceIndex;
    // Compute largest piece size.
    final Array<Piece>  pieces  = delegate.getAvailablePieces();
    int                 largest = Integer.MIN_VALUE;
    for (final Piece piece : pieces)
      largest   = Math.max(Math.max(Math.max(largest, piece.sharedPiece.size.x), piece.sharedPiece.size.y), piece.sharedPiece.size.z);
    largest     += 2;
    final int           width   = (int)Math.round(Math.sqrt(pieces.size));
    final Vector3       origin  = new Vector3(-(width-1)/2.0f*largest, -3, -(width-1)/2.0f*largest);
    final Vector3       shift   = new Vector3(0, 0, 0);
    for (int i=0; i<pieces.size; ++i) {
      final int         index   = ( i + firstPieceIndex ) % pieces.size;
      final int         x       = ( i % width ) * largest;
      final int         z       = ( i / width ) * largest;
      final Piece       piece   = pieces.get(index);
      shift
        .set  (x, 0, z)
        .add  (origin)
        .sub  (piece.anim.targetPosition);
      delegate.translatePiece   (piece, shift);
    }
  }
}
