package fr.pierreqr.communitrix.commands;

import java.util.Formatter;

import com.badlogic.gdx.utils.Json;

// This is the base class for any outgoing command.
public abstract class OCBase {
  public final static Formatter formatter = new Formatter();
  public final static Json      encoder   = new Json();
  public final int code;
  public byte[] toJson () {
    return formatter.format("%s", encoder.toJson(new OCJoinCombat("CBT1"))).toString().getBytes();
  }
  public OCBase (final int c) {
    code    = c;
  }
}
