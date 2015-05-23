package fr.pierreqr.communitrix;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.bitfire.utils.ShaderLoader;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.MotionBlur;
import com.bitfire.postprocessing.effects.Nfaa;

public class Communitrix extends ApplicationAdapter {
  // Constants.
  public  static final  Vector3       CELL_DIMENSIONS       = new Vector3(5, 5, 5);
  private static final  float         TRANSLATION_SPEED     = 20.0f;
  private static final  float         ROTATION_SPEED        = 120.0f;
  // Those are temporaries.
  private               float         tmpFloat;

  
  // FPS logging class.
  public        FPSLogger             lgrFps;
  // Scene setup related objects.
  public        Environment           envMain;
  public        PerspectiveCamera     camMain;
  public        CameraInputController camCtrlMain;
  public        PostProcessor         postProMain;
  public        ModelBatch            mdlBtchMain;
  // Main cube model.
  public        Model                 mdlCube;
  // Various object instances.
  public        SimpleCube            mdlInstCharacter;
  public        Node                  nodeCharacter;
  public final  Array<GameObject>     instances   = new Array<GameObject>();
  // Caches.
  private       int                   viewWidth, viewHeight;
  
  @Override
  public void create () {
    // After starting the application, we can query for the desktop dimensions
    if (Gdx.app.getType()==ApplicationType.Desktop) {
      final DisplayMode   dm      = Gdx.graphics.getDesktopDisplayMode();
      Gdx.graphics.setDisplayMode (dm.width, dm.height, true);
    }

    // Configure assets etc.
    ShaderLoader.BasePath = "../android/assets/shaders/";
    // Set up our FPS logging object.
    lgrFps                = new FPSLogger();
        
    // Cache viewport size.
    resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    
    // Environment decdicated initializer.
    initEnvironment();
    // Post-processing dedicated initializer.
    initPostProcessing();
    // Camera / Camera controller dedicated initializer.
    initCamera();
    // Models / Instances dedicated initializer.
    initModelsAndInstances();
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
    blur.setBlurOpacity   (0.84f);
    postProMain.addEffect (blur);
    // Add FFA to post-processing.
    Nfaa faa              = new Nfaa(viewWidth/3, viewHeight/3);
    postProMain.addEffect (faa);
  }
  private void initCamera () {
    // Set up our main camera, and position it.
    camMain               = new PerspectiveCamera(67, viewWidth, viewHeight);
    camMain.position.set  (0.0f, 0.0f, 25.0f);
    camMain.lookAt        (0, 0, 0);
    camMain.near          = 1f;
    camMain.far           = 150f;
    camMain.update        ();
    // Attach a camera controller to the main camera, set it as the main processor.
    camCtrlMain           = new CameraInputController(camMain);
    Gdx.input.setInputProcessor(camCtrlMain);
  }
  private void initModelsAndInstances () {
    // This is the main model rendering batch.
    mdlBtchMain           = new ModelBatch();
        
    // Instantiate a single model builder.
    ModelBuilder mdlBuilder = new ModelBuilder();
    // Create a default material to work with.
    Material mtlDefault     = new Material(ColorAttribute.createDiffuse(1.0f, 1.0f, 1.0f, 1.0f), new BlendingAttribute(1.0f));
    // Get a cube model.
    mdlCube                 = mdlBuilder.createBox(2f, 2f, 2f, mtlDefault, Usage.Position | Usage.Normal);
    
    // Prepare the character model...
    mdlInstCharacter          = new SimpleCube();
    // As our character model will be rendered with everything else, add it to our instances array.
    instances.add           (mdlInstCharacter);
    
    // Create an array of cube for testing.
    int   iTrans, jTrans;
    float halfWidth         = CELL_DIMENSIONS.x / 2.0f;  
    float halfHeight        = CELL_DIMENSIONS.y / 2.0f;  
    float halfDepth         = CELL_DIMENSIONS.z / 2.0f;  
    for (int i = 0; i<CELL_DIMENSIONS.x; ++i) {
      iTrans  = i * 5;
      for (int j = 0; j<CELL_DIMENSIONS.y; ++j) {
        jTrans = j * 5;
        for (int k = 0; k<CELL_DIMENSIONS.z; ++k) {
          GameObject instance = new GameObject(mdlCube);
          instance.materials.get(0).set(
              ColorAttribute.createDiffuse(
                  1.0f / CELL_DIMENSIONS.x * i,
                  1.0f / CELL_DIMENSIONS.y * j,
                  1.0f / CELL_DIMENSIONS.z * k,
                  0.80f
              )
          );
          instance.transform.setToTranslation(iTrans-halfWidth, jTrans-halfHeight, k*5-halfDepth);
          instances.add(instance);
        }
      }
    }
  }

