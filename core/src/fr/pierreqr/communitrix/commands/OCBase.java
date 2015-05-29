package fr.pierreqr.communitrix.commands;

import java.util.Formatter;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

// This is the base class for any outgoing command.
public abstract class OCBase {
  public final static Formatter formatter = new Formatter();
  public final int code;
  public byte[] toJson () {
    Json      encoder       = new Json();
    encoder.setOutputType   (OutputType.json);
    return formatter.format ("%s\n", encoder.toJson(new OCJoinCombat("CBT1"))).toString().getBytes();
  }
  public OCBase (final int c) {
    code    = c;
  }
}
