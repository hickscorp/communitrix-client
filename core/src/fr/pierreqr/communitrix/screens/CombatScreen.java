package fr.pierreqr.communitrix.screens;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.MotionBlur;

import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.commands.out.OCJoinCombat;
import fr.pierreqr.communitrix.gameObjects.FuelCell;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.GameObjectAccessor;

public class CombatScreen implements Screen {
  public final static int   MODE_VIEW_HOLOGRAM    = 1;
  public final static int   MODE_MODELING         = 2;
  public final static int   WAITING               = 4;
  
  private       Environment           envMain;
  private       PerspectiveCamera     camMain;
  private       CameraInputController camCtrlMain;
  private       PostProcessor         postProMain;
  public        TweenManager          tweener;
  // Various object instances.
  public        GameObject            mdlInstCharacter;
  private final Array<FuelCell>       fuelCellInstances = new Array<FuelCell>();
  private       int                   randomizeId       = -1;
  private final Array<GameObject>     instances         = new Array<GameObject>();
  // UI Components.
  private       Label                 lblInstructions, lblFPS;
  
  // Game instance cache.
  private final Communitrix           communitrix;
  
  // Temporaries.
  private static int                  visibleCount;
  
  public CombatScreen (final Communitrix communitrixInstance) {
    // Cache our game instance.
    communitrix           = communitrixInstance;
    initTweening          ();   // Set up the motion tween engine.
    initEnvironment       ();   // Environment dedicated initializer.
    initPostProcessing    ();   // Post-processing dedicated initializer.
    initCamera            ();   // Camera / Camera controller dedicated initializer.
    initFlatUI            ();   // Flat UI initializer.
    initModelsAndInstances();   // Models / Instances dedicated initializer.
  }
  private void initTweening () {
    Tween.registerAccessor(GameObject.class, new GameObjectAccessor());
    tweener               = new TweenManager();
  }
  private void initEnvironment () {
    // Set up the scene environment.
    envMain               = new Environment();
    envMain.set           (new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1.0f));
    envMain.set           (new ColorAttribute(ColorAttribute.Fog, 0.01f, 0.01f, 0.01f, 1.0f));
    envMain.add           (new DirectionalLight().set(Color.WHITE, -1f, -0.8f, -0.2f));
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
    camMain.position.set  (25, 25, 25);
    camMain.near          = 1f;
    camMain.far           = 150f;
    camMain.lookAt        (0, 0, 0);
    camMain.update        ();
    // Attach a camera controller to the main camera, set it as the main processor.
    camCtrlMain           = new CameraInputController(camMain);
  }
  private void initModelsAndInstances () {
    // Prepare the character model...
    mdlInstCharacter    = new GameObject(communitrix.getModel("Cube"));
    // As our character model will be rendered with everything else, add it to our instances array.
    instances.add       (mdlInstCharacter);
  }
  private void initFlatUI () {
    // Create the FPS label and place it on stage.
    lblFPS                        = new Label("", communitrix.uiSkin);
    lblFPS.setColor               (Color.WHITE);
    lblInstructions               = new Label("Press ESC to change screen.", communitrix.uiSkin);
    lblInstructions.setColor      (Color.ORANGE);
  }
  
  @Override public void show () {
    // Set the input controller.
    Gdx.input.setInputProcessor(camCtrlMain);
    
//    // Cache our cube model.
//    Model   mdlCube     = communitrix.getModel("Cube");
//    // Prepare a blending attribute for our cubes.
//    BlendingAttribute alphaBlend  = new BlendingAttribute();
//    // Create an array of cube for testing.
//    int   iTrans, jTrans;
//    float halfWidth     = Communitrix.CELL_DIMENSIONS.x*5/2.0f;
//    float halfHeight    = Communitrix.CELL_DIMENSIONS.y*5/2.0f;
//    float halfDepth     = Communitrix.CELL_DIMENSIONS.z*5/2.0f;
//    for (int i = 0; i<Communitrix.CELL_DIMENSIONS.x; ++i) {
//      iTrans  = i * 5;
//      for (int j = 0; j<Communitrix.CELL_DIMENSIONS.y; ++j) {
//        jTrans = j * 5;
//        for (int k = 0; k<Communitrix.CELL_DIMENSIONS.z; ++k) {
//          // Create a new cube instance and position it.
//          GameObject instance = new GameObject(mdlCube);
//          instance.transform.setToTranslation(iTrans-halfWidth, jTrans-halfHeight, k*5-halfDepth);
//          // Because our model might have different materials, reset them all to a diffuse color.
//          for (Material mat : instance.materials)
//            mat.set(
//                ColorAttribute.createDiffuse(
//                    1.0f/Communitrix.CELL_DIMENSIONS.x*i,
//                    1.0f/Communitrix.CELL_DIMENSIONS.y*j,
//                    1.0f/Communitrix.CELL_DIMENSIONS.z*k,
//                    0.70f
//                ), alphaBlend
//          );
//          instances.add(instance);
//        }
//      }
//    }
    
//    Random  rnd   = new Random();
//    // Test our fuel cell.
//    for (int x = 0; x < 10; x ++)
//      for (int y = 0; y < 10; y ++)
//        for (int z = 0; z < 10; z ++) {
//          FuelCell    another     = new FuelCell(5, 5, 5, rnd.nextInt(4)+1, true);
//          another.transform.translate(new Vector3(x * 6, y * 6, z * 6));
//          fuelCellInstances.add   (another);
//          instances.add           (another);
//        }

    
    // Put our label on stage.
    communitrix.uiStage.addActor  (lblFPS);
    communitrix.uiStage.addActor  (lblInstructions);
  }
  @Override public void hide () {
    // Remove all fuel cells.
    for (FuelCell fc : fuelCellInstances)
      fc.dispose();
    fuelCellInstances.clear();
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
    
    // Update any pending tweening.
    tweener.update        (delta);
    
    // Process user inputs.
    handleInputs(delta);
    if (randomizeId>-1) {
      if (fuelCellInstances.size>0) {
        FuelCell      fc  = fuelCellInstances.get(randomizeId);
        fc.randomize      ();
      }
      randomizeId++;
      if (randomizeId>=fuelCellInstances.size)
        randomizeId     = -1;
    }
    
    // Capture FBO for post-processing.
    postProMain.capture();
    // Mark the beginning of our rendering phase.
    communitrix.modelBatch.begin(camMain);
    // Render all instances in our batch array.
    visibleCount  = 0;
    for (final GameObject instance : instances)
      if (instance.isVisible(camMain)) {
        visibleCount++;
        communitrix.modelBatch.render(instance, envMain);
      }
    // Rendering is over.
    communitrix.modelBatch.end();
    // Apply post-processing.
    postProMain.render();
    
    // Update flat UI.
    lblFPS.setText            ("Combat FPS: " + Gdx.graphics.getFramesPerSecond() + ", Visible Objects: " + visibleCount);
    communitrix.uiStage.act   (delta);
    communitrix.uiStage.draw  ();
  }
  private void handleInputs (final float delta) {
    // Update camera controller.
    camCtrlMain.update  ();
    
    // Randomize fuel cell.
    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER))
      randomizeId   = 0;
    
    // Screen change.
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      communitrix.networkingManager.send(new OCJoinCombat("CBT1"));
      communitrix.combatScreenRequestingExit();
    }
    
    // Move character forward / backward events.
    if (Gdx.input.isKeyPressed(Input.Keys.UP))
      mdlInstCharacter.transform.translate(Communitrix.TRANSLATION_SPEED * delta, 0, 0);
    else if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
      mdlInstCharacter.transform.translate(-Communitrix.TRANSLATION_SPEED * delta, 0, 0);
    
    // Character rotation relative to camera on X axis.
    if (Gdx.input.isKeyPressed(Input.Keys.I))
      mdlInstCharacter.relativeRotate(camMain, Vector3.X, -Communitrix.ROTATION_SPEED * delta);
    else if (Gdx.input.isKeyPressed(Input.Keys.K))
      mdlInstCharacter.relativeRotate(camMain, Vector3.X,  Communitrix.ROTATION_SPEED * delta);
    // Character rotation relative to camera on Y axis.
    if (Gdx.input.isKeyPressed(Input.Keys.J))
      mdlInstCharacter.relativeRotate(camMain, Vector3.Y, -Communitrix.ROTATION_SPEED * delta);
    else if (Gdx.input.isKeyPressed(Input.Keys.L))
      mdlInstCharacter.relativeRotate(camMain, Vector3.Y,  Communitrix.ROTATION_SPEED * delta);
    
    // Left / Right events.
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
      mdlInstCharacter.transform.rotate(Vector3.Y,  Communitrix.ROTATION_SPEED * delta);
    else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
      mdlInstCharacter.transform.rotate(Vector3.Y, -Communitrix.ROTATION_SPEED * delta);

    // Attach / Detach event.
    if (Gdx.input.isKeyPressed(Input.Keys.M) && !mdlInstCharacter.nodes.get(0).hasChildren()) {
      // Prepare an uninitialized model instance pointer.
      ModelInstance mdlInst   = null;
      // Cache our cube model.
      final Model   mdlCube   = communitrix.getModel("Cube");
      // Prepare the green cube...
      mdlInst                 = new ModelInstance(mdlCube);
      for (final Material mtl : mdlInst.materials)
        mtl.set(ColorAttribute.createDiffuse(Color.PURPLE));
      mdlInstCharacter.attachAt(mdlInst.nodes.get(0), 0.0f, 2.0f, 0.0f);
      // Prepare the red cube.
      mdlInst                 = new ModelInstance(mdlCube);
      for (final Material mtl : mdlInst.materials)
        mtl.set(ColorAttribute.createDiffuse(Color.ORANGE));
      mdlInstCharacter.attachAt(mdlInst.nodes.get(0), 2.0f, 0.0f, 0.0f);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.N) && mdlInstCharacter.nodes.get(0).hasChildren())
      mdlInstCharacter.detachAllNodes();
  }
  
  @Override public void resize (final int width, final int height) {
    // If a post-processor exists, update it.
    if (postProMain!=null)
      postProMain.rebind();
    // Place flat UI.
    lblFPS.setPosition            (5, 15);
    lblInstructions.setPosition   (5, communitrix.viewHeight - 20);
  }

  @Override public void pause () {
  }
  @Override public void resume () {
  }
}
