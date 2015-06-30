package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.shared.SHPiece;

public class RXCombatStart extends RXBase {
  public    String        uuid;
  public    SHPiece       target;
  public    SHPiece[]     units, pieces;
  
  public RXCombatStart () {
    super   (Rx.CombatStart);
  }
}
