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
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.UBJsonReader;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.MotionBlur;

import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.CameraAccessor;
import fr.pierreqr.communitrix.gameObjects.FuelCell;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.GameObjectAccessor;
import fr.pierreqr.communitrix.networking.commands.out.OCJoinCombat;

public class CombatScreen implements Screen {
  public final static int   MODE_VIEW_HOLOGRAM    = 1;
  public final static int   MODE_MODELING         = 2;
  public final static int   WAITING               = 4;
  
  public        Environment           envMain;
  public        PerspectiveCamera     camMain;
  public        CameraInputController camCtrlMain;
  public        PostProcessor         postProMain;
  public        TweenManager          tweener;
  // Various object instances.
  private final Array<FuelCell>       fuelCells         = new Array<FuelCell>();
  private final Array<GameObject>     instances         = new Array<GameObject>();
  
  // Game instance cache.
  private final Communitrix           communitrix;
  
  public CombatScreen (final Communitrix communitrixInstance) {
    // Cache our game instance.
    communitrix           = communitrixInstance;
    initTweening          ();   // Set up the motion tween engine.
    initEnvironment       ();   // Environment dedicated initializer.
    initPostProcessing    ();   // Post-processing dedicated initializer.
    initCamera            ();   // Camera / Camera controller dedicated initializer.
  }
  private void initTweening () {
    Tween.registerAccessor(GameObject.class, new GameObjectAccessor());
    Tween.registerAccessor(Camera.class, new CameraAccessor());
    tweener               = new TweenManager();
  }
  private void initEnvironment () {
    // Set up the scene environment.
    envMain               = new Environment();
    envMain.set           (new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1.0f));
    envMain.set           (new ColorAttribute(ColorAttribute.Fog, 0.01f, 0.01f, 0.01f, 1.0f));
    envMain.add           (new DirectionalLight().set(new Color(0.6f, 0.6f, 0.6f, 1.0f), -1f, -0.8f, -0.2f));
    
    UBJsonReader    reader  = new UBJsonReader();
    G3dModelLoader  loader  = new G3dModelLoader(reader);
    Model           model   = loader.loadModel(Gdx.files.internal("models/interior.g3db"));
    GameObject      inst    = new GameObject(model);
    inst.transform.rotate   (1, 0, 0, -90);
    instances.add           (inst);
  }
  private void initPostProcessing () {
    // Set up the main post-processor.
    postProMain           = new PostProcessor(true, true, true);
    if (communitrix.applicationType!=ApplicationType.WebGL) {
      // Add bloom to post-processor.
      Bloom blm             = new Bloom(communitrix.viewWidth/3, communitrix.viewHeight/3);
      blm.setBloomIntesity  (0.6f);
      blm.setBloomSaturation(0.7f);
      postProMain.addEffect (blm);
      // Add motion blur to post-processor.
      MotionBlur blur       = new MotionBlur();
      blur.setBlurOpacity   (0.70f);
      postProMain.addEffect (blur);
    }
  }
  private void initCamera () {
    // Set up our main camera, and position it.
    camMain               = new PerspectiveCamera(90, communitrix.viewWidth, communitrix.viewHeight);
    camMain.position.set  (-5, 3, 5);
    camMain.near          = 1f;
    camMain.far           = 150f;
    camMain.lookAt        (0, 0, 0);
    camMain.update        ();
    // Attach a camera controller to the main camera, set it as the main processor.
    camCtrlMain           = new CameraInputController(camMain);
  }
  
  @Override public void show () {
    // Set the input controller.
    Gdx.input.setInputProcessor(camCtrlMain);
    
    // Test our fuel cell.
    final int   entropy     = 3;
    for (int x = 0; x < 1; x ++)
      for (int y = 0; y < 1; y ++)
        for (int z = 0; z < 1; z ++) {
          FuelCell    another     = new FuelCell(5, 5, 5, entropy, true);
          another.transform.translate(new Vector3(x * 6, y * 6, z * 6));
          fuelCells.add   (another);
          instances.add           (another);
        }
  }
  @Override public void hide () {
    // Remove all fuel cells.
    for (FuelCell fc : fuelCells)
      fc.dispose();
    fuelCells.clear();
    // Remove all instances except our character.
    if (instances.size>1)
      instances.removeRange(1, instances.size - 1);
  }
  
  @Override public void dispose () {
    postProMain.dispose ();
  }

  @Override public void render (final float delta) {
    // Clear viewport etc.
    Gdx.gl.glViewport(0, 0, communitrix.viewWidth, communitrix.viewHeight);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    // Enable alpha blending.
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    // Enable back-face culling.
    Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    Gdx.gl.glCullFace(GL20.GL_BACK);
    
    // Process user inputs.
    handleInputs(delta);

    // Update any pending tweening.
    tweener.update        (delta);
    camMain.update        ();
    camMain.lookAt        (0, 0, 0);
    
    // Capture FBO for post-processing.
    postProMain.capture();
    
    // Mark the beginning of our rendering phase.
    communitrix.modelBatch.begin(camMain);
    // Render all instances in our batch array.
    for (final GameObject instance : instances)
      if (instance.isVisible(camMain))
        communitrix.modelBatch.render(instance, envMain);
    // Rendering is over.
    communitrix.modelBatch.end();
    
    // Apply post-processing.
    postProMain.render();
  }
  private void handleInputs (final float delta) {
    // Update camera controller.
    camCtrlMain.update  ();
    
    // Screen change.
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      communitrix.networkingManager.send(new OCJoinCombat("CBT1"));
      communitrix.combatScreenRequestingExit();
    }
    
    // Move character forward / backward events.
    if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {}
    else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {}
  }
  
  @Override public void resize (final int width, final int height) {
    // If a post-processor exists, update it.
    if (postProMain!=null)
      postProMain.rebind();
  }

  @Override public void pause () {
  }
  @Override public void resume () {
  }
}
