package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.shared.SHPiece;
import fr.pierreqr.communitrix.networking.shared.SHUnit;

public class RXCombatStart extends RXBase {
  public    String        uuid;
  public    SHPiece       target;
  public    SHUnit[]      units;
  public    SHPiece[]     pieces;
  
  public RXCombatStart () {
    super   (Rx.CombatStart);
  }
}
