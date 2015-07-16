package fr.pierreqr.communitrix.networking.cmd.beans;

import java.util.ArrayList;

public class CombatBean {
  public    String              uuid;
  public    int                 minPlayers, maxPlayers;
  public    boolean             started;
  public    int                 currentTurn;
  public    ArrayList<PlayerBean> players;
  
  public CombatBean set (final CombatBean combat) {
    uuid            = combat.uuid;
    minPlayers      = combat.minPlayers;
    maxPlayers      = combat.maxPlayers;
    started         = combat.started;
    players.clear   ();
    players.addAll  (combat.players);
    return          this;
  }
  
  public String toString () {
    return String.format("%s with %d over %d to %d players.", uuid, players.size(), minPlayers, maxPlayers);
  }
}
