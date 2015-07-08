package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.shared.SHUnit;

public class RXCombatPlayerTurn extends RXBase {
  public    String        playerUUID;
  public    int           pieceId;
  public    int           unitId;
  public    SHUnit        unit;
  public RXCombatPlayerTurn () {
    super   (Rx.CombatPlayerTurn);
  }
}
