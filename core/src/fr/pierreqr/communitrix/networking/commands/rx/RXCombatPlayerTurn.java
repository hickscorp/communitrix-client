package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.Piece;

public class RXCombatPlayerTurn extends RXBase {
  public    String      playerUUID;
  public    Piece       piece;
  public RXCombatPlayerTurn () {
    super   (Rx.CombatPlayerTurn);
  }
}
