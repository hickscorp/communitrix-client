package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.Piece;

public class RXCombatStart extends RXBase {
  public    String      uuid;
  public    Piece       target;
  public    Piece[]     cells;
  public    Piece[]     pieces;
  
  public RXCombatStart () {
    super   (Rx.CombatStart);
  }
}
