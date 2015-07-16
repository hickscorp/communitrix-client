package fr.pierreqr.communitrix.networking.cmd.rx;

public class RXError extends RXBase {
  public    int       code;
  public    String    reason;
  
  public RXError () {
    super   (Type.Error);
  }
}
