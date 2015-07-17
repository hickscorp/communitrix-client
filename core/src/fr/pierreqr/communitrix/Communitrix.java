package fr.pierreqr.communitrix;

import java.util.EnumMap;
import java.util.Random;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.UBJsonReader;
import com.bitfire.utils.ShaderLoader;
import fr.pierreqr.communitrix.Constants.CubeFace;
import fr.pierreqr.communitrix.Constants.SkinSize;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.networking.NetworkingManager;
import fr.pierreqr.communitrix.networking.NetworkingManager.NetworkDelegate;
import fr.pierreqr.communitrix.networking.cmd.rx.*;
import fr.pierreqr.communitrix.screens.BaseScreen;
import fr.pierreqr.communitrix.screens.MainScreen;
import fr.pierreqr.communitrix.screens.StartupScreen;
import fr.pierreqr.communitrix.tweeners.CameraAccessor;
import fr.pierreqr.communitrix.tweeners.GameObjectAccessor;

public class Communitrix extends Game implements NetworkDelegate {
  // Common materials.
  public final static   Material[]  faceMaterials         = new Material[12];
  // Various constants.
  private static final  String      LogTag                = "Communitrix";
  private static final  String      host                  = "localhost";

  // Shared members.
  public          ApplicationType   applicationType;
  public          int               viewWidth, viewHeight;
  public          EnumMap<SkinSize, Skin>
                                    skins                 = new EnumMap<SkinSize, Skin>(SkinSize.class);
  public          TweenManager      tweener               = new TweenManager();
  public          ModelBuilder      modelBuilder;
  public          ModelBatch        modelBatch;
  public          G3dModelLoader    modelLoader;
  public          Material          defaultMaterial;
  public          Model             dummyModel;
  // Random generator.
  public final    Random            rand                  = new Random();
  // Network-related objects.
  public          NetworkingManager networkingManager;
  public          boolean           connected             = false;
  public          Timer             networkTimer;
  // Currently displayed screen.
  public          BaseScreen        currentScreen                = null;
  
  // The singleton instance.
  private static  Communitrix       instance;
  
  public static Communitrix getInstance() {
    return instance;
  }
  
  public Communitrix () {
    // Store singleton instance.
    instance                = this;

    // Configure assets etc.
    ShaderLoader.BasePath   = "shaders/";
    
    // Register motion tweening accessors.
    Tween.setCombinedAttributesLimit  (6);
    Tween.registerAccessor            (GameObject.class,        new GameObjectAccessor());
    Tween.registerAccessor            (PerspectiveCamera.class, new CameraAccessor());
  }
  
  public void setLastError (final int code, final String reason) {
    if (currentScreen!=null)   currentScreen.setLastError(code, reason);
    else                Gdx.app.log(LogTag, "An error has occured: #" + code + " - " + reason);
  }
  
  @Override public void create () {
    // Cache application type.
    applicationType     = Gdx.app.getType();

    // After starting the application, we can query for the desktop dimensions
    boolean fullScreen = false;
    if (fullScreen && applicationType==ApplicationType.Desktop)
      Gdx.graphics.setDisplayMode (Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);
    
    // Prepare face materials.
    if (faceMaterials[0]==null) {
      final TextureAtlas  atlas       = new TextureAtlas(Gdx.files.internal("atlases/Main.atlas"));
      final Attribute     blend       = new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.95f);
      for (final CubeFace face : Constants.CubeFace.values())
        faceMaterials[face.ordinal()]   = new Material(TextureAttribute.createDiffuse(atlas.findRegion(face.toString())), blend);
    }

    // Force cache viewport size.
    resize                  (Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    
    // Instantiate shared members.
    skins.put               (SkinSize.Mini,     new Skin(Gdx.files.internal("skins/uiskin-mini.json")));
    skins.put               (SkinSize.Medium,   new Skin(Gdx.files.internal("skins/uiskin-medium.json")));
    skins.put               (SkinSize.Large,    new Skin(Gdx.files.internal("skins/uiskin-large.json")));
    
    modelBuilder            = new ModelBuilder();
    modelBatch              = new ModelBatch();
    defaultMaterial         = new Material(ColorAttribute.createDiffuse(Color.WHITE));
    dummyModel              = new Model();
    
    // Instantiate networking manager.
    networkTimer            = new Timer();
    networkingManager       = new NetworkingManager(host, 9003, this);
    networkingManager.start ();
    
    // Prepare our shared model loader.
    modelLoader             = new G3dModelLoader(new UBJsonReader());
    
    setScreen               (new StartupScreen());
  }
  
  // Occurs when the game exits.
  @Override public void dispose () {
    setScreen               (null);
    if (networkingManager!=null) {
      networkTimer.stop     ();
      networkingManager.stop();
      networkingManager     = null;
    }
    modelBatch.dispose      ();
  }

  @Override public void resize (final int w, final int h) {
    super.resize(viewWidth = w, viewHeight = h);
  }
  
  // This method runs after rendering.
  @Override public void onServerMessage (final RXBase baseCmd) {
    switch (baseCmd.type) {
      // We received the welcome message. Switch screens.
      case Welcome:
        if (currentScreen==null || !currentScreen.getClass().toString().equals("StartupScreen"))
          setScreen (new MainScreen());
        break;
      // We are disconnected, remove the screen.
      case Disconnected:
        if (currentScreen==null || !currentScreen.getClass().toString().equals("StartupScreen"))
          setScreen (new StartupScreen());
        networkTimer
          .scheduleTask(new Timer.Task() {
            @Override public void run() {
              networkingManager.start();
            }
          }, 1.0f);
        break;
      // We received an error!.
      case Error: {
        final RXError cmd   = (RXError)baseCmd;
        setLastError(cmd.code, cmd.reason);
        break;
      }
      default:
    }
    return;
  }

  private void setScreen (final BaseScreen newScreen) {
    if (currentScreen==newScreen)
      return;
    else if (currentScreen!=null) {
      networkingManager.removeDelegate(currentScreen);
      currentScreen.dispose();
    }
    if ((currentScreen=newScreen)!=null)
      networkingManager.addDelegate(currentScreen);
    super.setScreen(currentScreen);
  }

  @Override public void render () {
    // Clear viewport etc.
    Gdx.gl.glClear        (GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    // Enable alpha blending.
    Gdx.gl.glEnable       (GL20.GL_BLEND);
    Gdx.gl.glBlendFunc    (GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    // Enable back-face culling.
    Gdx.gl.glEnable       (GL20.GL_CULL_FACE);
    Gdx.gl.glCullFace     (GL20.GL_FRONT);
    super.render          ();
  }

  public static float round (final float v, final float precision) {
    return    Math.round(v * precision) / precision;
  }
  public static Vector3 round (final Vector3 v, final float precision) {
    v.x     = round(v.x, precision);
    v.y     = round(v.y, precision);
    v.z     = round(v.z, precision);
    return  v;
  }
  public static Quaternion round (final Quaternion q, final float precision) {
    q.x     = round(q.x, precision);
    q.y     = round(q.y, precision);
    q.z     = round(q.z, precision);
    q.w     = round(q.w, precision);
    return  q.nor();
  }
  public static BoundingBox round (final BoundingBox b, final float precision) {
    round   (b.min, precision);
    round   (b.max, precision);
    return  b;
  }
}
