package fr.pierreqr.communitrix.networking.cmd.tx;

public class TXBase {
  public enum Type {
    Register,           // Register to the server.
    CombatList,         // Asks for a list of combats.
    CombatJoin,         // Asks to join a combat.
    CombatLeave,        // Asks to leave a combat.
    CombatPlayTurn      // Plays a turn.
  }
  public    TXBase.Type    type;
  public    String       serial;
  public TXBase (final Type type) {
    this.type   = type;
  }
}
