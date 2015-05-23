package fr.pierreqr.communitrix;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.bitfire.utils.ShaderLoader;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.MotionBlur;

import fr.pierreqr.communitrix.modelTemplaters.CubeModelTemplater;

public class Communitrix extends ApplicationAdapter {
  // Constants.
  public  static final  Vector3       CELL_DIMENSIONS   = new Vector3(5, 5, 5);
  public  static final  float         TRANSLATION_SPEED = 20.0f;
  public  static final  float         ROTATION_SPEED    = 120.0f;

  // Main logic manager instance is cached here.
  private       LogicManager          logicManager;
  // FPS logging class.
  private       FPSLogger             lgrFps;
  // Scene setup related objects.
  private       Environment           envMain;
  private       PerspectiveCamera     camMain;
  private       CameraInputController camCtrlMain;
  private       PostProcessor         postProMain;
  private       ModelBatch            mdlBtchMain;
  // Various object instances.
  private       GameObject            mdlInstCharacter;
  private final Array<GameObject>     instances         = new Array<GameObject>();
  // Caches.
  private       int                   viewWidth, viewHeight;

  // Flat UI related members.
  private       Stage                 uiStage;
  private       Skin                  uiSkin;
  private       Label                 lblFPS;

  // Those are temporaries.
  private       float                 tmpFloat;

