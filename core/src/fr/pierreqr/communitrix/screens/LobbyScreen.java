package fr.pierreqr.communitrix.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Bounce;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.MotionBlur;

import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.GameObjectAccessor;
import fr.pierreqr.communitrix.gameObjects.Piece;
import fr.pierreqr.communitrix.networking.commands.tx.TXCombatPlayTurn;
import fr.pierreqr.communitrix.networking.shared.SHPiece;
import fr.pierreqr.communitrix.networking.shared.SHPlayer;

public class LobbyScreen implements Screen {
  public                enum          State         { Global, Joined, Starting }
  private static final  String        LogTag        = "LobbyScreen";
  
  // Scene setup related objects.
  public        Stage                 uiStage;
  private       TweenManager          tweener       = null;
  private       Environment           envMain;
  private       PerspectiveCamera     camMain;
  private       CameraInputController camCtrlMain;
  private       PostProcessor         postProMain;
  // State related members.
  private       State                 state         = State.Global;
  public        Array<SHPlayer>       players       = new Array<SHPlayer>();
  // Various object instances.
  private final Model                 characterModel;
  private final Array<GameObject>     instances     = new Array<GameObject>();
  private final Map<String,GameObject>characters    = new HashMap<String,GameObject>();
  
  private final Piece                 myPiece;
  // UI Components.
  private       Label                 lblFPS, lblPlayers;
  
  // Game instance cache.
  private final Communitrix           ctx;
  
  public LobbyScreen (final Communitrix communitrixInstance) {
    Gdx.app.log           (LogTag, "Constructing.");
    // Cache our game instance.
    ctx                   = communitrixInstance;

    // Initialize tweening engine.
    tweener               = new TweenManager();
    
    // Set up the scene environment.
    envMain               = new Environment();
    envMain.set           (new ColorAttribute(ColorAttribute.AmbientLight, 0.7f, 0.7f, 0.7f, 1.0f));
    envMain.set           (new ColorAttribute(ColorAttribute.Fog, 0.01f, 0.01f, 0.01f, 1.0f));
    envMain.add           (new DirectionalLight().set(new Color(0.7f, 0.7f, 0.7f, 1.0f), -1f, -0.8f, -0.2f));

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
    // Attach a camera controller to the main camera, set it as the main processor.
    camCtrlMain           = new CameraInputController(camMain);

    // Initialize flat UI.
    uiStage               = new Stage();
    // Create the various UI elements.
    lblFPS                = new Label("", ctx.uiSkin);
    lblFPS.setColor       (Color.WHITE);
    lblPlayers            = new Label("", ctx.uiSkin);
    lblPlayers.setColor   (Color.WHITE);
    
    characterModel        = ctx.modelBuilder.createBox(
                                2, 2, 2,
                                ctx.defaultMaterial,
                                Usage.Position | Usage.Normal);

    
    myPiece               = new Piece(null);
    myPiece.transform.rotate(Vector3.X, 15);
    myPiece.transform.rotate(Vector3.Z, 15);
    instances.add         (myPiece);
    
    final Model       xyz   = ctx.modelBuilder.createXYZCoordinates(1, ctx.defaultMaterial, Usage.Position | Usage.Normal);
    final GameObject  obj   = new GameObject(xyz);
    instances.add(obj);
  }
  // Setter on state, handles transitions.
  public LobbyScreen setState (final State state) {
    if (this.state!=state) {
      Gdx.app.log   (LogTag, "Changing state to " + state + ".");
      this.state  = state;
      switch (this.state) {
        case Global:
          break;
        case Joined:
          break;
        case Starting:
          break;
      }
    }
    return this;
  }
  // Setter on players, handles list population.
  public LobbyScreen setPlayers (final ArrayList<SHPlayer> players) {
    Gdx.app.log         (LogTag, "Setting new player list (" + players.size() + ").");
    // Remove all character instances.
    for (final SHPlayer player : this.players)
      instances.removeValue(
        characters.remove(player.uuid)
      , true);
    // Clear character list.
    this.players.clear  ();
    // Create our players.
    for (final SHPlayer player : players)
      addPlayer         (player);
    updatePlayers       ();
    return this;
  }
  public LobbyScreen addPlayer (final SHPlayer player) {
    Gdx.app.log       (LogTag, "Adding player (" + player.uuid + ").");
    
    final GameObject obj  = new GameObject(characterModel);
    
    obj.transform.setTranslation(players.size * 2.5f, 0, 0);
    characters.put        (player.uuid, obj);
    //instances.add         (obj);
    players.add           (player);
    updatePlayers         ();
    // Schedule animation.
    Tween
      .to                 (obj, GameObjectAccessor.TransY, 1.2f)
      .target             (0)
      .ease               (Bounce.OUT)
      .start              (tweener);
    return this;
  }
  public LobbyScreen removePlayer (final String uuid) {
    Gdx.app.log       (LogTag, "Removing player (" + uuid + ").");
    boolean shift     = false;
    int     idx       = 0;
    SHPlayer  remove    = null;
    for (final SHPlayer player : players) {
      if (shift) {
        final GameObject obj  = characters.get(player.uuid);
        Tween
          .to             (obj, GameObjectAccessor.TransX, 0.5f)
          .delay          (0.3f)
          .target         ((idx-1) * 2.5f)
          .ease           (Bounce.OUT)
          .start          (tweener);
      }
      else if (player.uuid.equals(uuid)) {
        final GameObject obj  = characters.remove(player.uuid);
        remove                = player;
        shift                 = true;
        Tween
          .to(obj, GameObjectAccessor.TransYRotX, 0.5f)
          .target(-10.0f, 180.0f)
          .ease(aurelienribon.tweenengine.equations.Expo.IN)
          .start(tweener)
          .setCallback(new TweenCallback() { @Override public void onEvent(int arg0, BaseTween<?> arg1) { instances.removeValue (obj, true); } });
      }
      ++idx;
    }
    players.removeValue   (remove, true);
    updatePlayers         ();
    return this;
  }
  private void updatePlayers() {
    if (players==null)  return;
    StringBuilder sb    = new StringBuilder(players.size * 16);
    for (final SHPlayer player : players) {
      if (sb.length()>0)
        sb.append       (", ");
      sb.append         (player.username);
    }
    sb.append           (" (" + players.size + ")");
    lblPlayers.setText  (sb.toString());
  }

