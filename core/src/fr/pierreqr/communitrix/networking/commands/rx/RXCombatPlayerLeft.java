package fr.pierreqr.communitrix.networking.commands.rx;

public class RXCombatPlayerLeft extends RXBase {
  public    String    player;

  public RXCombatPlayerLeft () {
    super (Rx.CombatPlayerLeft);
  }
}
