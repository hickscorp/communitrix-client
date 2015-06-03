package fr.pierreqr.communitrix.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.*;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.MotionBlur;

import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.GameObjectAccessor;
import fr.pierreqr.communitrix.networking.Player;

public class LobbyScreen implements Screen {
  public        enum                  State         { Global, Joined, Starting }
  
  // Scene setup related objects.
  public        Stage                 uiStage;
  private       TweenManager          tweener       = null;
  private       Environment           envMain;
  private       PerspectiveCamera     camMain;
  private       CameraInputController camCtrlMain;
  private       PostProcessor         postProMain;
  // State related members.
  private       State                 state         = State.Global;
  public        Array<Player>         players       = new Array<Player>();
  // Various object instances.
  private final Array<GameObject>     instances     = new Array<GameObject>();
  private final Map<String,GameObject>characters    = new HashMap<String,GameObject>();
  // UI Components.
  private       Label                 lblFPS, lblPlayers;
  
  // Game instance cache.
  private final Communitrix           ctx;
  
  public LobbyScreen (final Communitrix communitrixInstance) {
    // Cache our game instance.
    ctx                   = communitrixInstance;

    // Initialize tweening engine.
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
    // Attach a camera controller to the main camera, set it as the main processor.
    camCtrlMain           = new CameraInputController(camMain);

    // Initialize flat UI.
    uiStage               = new Stage();
    // Create the various UI elements.
    lblFPS                = new Label("", ctx.uiSkin);
    lblFPS.setColor       (Color.WHITE);
    lblPlayers            = new Label("", ctx.uiSkin);
    lblPlayers.setColor   (Color.WHITE);
  }
  // Setter on state, handles transitions.
  public LobbyScreen setState (final State state) {
    if (this.state!=state) {
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
  public LobbyScreen setPlayers (final ArrayList<Player> players) {
    // Remove all character instances.
    for (final Player player : this.players)
      instances.removeValue(
        characters.remove(player.uuid)
      , true);
    // Clear character list.
    this.players.clear  ();
    // Create our players.
    for (final Player player : players)
      addPlayer(player);
    updatePlayers  ();
    return this;
  }
  public LobbyScreen addPlayer (final Player player) {
    final GameObject obj  = new GameObject(ctx.getModel("Cube"));
    obj.transform.setTranslation(players.size * 2.5f, 15, 0);
    characters.put      (player.uuid, obj);
    instances.add       (obj);
    players.add         (player);
    updatePlayers       ();
    // Schedule animation.
    Tween
      .to(obj, GameObjectAccessor.TransY, 1.2f)
      .target(0)
      .ease(Bounce.OUT)
      .start(tweener);
    return this;
  }
  public LobbyScreen removePlayer (final String uuid) {
    boolean shift   = false;
    int     idx     = 0;
    Player  remove  = null;
    for (final Player player : players) {
      if (shift) {
        final GameObject obj  = characters.get(player.uuid);
        Tween
          .to(obj, GameObjectAccessor.TransX, 0.5f)
          .delay(0.3f)
          .target((idx-1) * 2.5f)
          .ease(Bounce.OUT)
          .start(tweener);
      }
      else if (player.uuid.equals(uuid)) {
        final GameObject obj  = characters.remove(player.uuid);
        remove                = player;
        shift                 = true;
        Tween
          .to(obj, GameObjectAccessor.TransY | GameObjectAccessor.RotX, 0.5f)
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
    for (final Player player : players) {
      if (sb.length()>0)
        sb.append       (", ");
      sb.append         (player.username);
    }
    sb.append           (" (" + players.size + ")");
    lblPlayers.setText  (sb.toString());
  }

  @Override public void show () {
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
    // Remove all instances except our character.
    if (instances.size!=0)
      instances.clear   ();
    // Clear flat UI.
    uiStage.clear();
  }
  @Override public void pause () {}
  @Override public void resume () {}
  
  @Override public void dispose () {
    postProMain.dispose ();
  }

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
