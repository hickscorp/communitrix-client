package fr.pierreqr.communitrix;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
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
import com.bitfire.utils.ShaderLoader;

public class Communitrix extends ApplicationAdapter {
  public Environment environment;
  public PerspectiveCamera cam;
  public CameraInputController camController;
  public Model model;
  public Array<ModelInstance> instances = new Array<ModelInstance>();
  public ModelBatch modelBatch;
  public PostProcessor postProcessor;
  public FPSLogger logger;

  public ModelInstance ourCube;
  public ModelInstance secondCube;
  public ModelInstance thirdCube;
  public Node ourCubeNode;
  public Node secondCubeNode;
  public Node thirdCubeNode;

  @Override
  public void create () {
    // After starting the application, we can query for the desktop dimensions
    if ( false && Gdx.app.getType() == ApplicationType.Desktop )
      Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);

    ShaderLoader.BasePath = "../android/assets/shaders/";
    postProcessor         = new PostProcessor(true, true, true);

    Bloom bloom           = new Bloom(Gdx.graphics.getWidth()/5, Gdx.graphics.getHeight()/5);
    bloom.setBloomIntesity(1.2f);
    bloom.setBloomSaturation(1.0f);
    postProcessor.addEffect(bloom);

    MotionBlur motionBlur = new MotionBlur();
    motionBlur.setBlurOpacity(0.60f);
    postProcessor.addEffect(motionBlur);

    logger   = new FPSLogger();

    environment = new Environment();
    environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.9f, 0.9f, 0.9f, 1.0f));
    environment.set(new ColorAttribute(ColorAttribute.Fog, 0.01f, 0.01f, 0.01f, 1.0f));
    //environment.add(new DirectionalLight().set(Color.WHITE, -1f, -0.8f, -0.2f));

    modelBatch = new ModelBatch();

    cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    cam.position.set(25f, 25f, 25f);
    cam.lookAt(0, 0, 0);
    cam.near = 1f;
    cam.far = 200f;
    cam.update();

    camController = new CameraInputController(cam);
    Gdx.input.setInputProcessor(camController);

    int width     = 10;
    int height    = 10;
    int depth     = 10;
    int halfSize  = width * 5 / 2;

    ModelBuilder modelBuilder   = new ModelBuilder();

    Material material           = new Material(ColorAttribute.createDiffuse(1.0f, 1.0f, 1.0f, 1.0f), new BlendingAttribute(0.8f));
    model                       = modelBuilder.createBox(3f, 3f, 3f, material, Usage.Position | Usage.Normal);
    ourCube                     = new ModelInstance(model);
    ourCubeNode                 = ourCube.nodes.get(0);

    Material material2          = new Material(ColorAttribute.createDiffuse(Color.RED));
    model                       = modelBuilder.createBox(3f, 3f, 3f, material2, Usage.Position | Usage.Normal);
    secondCube                  = new ModelInstance(model);
    secondCubeNode                = secondCube.nodes.get(0);

    Material material3          = new Material(ColorAttribute.createDiffuse(Color.GREEN));
    model                       = modelBuilder.createBox(3f, 3f, 3f, material3, Usage.Position | Usage.Normal);
    thirdCube                  = new ModelInstance(model);
    thirdCubeNode              = thirdCube.nodes.get(0);


//    int iTrans, jTrans;
//    for (int i = 0; i<width; ++i) {
//      iTrans  = i * 5;
//      for (int j = 0; j<height; ++j) {
//        jTrans = j * 5;
//        for (int k = 0; k<depth; ++k) {
//          ModelInstance instance      = new ModelInstance(model);
//          Material current            = instance.materials.get(0);
//          current.set(ColorAttribute.createDiffuse(1.0f / width * i, 1.0f / height * j, 1.0f / depth * k, 0.65f));
//
//          instance.transform.setToTranslation(iTrans - halfSize, jTrans - halfSize, k*5 - halfSize);
//          instances.add(instance);
//        }
//      }
//    }
  }

  private static final float ourCubeSpeed         = 10.0f;
  private static final float ourCubeRotationSpeed = 90.0f;
  private float       delta;

  @Override
  public void render () {
    delta       = Gdx.graphics.getDeltaTime();

    if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
      ourCube.transform.translate(ourCubeSpeed * delta, 0, 0);
    }
    else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
      ourCube.transform.translate(-ourCubeSpeed * delta, 0, 0);
    }

    if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      ourCube.transform.rotate(Vector3.Y, ourCubeRotationSpeed * delta);
    }
    else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
      ourCube.transform.rotate(Vector3.Y, -ourCubeRotationSpeed * delta);
    }

    if (Gdx.input.isKeyPressed(Input.Keys.M)) {
      if(secondCubeNode.getParent() != ourCubeNode)
        attachAtPosition(secondCubeNode,3,3,0);
        attachAtPosition(thirdCubeNode,3,0,0);
        ourCube.calculateTransforms();
    }
    if(Gdx.input.isKeyPressed(Input.Keys.N)){
      if(secondCubeNode.getParent() == ourCubeNode)
        detach();
    }


    postProcessor.capture();

    // Clear viewport etc.
    Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

    // Enable alpha blending.
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

    // Enable backface culling.
    Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    Gdx.gl.glCullFace(GL20.GL_BACK);

    camController.update();

    modelBatch.begin(cam);
    modelBatch.render(ourCube, environment);
//    for (final ModelInstance instance : instances) {
//      if (isVisible(cam, instance)) {
//        modelBatch.render(instance, environment);
//      }
//    }
    modelBatch.end();
    postProcessor.render();

    logger.log();
  }

  private void attachAtPosition(Node node, int x, int y, int z){
    node.attachTo(ourCubeNode);
    node.translation.set(x,y,z);
    node.scale.set(1,1,1);
    node.rotation.idt();
  }
  private void detach(){
    int n = ourCubeNode.getChildCount() - 1;
    while( n >= 0){
      ourCubeNode.removeChild(ourCubeNode.getChild(n));
      n--;
    }
  }

  private Vector3 position = new Vector3();
  private final Vector3 dimensions = new Vector3(3.0f, 3.0f, 3.0f);
  protected boolean isVisible(final Camera cam, final ModelInstance instance) {
    instance.transform.getTranslation(position);
    return cam.frustum.boundsInFrustum(position, dimensions);
  }

  @Override
  public void dispose () {
    modelBatch.dispose();
    model.dispose();
    postProcessor.dispose();
  }

  @Override
  public void resume () {
    postProcessor.rebind();
  }

  @Override
  public void resize (int width, int height) {
  }

  @Override
  public void pause () {
  }
}
