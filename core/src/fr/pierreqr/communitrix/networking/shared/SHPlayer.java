package fr.pierreqr.communitrix.networking.shared;

public class SHPlayer {
  public    String    uuid;
  public    String    username;
  public    int       level;
  
  public SHPlayer set (final SHPlayer player) {
    uuid      = player.uuid;
    username  = player.username;
    level     = player.level;
    return    this;
  }
}
