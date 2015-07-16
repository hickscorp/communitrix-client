package fr.pierreqr.communitrix.networking.cmd.tx;

public class TXCombatJoin extends TXBase {
  public    String    uuid;
  
  public TXCombatJoin (final String uuid) {
    super       (Type.CombatJoin);
    this.uuid   = uuid;
  }
}
