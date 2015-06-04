package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.Vector;

public class RXCombatStart extends RXBase {
  public    String      uuid;
  public    Vector[]    target;
  public    Vector[][]  pieces;
  
  public RXCombatStart () {
    super   (Rx.CombatStart);
  }
}
