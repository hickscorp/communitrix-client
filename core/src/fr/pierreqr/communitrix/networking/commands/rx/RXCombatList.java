package fr.pierreqr.communitrix.networking.commands.rx;

import java.util.ArrayList;

public class RXCombatList extends RXBase {
  public  ArrayList<String>     combats;

  public RXCombatList () {
    super (Rx.CombatList);
  }
}
