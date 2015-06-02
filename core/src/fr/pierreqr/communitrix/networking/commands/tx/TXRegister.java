package fr.pierreqr.communitrix.networking.commands.tx;

public class TXRegister extends TXBase {
  public    String    username;
  public TXRegister (final String username) {
    super           (Tx.Register);
    this.username   = username;
  }
}
