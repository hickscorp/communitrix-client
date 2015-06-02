package fr.pierreqr.communitrix.networking.commands.rx;

public class RXError extends RXBase {
  public    int       code;
  public    String    reason;
  
  public RXError () {
    super   (Rx.Error);
  }
}