  @Override
  public void render () {
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

    // Log our FPS count to the console.
    lgrFps.log();
  }
  
  private void handleInputs () {
    tmpFloat       = Gdx.graphics.getDeltaTime();
    // Character moves forward / backward events.
    if (Gdx.input.isKeyPressed(Input.Keys.UP))
      mdlInstCharacter.transform.translate(TRANSLATION_SPEED * tmpFloat, 0, 0);
    else if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
      mdlInstCharacter.transform.translate(-TRANSLATION_SPEED * tmpFloat, 0, 0);
    
    // Character rotation events.
    if (Gdx.input.isKeyPressed(Input.Keys.I))
      mdlInstCharacter.rotate(camMain, Vector3.X, -1.0f*ROTATION_SPEED*tmpFloat);
    else if (Gdx.input.isKeyPressed(Input.Keys.K))
      mdlInstCharacter.rotate(camMain, Vector3.X, 1.0f*ROTATION_SPEED*tmpFloat);
    if (Gdx.input.isKeyPressed(Input.Keys.J))
      mdlInstCharacter.rotate(camMain, Vector3.Y, -1.0f*ROTATION_SPEED*tmpFloat);
    else if (Gdx.input.isKeyPressed(Input.Keys.L))
      mdlInstCharacter.rotate(camMain, Vector3.Y, 1.0f*ROTATION_SPEED*tmpFloat);

    
    // Left / Right events.
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
      mdlInstCharacter.transform.rotate(Vector3.Y, ROTATION_SPEED * tmpFloat);
    else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
      mdlInstCharacter.transform.rotate(Vector3.Y, -ROTATION_SPEED * tmpFloat);

    // Attach / Detach event.
    if (Gdx.input.isKeyPressed(Input.Keys.M) && !mdlInstCharacter.nodes.get(0).hasChildren()) {
      // Prepare an uninitialized model instance pointer.
      ModelInstance mdlInst   = null;
      // Prepare the green cube...
      mdlInst                 = new ModelInstance(mdlCube);
      mdlInst.materials.get(0).set(ColorAttribute.createDiffuse(0.0f, 1.0f, 0.0f, 0.7f));
      mdlInstCharacter.attachAt(mdlInst.nodes.get(0), 0.0f, 2.0f, 0.0f);
      // Prepare the red cube.
      mdlInst                 = new ModelInstance(mdlCube);
      mdlInst.materials.get(0).set(ColorAttribute.createDiffuse(1.0f, 0.0f, 0.0f, 0.7f));
      mdlInstCharacter.attachAt(mdlInst.nodes.get(0), 2.0f, 0.0f, 0.0f);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.N) && mdlInstCharacter.nodes.get(0).hasChildren())
      mdlInstCharacter.detachAllNodes();
    
    // Update camera controller.
    camCtrlMain.update();
  }

  @Override
  public void dispose () {
    postProMain.dispose();
    mdlBtchMain.dispose();
    mdlCube.dispose();
  }

  @Override
  public void resume () {
    postProMain.rebind();
  }

  @Override
  public void resize (int width, int height) {
    viewWidth         = width;
    viewHeight        = height;
  }

  @Override
  public void pause () {
  }
}
