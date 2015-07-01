package fr.pierreqr.communitrix.networking.commands.rx;

import fr.pierreqr.communitrix.networking.shared.SHCombat;

public class RXCombatList extends RXBase {
  public    SHCombat[]      combats;

  public RXCombatList () {
    super (Rx.CombatList);
  }
}
