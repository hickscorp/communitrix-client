package fr.pierreqr.communitrix.networking.cmd.rx;

import java.util.HashMap;

import com.fasterxml.jackson.core.type.TypeReference;

public class RXBase {
  public enum Type {
    Connected,          // Fake command, emited whenever we connect.
    Disconnected,       // Fake command, emited whenever we disconnect.
    Error,              // Error notification.
    Acknowledgment,     // Acceptation / Deny of a previous command.
    Welcome,            // Server welcome.
    Registered,         // Received upon successful registration.
    CombatList,         // List of combats.
    CombatJoin,         // Order to join a combat.
    CombatPlayerJoined, // Another player has joined.
    CombatPlayerLeft,   // Another player has joined.
    CombatStart,        // Starts a combat.
    CombatNewTurn,      // Starts a new combat turn.
    CombatPlayerTurn,   // A player has played his turn.
    CombatEnd;          // The combat has ended.

    // Type references map.
    private final static  HashMap<RXBase.Type,TypeReference<?>> TypesMap; static {
      TypesMap      = new HashMap<RXBase.Type,TypeReference<?>>();
      TypesMap.put  (Type.Error,              new TypeReference<RXError>(){});
      TypesMap.put  (Type.Acknowledgment,     new TypeReference<RXAcknowledgment>(){});
      TypesMap.put  (Type.Welcome,            new TypeReference<RXWelcome>(){});
      TypesMap.put  (Type.Registered,         new TypeReference<RXRegistered>(){});
      TypesMap.put  (Type.CombatList,         new TypeReference<RXCombatList>(){});
      TypesMap.put  (Type.CombatJoin,         new TypeReference<RXCombatJoin>(){});
      TypesMap.put  (Type.CombatPlayerJoined, new TypeReference<RXCombatPlayerJoined>(){});
      TypesMap.put  (Type.CombatPlayerLeft,   new TypeReference<RXCombatPlayerLeft>(){});
      TypesMap.put  (Type.CombatStart,        new TypeReference<RXCombatStart>(){});
      TypesMap.put  (Type.CombatNewTurn,      new TypeReference<RXCombatNewTurn>(){});
      TypesMap.put  (Type.CombatPlayerTurn,   new TypeReference<RXCombatPlayerTurn>(){});
      TypesMap.put  (Type.CombatEnd,          new TypeReference<RXCombatEnd>(){});
    }
    public TypeReference<?> toTypeReference () {
      return TypesMap.get(this);
    }
  }

  public    RXBase.Type    type;
  public RXBase (final Type type) {
    this.type   = type;
  }
}
