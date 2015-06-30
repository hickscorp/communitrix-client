package fr.pierreqr.communitrix.networking.commands.rx;


public class RXCombatNewTurn extends RXBase {
  public    int       turnId;
  public    int       unitId;
  public RXCombatNewTurn () {
    super (Rx.CombatNewTurn);
  }
}
