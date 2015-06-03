package fr.pierreqr.communitrix.networking.commands.rx;

public class RXCombatPlayerLeft extends RXBase {
  public    String      uuid;

  public RXCombatPlayerLeft () {
    super (Rx.CombatPlayerLeft);
  }
}
