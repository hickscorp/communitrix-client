package fr.pierreqr.communitrix.networking.commands.tx;

public class TXBase {
  public enum Tx {
    Register,           // Register to the server.
    CombatList,         // Asks for a list of combats.
    CombatJoin,         // Asks to join a combat.
    CombatLeave,        // Asks to leave a combat.
    CombatPlayTurn      // Plays a turn.
  }
  public    TXBase.Tx    type;
  public TXBase (final Tx type) {
    this.type   = type;
  }
}
