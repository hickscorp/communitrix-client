package fr.pierreqr.communitrix.screens;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.utils.Array;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.MotionBlur;

import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.CameraAccessor;
import fr.pierreqr.communitrix.gameObjects.FuelCell;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.GameObjectAccessor;
import fr.pierreqr.communitrix.networking.commands.in.ICCombatStart;
import fr.pierreqr.communitrix.networking.commands.out.OCJoinCombat;

public class CombatScreen implements Screen {
  // This is the configuration class for this screen.
  public class Configuration {
    public final    String    combatUUID;
    public final    int       minPlayers, maxPlayers, playerCount;
    public Configuration (ICCombatStart command) {
      combatUUID    = command.uuid;
      minPlayers    = command.minPlayers;
      maxPlayers    = command.maxPlayers;
      playerCount   = maxPlayers;
    }
  }
  
  // Those are private members.
  private final Communitrix           ctx;
  private       Configuration         config            = null;
  private       TweenManager          tweener           = null;
  private       Environment           envMain           = null;
  private       PerspectiveCamera     camMain           = null;
  private       PostProcessor         postProMain       = null;
  private       Model                 envModel          = null;
  private       FuelCell              myFuelCell        = null;
  private final Array<FuelCell>       fuelCells         = new Array<FuelCell>();
  private final Array<GameObject>     instances         = new Array<GameObject>();
  
  public CombatScreen (final Communitrix communitrix) {
    // Cache our game instance.
    ctx                   = communitrix;
    
    // Initialize tweening engine.
    Tween.registerAccessor(GameObject.class, new GameObjectAccessor());
    Tween.registerAccessor(Camera.class, new CameraAccessor());
    tweener               = new TweenManager();
    
    // Set up the scene environment.
    envMain               = new Environment();
    envMain.set           (new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1.0f));
    envMain.set           (new ColorAttribute(ColorAttribute.Fog, 0.01f, 0.01f, 0.01f, 1.0f));
    envMain.add           (new DirectionalLight().set(new Color(0.6f, 0.6f, 0.6f, 1.0f), -1f, -0.8f, -0.2f));

    // Set up the main post-processor.
    postProMain           = new PostProcessor(true, true, true);
    if (ctx.applicationType!=ApplicationType.WebGL) {
      // Add bloom to post-processor.
      Bloom blm             = new Bloom(ctx.viewWidth/3, ctx.viewHeight/3);
      blm.setBloomIntesity  (0.6f);
      blm.setBloomSaturation(0.7f);
      postProMain.addEffect (blm);
      // Add motion blur to post-processor.
      MotionBlur blur       = new MotionBlur();
      blur.setBlurOpacity   (0.70f);
      postProMain.addEffect (blur);
    }

    // Set up our main camera, and position it.
    camMain               = new PerspectiveCamera(90, ctx.viewWidth, ctx.viewHeight);
    camMain.position.set  (-5, 3, 5);
    camMain.near          = 1f;
    camMain.far           = 150f;
    camMain.lookAt        (0, 0, 0);
    camMain.update        ();
  }
  
  // Configure this screen.
  public boolean reconfigure (final Configuration c) {
    hide            ();
    config          = c;
    show            ();
    return          true;
  }

  // Whenever this screen will temporarilly not be used anymore...
  @Override public void show () {
    // No configuration for this screen yet... Don't show it.
    if (config==null) return;
    
    // Read environment model.
    if (envModel==null)
      envModel        = ctx.modelLoader.loadModel(Gdx.files.internal("models/interior.g3db"));
    
    // Instanciate environment.
    if (instances.size==0) {
      GameObject      envInst   = new GameObject(envModel);
      envInst.transform.rotate  (1, 0, 0, -90);
      envInst.transform.scale   (3, 3, 3);
      instances.add             (envInst);
  
      // Create fuel cell.
      if (myFuelCell==null) {
        myFuelCell      = new FuelCell(5, 5, 5, config.playerCount, true);
        fuelCells.add   (myFuelCell);
        instances.add   (myFuelCell);
      }
    }
  }
  // Whenever this screen becomes available for showing again...
  @Override public void hide () {
    // Remove all fuel cells.
    if (fuelCells.size!=0) {
      for (FuelCell fc : fuelCells)
        fc.dispose      ();
      fuelCells.clear   ();
      myFuelCell        = null;
    }
    // Remove all instances except our character.
    if (instances.size!=0)
      instances.clear   ();
    // Clear environment model.
    if (envModel!=null) {
      envModel.dispose  ();
      envModel          = null;
    }
  }
  @Override public void pause () {
    hide                ();
  }
  @Override public void resume () {
    show                ();
  }
  @Override public void dispose () {
    // Most of the dispose code can be achieved just by hidding.
    hide                ();
    // This should get rid of internal effects as well.
    postProMain.dispose ();
  }
  
  // Main rendering method.
  @Override public void render (final float delta) {
    // Clear viewport etc.
    Gdx.gl.glViewport(0, 0, ctx.viewWidth, ctx.viewHeight);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    // Enable alpha blending.
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    // Enable back-face culling.
    Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    Gdx.gl.glCullFace(GL20.GL_BACK);
    
    // Update any pending tweening.
    tweener.update        (delta);
    camMain.update        ();
    camMain.lookAt        (0, 0, 0);
    
    // Capture FBO for post-processing.
    postProMain.capture();
    
    // Mark the beginning of our rendering phase.
    ctx.modelBatch.begin(camMain);
    // Render all instances in our batch array.
    for (final GameObject instance : instances)
      if (instance.isVisible(camMain))
        ctx.modelBatch.render(instance, envMain);
    // Rendering is over.
    ctx.modelBatch.end();
    
    // Apply post-processing.
    postProMain.render();
  }
  
  @Override public void resize (final int width, final int height) {
    // If a post-processor exists, update it.
    if (postProMain!=null)
      postProMain.rebind();
  }
}
