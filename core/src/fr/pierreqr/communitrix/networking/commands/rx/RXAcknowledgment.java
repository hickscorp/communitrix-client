package fr.pierreqr.communitrix.networking.commands.rx;

public class RXAcknowledgment extends RXBase {
  public    String        serial;
  public    boolean       valid;
  
  public RXAcknowledgment () {
    super   (Rx.Acknowledgment);
  }
}
