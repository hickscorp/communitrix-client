package fr.pierreqr.communitrix.commands;

public class OCJoinCombat extends OCBase {
  // The unique code for this command.
  public static final int     CODE        = 1;
  // The combatUUID to join.
  public final        String  uuid;
  
  // Designated constructor.
  public OCJoinCombat (final String id) {
    super         (CODE);
    uuid          = id;
  }
}
