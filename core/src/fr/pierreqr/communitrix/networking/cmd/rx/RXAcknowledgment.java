package fr.pierreqr.communitrix.networking.cmd.rx;

public class RXAcknowledgment extends RXBase {
  public    String        serial;
  public    boolean       valid;
  public    String        errorMessage;
  
  public RXAcknowledgment () {
    super   (Type.Acknowledgment);
  }
}
