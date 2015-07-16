package fr.pierreqr.communitrix.networking.cmd.rx;

import fr.pierreqr.communitrix.networking.cmd.beans.PieceBean;
import fr.pierreqr.communitrix.networking.cmd.beans.UnitBean;

public class RXCombatStart extends RXBase {
  public    String        uuid;
  public    PieceBean       target;
  public    UnitBean[]      units;
  public    PieceBean[]     pieces;
  
  public RXCombatStart () {
    super   (Type.CombatStart);
  }
}
