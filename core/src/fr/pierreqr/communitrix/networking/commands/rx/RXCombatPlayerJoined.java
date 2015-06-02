package fr.pierreqr.communitrix.networking.commands.rx;

public class RXCombatPlayerJoined extends RXBase {
  public    String    player;

  public RXCombatPlayerJoined () {
    super (Rx.CombatPlayerJoined);
  }
}
