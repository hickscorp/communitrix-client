package fr.pierreqr.communitrix.networking.cmd.tx;

public class TXRegister extends TXBase {
  public    String    username;
  public TXRegister (final String username) {
    super           (Type.Register);
    this.username   = username;
  }
}
