package fr.pierreqr.communitrix.networking.shared;

import java.util.HashMap;

public class SHUnit extends SHPiece {
  public HashMap<String, int[]>  moves;
  
  public SHUnit set (final SHUnit unit) {
    super.set     (unit);
    moves.clear   ();
    moves.putAll  (moves);
    return        this;
  }
}
