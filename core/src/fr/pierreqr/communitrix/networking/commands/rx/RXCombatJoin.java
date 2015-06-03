package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.Combat;

public class RXCombatJoin extends RXBase {
  public    Combat              combat;

  public RXCombatJoin () {
    super (Rx.CombatJoin);
  }
}
