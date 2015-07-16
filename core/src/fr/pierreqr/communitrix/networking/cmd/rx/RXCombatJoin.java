package fr.pierreqr.communitrix.networking.cmd.rx;

import fr.pierreqr.communitrix.networking.cmd.beans.CombatBean;

public class RXCombatJoin extends RXBase {
  public    CombatBean              combat;

  public RXCombatJoin () {
    super (Type.CombatJoin);
  }
}
