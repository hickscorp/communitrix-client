package fr.pierreqr.communitrix.commands;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

// This is the base class for any incoming command.
public abstract class ICBase {
  public static ICBase fromJson (final String data) {
    Json   decoder          = new Json();
    decoder.setOutputType   (OutputType.json);
    return decoder.fromJson (null, data);
  }
}
