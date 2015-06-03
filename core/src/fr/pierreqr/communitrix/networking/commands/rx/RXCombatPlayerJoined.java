package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.Player;

public class RXCombatPlayerJoined extends RXBase {
  public    Player    player;

  public RXCombatPlayerJoined () {
    super (Rx.CombatPlayerJoined);
  }
}