  public LobbyScreen setRemotePiece (final SHPiece piece) {
    myPiece.setFromSharedPiece(piece);
    return this;
  }
  public LobbyScreen setPieces (final SHPiece[] pieces) {
    return this;
  }
  
  @Override public void show () {
    Gdx.app.log       (LogTag, "Show!");
    // Set the input controller.
    Gdx.input.setInputProcessor(camCtrlMain);

    // Instantiate environment.
    if (instances.size==0) {
    }

    // Put our label on stage.
    uiStage.addActor    (lblFPS);
    uiStage.addActor    (lblPlayers);
  }
  @Override public void hide () {
    Gdx.app.log       (LogTag, "Hide!");
    // Remove all instances except our character.
    if (instances.size!=0)
      instances.clear   ();
    // Clear flat UI.
    uiStage.clear();
  }
  @Override public void pause () {}
  @Override public void resume () {}
  
  @Override public void dispose () {
    characterModel.dispose();
    postProMain.dispose ();
  }
  
  
  private static final Quaternion   tmpQuat   = new Quaternion();
  private static final Vector3      tmpVec3   =  new Vector3();
  
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
    tweener.update      (delta);
    
    if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
      tmpQuat.mul(new Quaternion(Vector3.Y, 90));
      ctx.networkingManager.send(new TXCombatPlayTurn("none", tmpQuat, tmpVec3));
    }
    else if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
      tmpQuat.mul(new Quaternion(Vector3.Y, -90));
      ctx.networkingManager.send(new TXCombatPlayTurn("none", tmpQuat, tmpVec3));
    }
    else if (Gdx.input.isKeyJustPressed(Keys.UP)) {
      tmpQuat.mul(new Quaternion(Vector3.X, -90));
      ctx.networkingManager.send(new TXCombatPlayTurn("none", tmpQuat, tmpVec3));
    }
    else if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
      tmpQuat.mul(new Quaternion(Vector3.X, 90));
      ctx.networkingManager.send(new TXCombatPlayTurn("none", tmpQuat, tmpVec3));
    }
    
    // Update camera controller.
    camCtrlMain.update  ();
    // Update flat UI.
    uiStage.act         (delta);

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
    
    // Update flat UI.
    lblFPS.setText            ("Lobby FPS: " + Gdx.graphics.getFramesPerSecond());
    uiStage.act               (delta);
    uiStage.draw              ();
  }

  @Override public void resize (final int width, final int height) {
    // Update flat UI.
    if (uiStage!=null)      uiStage.getViewport().update(width, height, true);
    // If a post-processor exists, update it.
    if (postProMain!=null)  postProMain.rebind();
    // Place flat UI.
    lblFPS.setPosition      (5, ctx.viewHeight - 15);
    lblPlayers.setPosition  (5, 15);
  }
}
