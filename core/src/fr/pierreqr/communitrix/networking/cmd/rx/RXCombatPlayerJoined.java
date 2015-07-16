package fr.pierreqr.communitrix.networking.cmd.rx;

import fr.pierreqr.communitrix.networking.cmd.beans.PlayerBean;

public class RXCombatPlayerJoined extends RXBase {
  public    PlayerBean    player;

  public RXCombatPlayerJoined () {
    super (Type.CombatPlayerJoined);
  }
}
