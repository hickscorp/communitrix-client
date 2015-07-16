package fr.pierreqr.communitrix.networking.cmd.rx;

import fr.pierreqr.communitrix.networking.cmd.beans.UnitBean;

public class RXCombatPlayerTurn extends RXBase {
  public    String        playerUUID;
  public    int           pieceId;
  public    int           unitId;
  public    UnitBean        unit;
  public RXCombatPlayerTurn () {
    super   (Type.CombatPlayerTurn);
  }
}
