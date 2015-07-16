package fr.pierreqr.communitrix.networking.cmd.beans;

import java.util.HashMap;

public class UnitBean extends PieceBean {
  public HashMap<String, int[]>  moves;
  
  public UnitBean set (final UnitBean unit) {
    super.set     (unit);
    moves.clear   ();
    moves.putAll  (moves);
    return        this;
  }
}
