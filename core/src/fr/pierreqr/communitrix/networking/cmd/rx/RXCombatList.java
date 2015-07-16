package fr.pierreqr.communitrix.networking.cmd.rx;

import fr.pierreqr.communitrix.networking.cmd.beans.CombatBean;

public class RXCombatList extends RXBase {
  public    CombatBean[]      combats;

  public RXCombatList () {
    super (Type.CombatList);
  }
}
