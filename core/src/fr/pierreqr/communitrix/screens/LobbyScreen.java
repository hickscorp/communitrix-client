package fr.pierreqr.communitrix.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.MotionBlur;

import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.GameObject;

public class LobbyScreen implements Screen {

  // Scene setup related objects.
  private       Environment           envMain;
  private       PerspectiveCamera     camMain;
  private       CameraInputController camCtrlMain;
  private       PostProcessor         postProMain;
  // Various object instances.
  private       GameObject            mdlInstCharacter;
  private       GameObject            mdlInstSphere;
  private final Array<GameObject>     instances         = new Array<GameObject>();
  
  private final Communitrix           communitrix;
  
  private       Label                 lblFPS;
  
  public LobbyScreen (final Communitrix communitrixInstance) {
    // Cache our game instance.
    communitrix           = communitrixInstance;
    // Set up our FPS logging object.
    initEnvironment       ();   // Environment dedicated initializer.
    initPostProcessing    ();   // Post-processing dedicated initializer.
    initCamera            ();   // Camera / Camera controller dedicated initializer.
    initModelsAndInstances();   // Models / Instances dedicated initializer.
    initFlatUI            ();   // Flat UI initializer.
  }
  private void initEnvironment () {
    // Set up the scene environment.
    envMain               = new Environment();
    envMain.set           (new ColorAttribute(ColorAttribute.AmbientLight, 0.9f, 0.9f, 0.9f, 1.0f));
    envMain.set           (new ColorAttribute(ColorAttribute.Fog, 0.01f, 0.01f, 0.01f, 1.0f));
  }
  private void initPostProcessing () {
    // Set up the main post-processor.
    postProMain           = new PostProcessor(true, true, true);
    // Add bloom to post-processor.
    Bloom blm             = new Bloom(communitrix.viewWidth/3, communitrix.viewHeight/3);
    blm.setBloomIntesity  (0.7f);
    blm.setBloomSaturation(0.8f);
    postProMain.addEffect (blm);
    // Add motion blur to post-processor.
    MotionBlur blur       = new MotionBlur();
    blur.setBlurOpacity   (0.80f);
    postProMain.addEffect (blur);
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
    lblFPS.setPosition            (5, communitrix.viewHeight - 15);
    lblFPS.setColor               (Color.WHITE);
  }
  
  @Override public void show () {
    // Set the input controller.
    Gdx.input.setInputProcessor(camCtrlMain);
    
    // Cache our cube model.
    Model   mdlSphere   = communitrix.modelBuilder.createSphere(10, 10, 10, 30, 30, new Material(ColorAttribute.createDiffuse(Color.WHITE)), Usage.Position | Usage.Normal);
    // Prepare a blending attribute for our cubes.
    BlendingAttribute alphaBlend  = new BlendingAttribute();
    // Set up our sphere.
    mdlInstSphere       = new GameObject(mdlSphere);
    instances.add       (mdlInstSphere);
    
    // Change sphere materials.
    for (Material mat : mdlInstSphere.materials)
      mat.set(ColorAttribute.createDiffuse(0.5f, 0.5f, 0.5f, 0.7f), alphaBlend);
    
    // Put our label on stage.
    communitrix.uiStage.addActor  (lblFPS);
  }
  @Override public void hide () {
    // Remove all instances except our character.
    instances.removeRange (1, instances.size - 1);
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
    
    // Update flat UI.
    lblFPS.setText            ("Lobby FPS: " + Gdx.graphics.getFramesPerSecond());
    communitrix.uiStage.act   (delta);
    communitrix.uiStage.draw  ();
  }
  private void handleInputs (final float delta) {
    // Update camera controller.
    camCtrlMain.update  ();
    
    // Screen change.
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      communitrix.lobbyScreenRequestingExit();
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
  }

  @Override public void pause () {
  }
  @Override public void resume () {
    Gdx.app.log     ("LobbyScreen", "Resume event.");
  }
}
