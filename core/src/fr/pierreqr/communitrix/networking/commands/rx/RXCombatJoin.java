package fr.pierreqr.communitrix.networking.commands.rx;

import java.util.ArrayList;

public class RXCombatJoin extends RXBase {
  public    String              uuid;
  public    int                 minPlayers, maxPlayers;
  public    ArrayList<String>   players;

  public RXCombatJoin () {
    super (Rx.CombatJoin);
  }
}
