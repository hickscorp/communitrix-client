package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.shared.SHCombat;

public class RXCombatJoin extends RXBase {
  public    SHCombat              combat;

  public RXCombatJoin () {
    super (Rx.CombatJoin);
  }
}
