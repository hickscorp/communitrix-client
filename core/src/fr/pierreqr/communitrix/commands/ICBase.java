package fr.pierreqr.communitrix.commands;

import com.badlogic.gdx.utils.Json;

// This is the base class for any incoming command.
public abstract class ICBase {
  public final static Json   decoder   = new Json();
  public static ICBase fromJson (final String data) {
    return decoder.fromJson(null, data);
  }
}
