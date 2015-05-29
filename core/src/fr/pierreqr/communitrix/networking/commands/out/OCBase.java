package fr.pierreqr.communitrix.networking.commands.out;

import java.util.Formatter;

import com.badlogic.gdx.utils.Json;

// This is the base class for any outgoing command.
public abstract class OCBase {
  public final static Json      encoder   = new Json();
  public final static Formatter formatter = new Formatter();

  public final int code;
  public OCBase (final int c) {
    code    = c;
  }

  public byte[] toJson () {
    return formatter.format ("%s\n", encoder.toJson(new OCJoinCombat("CBT1"))).toString().getBytes();
  }
}
