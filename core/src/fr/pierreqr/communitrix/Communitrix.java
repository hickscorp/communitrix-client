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
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.MotionBlur;
import com.bitfire.postprocessing.effects.Nfaa;
import com.bitfire.utils.ShaderLoader;

public class Communitrix extends ApplicationAdapter {
  // Constants.
  public  static final  Vector3       CELL_DIMENSIONS       = new Vector3(5, 5, 5);
  private static final  float         TRANSLATION_SPEED     = 20.0f;
  private static final  float         ROTATION_SPEED        = 120.0f;
  // Those are temporaries.
  private static        float         tmpFloat;

  
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
  public        GameObject            mdlInsBlueCube;
  public        Node                  nodeBlueCube, nodeGreenCube, nodeRedCube;
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
    
    // Set up the scene environment.
    envMain               = new Environment();
    envMain.set           (new ColorAttribute(ColorAttribute.AmbientLight, 0.9f, 0.9f, 0.9f, 1.0f));
    envMain.set           (new ColorAttribute(ColorAttribute.Fog, 0.01f, 0.01f, 0.01f, 1.0f));
    //environment.add(new DirectionalLight().set(Color.WHITE, -1f, -0.8f, -0.2f));
    
    // Cache viewport size.
    resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    
    // Set up the main post-processor.
    postProMain           = new PostProcessor(true, true, true);
    // Add bloom to post-processor.
    Bloom blm             = new Bloom(viewWidth/3, viewHeight/3);
    blm.setBloomIntesity  (1.2f);
    blm.setBloomSaturation(1.0f);
    postProMain.addEffect (blm);
    // Add motion blur to post-processor.
    MotionBlur blur       = new MotionBlur();
    blur.setBlurOpacity   (0.84f);
    postProMain.addEffect (blur);
    // Add FFA to post-processing.
    Nfaa faa              = new Nfaa(viewWidth/3, viewHeight/3);
    postProMain.addEffect(faa);
    
    // This is the main model rendering batch.
    mdlBtchMain           = new ModelBatch();

    // Set up our main camera, and position it.
    camMain               = new PerspectiveCamera(67, viewWidth, viewHeight);
    camMain.position.set  (25f, 25f, 25f);
    camMain.lookAt        (0, 0, 0);
    camMain.near          = 1f;
    camMain.far           = 150f;
    camMain.update        ();
    // Attach a camera controller to the main camera, set it as the main processor.
    camCtrlMain           = new CameraInputController(camMain);
    Gdx.input.setInputProcessor(camCtrlMain);
    
    // Instantiate a single model builder.
    ModelBuilder mdlBuilder = new ModelBuilder();
    // Create a default material to work with.
    Material mtlDefault     = new Material(ColorAttribute.createDiffuse(1.0f, 1.0f, 1.0f, 1.0f), new BlendingAttribute(1.0f));
    // Get a cube model.
    mdlCube                 = mdlBuilder.createBox(3f, 3f, 3f, mtlDefault, Usage.Position | Usage.Normal);
    // Prepare the blue cube...
    mdlInsBlueCube          = new GameObject(mdlCube);
    mdlInsBlueCube.materials.get(0).set(ColorAttribute.createDiffuse(0.0f, 0.0f, 1.0f, 0.7f));
    // As our blue cube will be rendered with everything else, add it to our instances array.
    instances.add           (mdlInsBlueCube);

    // Prepare an uninitialized model instance pointer.
    ModelInstance mdlInst;
    // Prepare the green cube...
    mdlInst                 = new ModelInstance(mdlCube);
    mdlInst.materials.get(0).set(ColorAttribute.createDiffuse(0.0f, 1.0f, 0.0f, 0.7f));
    nodeGreenCube           = mdlInst.nodes.get(0);
    // Prepare the red cube.
    mdlInst                 = new GameObject(mdlCube);
    mdlInst.materials.get(0).set(ColorAttribute.createDiffuse(1.0f, 0.0f, 0.0f, 0.7f));
    nodeRedCube             = mdlInst.nodes.get(0);

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
    
    tmpFloat       = Gdx.graphics.getDeltaTime();
    // Up / Down events.
    if (Gdx.input.isKeyPressed(Input.Keys.UP))
      mdlInsBlueCube.transform.translate(TRANSLATION_SPEED * tmpFloat, 0, 0);
    else if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
      mdlInsBlueCube.transform.translate(-TRANSLATION_SPEED * tmpFloat, 0, 0);
    // Left / Right events.
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
      mdlInsBlueCube.transform.rotate(Vector3.Y, ROTATION_SPEED * tmpFloat);
    else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
      mdlInsBlueCube.transform.rotate(Vector3.Y, -ROTATION_SPEED * tmpFloat);
    // Attach / Detach event.
    if (Gdx.input.isKeyPressed(Input.Keys.M)) {
      mdlInsBlueCube.attachAt(nodeGreenCube, 0.0f, 3.0f, 0.0f);
      mdlInsBlueCube.attachAt(nodeRedCube, 3.0f, 0.0f, 0.0f);
      // attachAtPosition(nodeGreenCube, 0.0f, 3.0f, 0.0f);
      // attachAtPosition(nodeRedCube, 3.0f, 0.0f, 0.0f);
      // mdlInsBlueCube.calculateTransforms();
    }
    if (Gdx.input.isKeyPressed(Input.Keys.N))
      mdlInsBlueCube.detachAllNodes();
    
    // Update the camera according to the controller inputs.
    camCtrlMain.update();
    
    // Begin post-processing FBO capture.
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
