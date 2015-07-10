package fr.pierreqr.communitrix.networking.shared;

public class SHPiece {
  public    SHVector    size, min, max;
  public    SHCell[]    content;
  
  public SHPiece set (final SHPiece piece) {
    size.set    (piece.size);
    min.set     (piece.min);
    max.set     (piece.max);
    content     = piece.content;
    return      this;
  }
}
