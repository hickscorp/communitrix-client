package fr.pierreqr.communitrix;

import fr.pierreqr.communitrix.modelTemplaters.ModelTemplater;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class LogicManager {
  // Our singleton instance.
  private static  LogicManager                    instance        = null;
  // Where our models will be cached.
  private         ModelBuilder                    modelBuilder    = null;
  private         HashMap<String, ModelTemplater> modelTemplaters = new HashMap<String, ModelTemplater>();
  private         HashMap<String, Model>          models          = new HashMap<String, Model>();
  
  // This protected constructor only exists to forbid direct instanciation.
  protected LogicManager () {
    modelBuilder          = new ModelBuilder();
  }
  // Main getter on instance.
  public static LogicManager getInstance() {
     return instance==null ? ( instance = new LogicManager() ) : instance;
  }
  
  public void registerModelTemplater (final String identifier, final ModelTemplater modelTemplater) {
    modelTemplaters.put(identifier, modelTemplater);
  }
  public Model getModel (final String identifier) throws Exception {
    // Get the requested model from our cache.
    Model     mdl     = models.get(identifier);
    // The model was not found in our cache.
    if (mdl==null) {
      // Let's find if we have a templater matching the identifier.
      final ModelTemplater templater   = modelTemplaters.get(identifier);
      // No templater found, log this as an error.
      if ( templater==null ) {
        String      msg   = "A call to getModel was made with an unknown templater identifier: " + identifier + ".";
        Gdx.app.error("LogicManager", msg);
        return null;
      }
      mdl     = templater.build(modelBuilder);
      models.put(identifier, mdl);
    }
    return mdl;
  }
}
