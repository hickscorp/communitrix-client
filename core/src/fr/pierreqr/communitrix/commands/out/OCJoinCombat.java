package fr.pierreqr.communitrix.commands.out;

public class OCJoinCombat extends OCBase {
  // The unique code for this command.
  public static final int     CODE        = 2;
  // The combatUUID to join.
  public final        String  uuid;
  
  // Designated constructor.
  public OCJoinCombat (final String id) {
    super         (CODE);
    uuid          = id;
  }
}