  @Override public void create () {
    // After starting the application, we can query for the desktop dimensions
    //if (Gdx.app.getType()==ApplicationType.Desktop) {
    //  final DisplayMode dm    = Gdx.graphics.getDesktopDisplayMode();
    //  Gdx.graphics.setDisplayMode   (dm.width, dm.height, true);
    //}
    // Cache viewport size.
    resize                (Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    // Configure assets etc.
    ShaderLoader.BasePath = "../android/assets/shaders/";
    // Set up our FPS logging object.
    lgrFps                = new FPSLogger();
    initBusinessLogic     ();   // General business-related logic initialization.
    initEnvironment       ();   // Environment dedicated initializer.
    initPostProcessing    ();   // Post-processing dedicated initializer.
    initCamera            ();   // Camera / Camera controller dedicated initializer.
    initModelsAndInstances();   // Models / Instances dedicated initializer.
    initFlatUI            ();   // Flat UI initializer.
  }
  private void initBusinessLogic () {
    logicManager                        = LogicManager.getInstance();
    logicManager.registerModelTemplater ("Cube", new CubeModelTemplater());
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
    Bloom blm             = new Bloom(viewWidth/3, viewHeight/3);
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
    camMain               = new PerspectiveCamera(67, viewWidth, viewHeight);
    camMain.position.set  (25, 25, 25);
    camMain.near          = 1f;
    camMain.far           = 150f;
    camMain.lookAt        (0, 0, 0);
    camMain.update        ();
    // Attach a camera controller to the main camera, set it as the main processor.
    camCtrlMain           = new CameraInputController(camMain);
    Gdx.input.setInputProcessor(camCtrlMain);
  }
  private void initModelsAndInstances () {
    // This is the main model rendering batch.
    mdlBtchMain         = new ModelBatch();
    // Cache our cube model.
    Model   mdlCube     = logicManager.getModel("Cube");
    
    // Prepare the character model...
    mdlInstCharacter    = new GameObject(logicManager.getModel("Cube"));
    // As our character model will be rendered with everything else, add it to our instances array.
    instances.add       (mdlInstCharacter);
    
    // Prepare a blending attribute for our cubes.
    BlendingAttribute alphaBlend  = new BlendingAttribute();
    // Create an array of cube for testing.
    int   iTrans, jTrans;
    float halfWidth     = CELL_DIMENSIONS.x*5/2.0f;
    float halfHeight    = CELL_DIMENSIONS.y*5/2.0f;
    float halfDepth     = CELL_DIMENSIONS.z*5/2.0f;
    for (int i = 0; i<CELL_DIMENSIONS.x; ++i) {
      iTrans  = i * 5;
      for (int j = 0; j<CELL_DIMENSIONS.y; ++j) {
        jTrans = j * 5;
        for (int k = 0; k<CELL_DIMENSIONS.z; ++k) {
          // Create a new cube instance and position it.
          GameObject instance = new GameObject(mdlCube);
          instance.transform.setToTranslation(iTrans-halfWidth, jTrans-halfHeight, k*5-halfDepth);
          // Because our model might have different materials, reset them all to a diffuse color.
          for (Material mat : instance.materials)
            mat.set(
                ColorAttribute.createDiffuse(
                    1.0f/CELL_DIMENSIONS.x*i,
                    1.0f/CELL_DIMENSIONS.y*j,
                    1.0f/CELL_DIMENSIONS.z*k,
                    0.70f
                ), alphaBlend
          );
          instances.add(instance);
        }
      }
    }
  }
  private void initFlatUI () {
    // Load the flat UI skin.
    uiSkin                      = new Skin(Gdx.files.local("../android/assets/skins/uiskin.json"));
    // Prepare the flat UI stage, and set it as first responder.
    uiStage                     = new Stage();
    // Create the FPS label and place it on stage.
    lblFPS                      = new Label("FPS:", uiSkin);
    lblFPS.setPosition          (5, 5);
    lblFPS.setColor             (Color.WHITE);
    uiStage.addActor            (lblFPS);
  }

  @Override public void dispose () {
    postProMain.dispose ();
    mdlBtchMain.dispose ();
    logicManager.dispose();
  }

  @Override public void render () {
    // Clear viewport etc.
    Gdx.gl.glViewport(0, 0, viewWidth, viewHeight);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    // Enable alpha blending.
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    // Enable back-face culling.
    Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    Gdx.gl.glCullFace(GL20.GL_BACK);
    
    // Process user inputs.
    handleInputs();
    
    // Capture FBO for post-processing.
    postProMain.capture();
    // Mark the beginning of our rendering phase.
    mdlBtchMain.begin(camMain);
    // Render all instances in our batch array.
    for (final GameObject instance : instances)
      if (instance.isVisible(camMain))
        mdlBtchMain.render(instance, envMain);
    // Rendering is over.
    mdlBtchMain.end();
    // Apply post-processing.
    postProMain.render();
    
    // Update flat UI.
    lblFPS.setText    ("FPS: " + Gdx.graphics.getFramesPerSecond());
    uiStage.draw      ();
    
    // Log our FPS count to the console.
    //lgrFps.log();
  }
  private void handleInputs () {
    // Store elapsed delta.
    tmpFloat            = Gdx.graphics.getDeltaTime();
    
    // Update camera controller.
    camCtrlMain.update  ();

    // Move character forward / backward events.
    if (Gdx.input.isKeyPressed(Input.Keys.UP))
      mdlInstCharacter.transform.translate(TRANSLATION_SPEED * tmpFloat, 0, 0);
    else if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
      mdlInstCharacter.transform.translate(-TRANSLATION_SPEED * tmpFloat, 0, 0);
    
    // Character rotation relative to camera on X axis.
    if (Gdx.input.isKeyPressed(Input.Keys.I))
      mdlInstCharacter.relativeRotate(camMain, Vector3.X, -ROTATION_SPEED * tmpFloat);
    else if (Gdx.input.isKeyPressed(Input.Keys.K))
      mdlInstCharacter.relativeRotate(camMain, Vector3.X,  ROTATION_SPEED * tmpFloat);
    // Character rotation relative to camera on Y axis.
    if (Gdx.input.isKeyPressed(Input.Keys.J))
      mdlInstCharacter.relativeRotate(camMain, Vector3.Y, -ROTATION_SPEED * tmpFloat);
    else if (Gdx.input.isKeyPressed(Input.Keys.L))
      mdlInstCharacter.relativeRotate(camMain, Vector3.Y,  ROTATION_SPEED * tmpFloat);
    
    // Left / Right events.
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
      mdlInstCharacter.transform.rotate(Vector3.Y,  ROTATION_SPEED * tmpFloat);
    else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
      mdlInstCharacter.transform.rotate(Vector3.Y, -ROTATION_SPEED * tmpFloat);

    // Attach / Detach event.
    if (Gdx.input.isKeyPressed(Input.Keys.M) && !mdlInstCharacter.nodes.get(0).hasChildren()) {
      // Prepare an uninitialized model instance pointer.
      ModelInstance mdlInst   = null;
      // Cache our cube model.
      final Model   mdlCube   = logicManager.getModel("Cube");
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
  
  // Occurs whenever the viewport size changes.
  @Override public void resize (int width, int height) {
    viewWidth         = width;
    viewHeight        = height;
  }
  
  // Occurs whenever the application is paused (Eg enters background, etc).
  @Override public void pause () {
  }
  // Transitions between pause and normal mode.
  @Override public void resume () {
    resize              (Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    postProMain.rebind  ();
  }
}
