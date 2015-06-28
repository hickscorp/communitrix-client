package fr.pierreqr.communitrix.screens.util;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import fr.pierreqr.communitrix.gameObjects.Piece;
import fr.pierreqr.communitrix.networking.shared.SHVector;

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
    setFirstPieceIndex(firstPieceIndex);
  }
  public int getFirstPieceIndex () {
    return firstPieceIndex;
  }
  public void setFirstPieceIndex (final int firstPieceIndex) {
    // Compute largest piece size.
    final SHVector      largest = new SHVector(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    final Array<Piece>  pieces  = delegate.getAvailablePieces();
    for (final Piece piece : pieces) {
      if (largest.x<piece.sharedPiece.size.x)  largest.x   = piece.sharedPiece.size.x;
      if (largest.y<piece.sharedPiece.size.x)  largest.y   = piece.sharedPiece.size.y;
      if (largest.z<piece.sharedPiece.size.x)  largest.z   = piece.sharedPiece.size.z;
    }
    largest.add                 (2, 2, 2);
    final int           width   = (int)Math.round(Math.sqrt(pieces.size));
    final Vector3       origin  = new Vector3(-(width-1)/2.0f*largest.x, -3, -(width-1)/2.0f*largest.z);
    final Vector3       shift   = new Vector3(0, 0, 0);
    for (int i=0; i<pieces.size; ++i) {
      final int         index   = ( i + firstPieceIndex ) % pieces.size;
      final int         x       = ( i % width ) * largest.x;
      final int         z       = ( i / width ) * largest.z;
      final Piece       piece   = pieces.get(index);
      shift
        .set  (x, 0, z)
        .add  (origin)
        .sub  (piece.targetPosition.x, piece.targetPosition.y, piece.targetPosition.z);
      delegate.translatePiece   (piece, shift);
    }
  }
}
