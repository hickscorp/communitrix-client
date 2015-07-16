package fr.pierreqr.communitrix.networking.cmd.beans;

public class PlayerBean {
  public    String    uuid;
  public    String    username;
  public    int       level;
  
  public PlayerBean set (final PlayerBean player) {
    uuid      = player.uuid;
    username  = player.username;
    level     = player.level;
    return    this;
  }
}
