package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.Player;

public class RXCombatPlayerLeft extends RXBase {
  public    String      uuid;

  public RXCombatPlayerLeft () {
    super (Rx.CombatPlayerLeft);
  }
}
