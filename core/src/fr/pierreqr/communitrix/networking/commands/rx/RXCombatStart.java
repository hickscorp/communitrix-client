package fr.pierreqr.communitrix.networking.commands.rx;

public class RXCombatStart extends RXBase {
  public    String    uuid;
  public    String[]  players;
  
  public RXCombatStart () {
    super   (Rx.CombatStart);
  }
}
