package fr.pierreqr.communitrix.networking.cmd.beans;

public class PieceBean {
  public    VectorBean    size, min, max;
  public    CellBean[]    content;
  
  public PieceBean set (final PieceBean piece) {
    size.set    (piece.size);
    min.set     (piece.min);
    max.set     (piece.max);
    content     = piece.content;
    return      this;
  }
}
