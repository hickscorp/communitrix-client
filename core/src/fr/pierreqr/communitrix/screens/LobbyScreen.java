package fr.pierreqr.communitrix.screens;

import java.util.ArrayList;

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
  public        Array<String>         players       = new Array<String>();
  // Various object instances.
  private final Array<GameObject>     instances     = new Array<GameObject>();
  private final Array<GameObject>     characters    = new Array<GameObject>();
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
  public LobbyScreen setPlayers (final ArrayList<String> players) {
    // Remove all character instances.
    while (characters.size>0)
      instances.removeValue (characters.removeIndex(0), true);
    // Clear character list.
    this.players.clear  ();
    // Create our players.
    for (final String player : players)
      addPlayer(player);
    updatePlayers  ();
    return this;
  }
  public LobbyScreen addPlayer (final String player) {
    if (!players.contains(player, false)) {
      final GameObject obj  = new GameObject(ctx.getModel("Cube"));
      obj.transform.setTranslation(players.size * 1.5f, 15, 0);
      Tween
        .to(obj, GameObjectAccessor.POSITION_Y, 1.2f)
        .target(0)
        .ease(Bounce.OUT)
        .start(tweener);
      players.add         (player);
      characters.add      (obj);
      instances.add       (obj);
      updatePlayers       ();
    }
    return this;
  }
  public LobbyScreen removePlayer (final String player) {
    if (players.contains(player, false)) {
      final GameObject obj  = characters.removeIndex(0);
      Tween
        .to(obj, GameObjectAccessor.POSITION_Y, 0.5f)
        .target(-10)
        .ease(aurelienribon.tweenengine.equations.Expo.IN)
        .start(tweener)
        .setCallback(new TweenCallback() { @Override public void onEvent(int arg0, BaseTween<?> arg1) { instances.removeValue (obj, true); } });
      players.removeValue   (player, false);
      updatePlayers         ();
    }
    return this;
  }
  private void updatePlayers() {
    if (players==null)  return;
    StringBuilder sb    = new StringBuilder(players.size * 32);
    for (final String player : players) {
      if (sb.length()>0)
        sb.append       (", ");
      sb.append         (player);
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
