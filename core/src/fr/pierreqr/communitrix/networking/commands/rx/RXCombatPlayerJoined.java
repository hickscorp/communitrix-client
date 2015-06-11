package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.shared.SHPlayer;

public class RXCombatPlayerJoined extends RXBase {
  public    SHPlayer    player;

  public RXCombatPlayerJoined () {
    super (Rx.CombatPlayerJoined);
  }
}
