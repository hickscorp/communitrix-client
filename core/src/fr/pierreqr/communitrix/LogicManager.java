package fr.pierreqr.communitrix;

import fr.pierreqr.communitrix.modelTemplaters.ModelTemplater;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class LogicManager {
  // Constants.
  public  static final  Vector3       CELL_DIMENSIONS   = new Vector3(5, 5, 5);
  public  static final  float         TRANSLATION_SPEED = 20.0f;
  public  static final  float         ROTATION_SPEED    = 120.0f;

  // Our singleton instance.
  private static  LogicManager                    instance        = null;
  // Where our models will be cached.
  private         ModelBuilder                    modelBuilder    = null;
  private         HashMap<String, ModelTemplater> modelTemplaters = new HashMap<String, ModelTemplater>();
  private         HashMap<String, Model>          models          = new HashMap<String, Model>();
  
  // This protected constructor only exists to forbid direct instantiation.
  protected LogicManager () {
    Gdx.app.debug("LogicManager", "Constructing a new logic manager...");
    modelBuilder            = new ModelBuilder();
  }
  // Getters / Setters.
  public ModelBuilder getModelBuilder () {
    return modelBuilder;
  }
  public ModelBuilder setModelBuilder (final ModelBuilder otherModelBuilder) {
    return modelBuilder = otherModelBuilder;
  }
  // Cache clearing methods.
  public void clearCaches () {
    // Get rid of all cached models.
    for (final String identifier : models.keySet())
      models.remove(identifier).dispose();
    // Get rid of all model templaters.
    for (final String identifier : modelTemplaters.keySet())
      modelTemplaters.remove(identifier).dispose();
  }
  
  // Main getter on instance.
  public static LogicManager getInstance() {
     return instance==null ? ( instance = new LogicManager() ) : instance;
  }
  public void dispose () {
    Gdx.app.debug("LogicManager", "Disposing of internal objects...");
    clearCaches();
    // Get rid of the singleton instance.
    instance              = null;
  }
  
  public void registerModelTemplater (final String identifier, final ModelTemplater modelTemplater) {
    modelTemplaters.put(identifier, modelTemplater);
  }
  public Model getModel (final String identifier) {
    // Get the requested model from our cache.
    Model     mdl     = models.get(identifier);
    // The model was not found in our cache.
    if (mdl==null) {
      Gdx.app.debug("LogicManager", "Getting templater for model " + identifier + "...");
      // Let's find if we have a templater matching the identifier.
      final ModelTemplater templater   = modelTemplaters.get(identifier);
      // Templater found, use it.
      if ( templater!=null ) {
        Gdx.app.debug("LogicManager", "Templater found, building model " + identifier + ". This should happend only once!");
        models.put(identifier, mdl = templater.build(modelBuilder));
      }
      // No templater found, log this as an error.
      else
        Gdx.app.error("LogicManager", "A call to getModel was made with an unknown templater identifier: " + identifier + ".");
    }
    return mdl;
  }
}
