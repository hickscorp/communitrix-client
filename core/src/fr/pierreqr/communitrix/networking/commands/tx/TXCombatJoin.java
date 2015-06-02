package fr.pierreqr.communitrix.networking.commands.tx;

public class TXCombatJoin extends TXBase {
  public    String    uuid;
  
  public TXCombatJoin (final String uuid) {
    super       (Tx.CombatJoin);
    this.uuid   = uuid;
  }
}
