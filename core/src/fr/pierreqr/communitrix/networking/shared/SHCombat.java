package fr.pierreqr.communitrix.networking.shared;

import java.util.ArrayList;

public class SHCombat {
  public    String              uuid;
  public    int                 minPlayers, maxPlayers;
  public    boolean             started;
  public    int                 currentTurn;
  public    ArrayList<SHPlayer> players;
  
  public SHCombat set (final SHCombat combat) {
    uuid            = combat.uuid;
    minPlayers      = combat.minPlayers;
    maxPlayers      = combat.maxPlayers;
    started         = combat.started;
    players.clear   ();
    players.addAll  (combat.players);
    return          this;
  }
}
