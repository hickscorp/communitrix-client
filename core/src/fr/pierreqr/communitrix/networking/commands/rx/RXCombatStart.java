package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.shared.SHPiece;

public class RXCombatStart extends RXBase {
  public    String      uuid;
  public    SHPiece       target;
  public    SHPiece[]     cells;
  public    SHPiece[]     pieces;
  
  public RXCombatStart () {
    super   (Rx.CombatStart);
  }
}
