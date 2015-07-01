package fr.pierreqr.communitrix.networking.shared;

import java.util.ArrayList;

public class SHCombat {
  public    String              uuid;
  public    int                 minPlayers, maxPlayers;
  public    boolean             started;
  public    int                 currentTurn;
  public    ArrayList<SHPlayer> players;
}
