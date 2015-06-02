package fr.pierreqr.communitrix.networking.commands.rx;

import java.util.HashMap;

import com.fasterxml.jackson.core.type.TypeReference;

public class RXBase {
  public enum Rx {
    Error,              // Error notification.
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
    private final static  HashMap<RXBase.Rx,TypeReference<?>> TypesMap; static {
      TypesMap      = new HashMap<RXBase.Rx,TypeReference<?>>();
      TypesMap.put  (Rx.Error,              new TypeReference<RXError>(){});
      TypesMap.put  (Rx.Error,              new TypeReference<RXError>(){});
      TypesMap.put  (Rx.Welcome,            new TypeReference<RXWelcome>(){});
      TypesMap.put  (Rx.Registered,         new TypeReference<RXRegistered>(){});
      TypesMap.put  (Rx.CombatList,         new TypeReference<RXCombatList>(){});
      TypesMap.put  (Rx.CombatJoin,         new TypeReference<RXCombatJoin>(){});
      TypesMap.put  (Rx.CombatPlayerJoined, new TypeReference<RXCombatPlayerJoined>(){});
      TypesMap.put  (Rx.CombatPlayerLeft,   new TypeReference<RXCombatPlayerLeft>(){});
      TypesMap.put  (Rx.CombatStart,        new TypeReference<RXCombatStart>(){});
      TypesMap.put  (Rx.CombatNewTurn,      new TypeReference<RXCombatNewTurn>(){});
      TypesMap.put  (Rx.CombatPlayerTurn,   new TypeReference<RXCombatPlayerTurn>(){});
      TypesMap.put  (Rx.CombatEnd,          new TypeReference<RXCombatEnd>(){});
    }
    public TypeReference<?> toTypeReference () {
      return TypesMap.get(this);
    }
  }

  public    RXBase.Rx    type;
  public RXBase (final Rx type) {
    this.type   = type;
  }
}
