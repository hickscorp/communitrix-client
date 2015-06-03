package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.Vector;

public class RXCombatPlayerTurn extends RXBase {
  public    String      playerUUID;
  public    Vector[]    contents;
  public RXCombatPlayerTurn () {
    super   (Rx.CombatPlayerTurn);
  }
}
