package fr.pierreqr.communitrix.screens.util;

import aurelienribon.tweenengine.TweenManager;
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
    TweenManager  getTweener            ();
  };
  
  private final         PiecesDockDelegate  delegate;
  private               int                 firstPieceIndex = 0;
  
  public PiecesDock (final PiecesDockDelegate newDelegate) {
    super           (Communitrix.getInstance().dummyModel);
    delegate        = newDelegate;
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
    largest     += 1.5f;
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
        .sub  (piece.targetPosition);
      delegate.translateWithinView(piece, shift);
    }
  }
}
