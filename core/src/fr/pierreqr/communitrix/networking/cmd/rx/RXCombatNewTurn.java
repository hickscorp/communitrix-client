package fr.pierreqr.communitrix.networking.cmd.rx;


public class RXCombatNewTurn extends RXBase {
  public    int       turnId;
  public    int       unitId;
  public RXCombatNewTurn () {
    super (Type.CombatNewTurn);
  }
}
