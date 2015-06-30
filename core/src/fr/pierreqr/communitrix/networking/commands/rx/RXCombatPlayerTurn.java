package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.shared.SHPiece;

public class RXCombatPlayerTurn extends RXBase {
  public    String        playerUUID;
  public    int           pieceId;
  public    int           unitId;
  public    SHPiece       unit;
  public RXCombatPlayerTurn () {
    super   (Rx.CombatPlayerTurn);
  }
}
