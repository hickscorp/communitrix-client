package fr.pierreqr.communitrix.gameObjects;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquation;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import fr.pierreqr.communitrix.Communitrix;

public class GameObject extends ModelInstance implements RenderableProvider {
  // Center and dimensions will be re-calculated based on radius.
  public final      Vector3       center          = new Vector3();
  public final      Vector3       dimensions      = new Vector3();
  public final      BoundingBox   bounds          = new BoundingBox();
  public            float         radius;

  // For animating purposes, we store starting position / rotation.
  public  final     Vector3       startPosition   = new Vector3();
  public  final     Quaternion    startRotation   = new Quaternion();
  // The current properties represent where the object is right now.
  public  final     Vector3       currentPosition = new Vector3();
  public  final     Quaternion    currentRotation = new Quaternion();
  // The target properties represent where the object will end up being.
  public  final     Vector3       targetPosition  = new Vector3();
  public  final     Quaternion    targetRotation  = new Quaternion();
  // Whenever an object is animated, its bounds will be recomputed into fakeBounds.
  public  final     BoundingBox   fakeBounds      = new BoundingBox();
  
  // Hierarchy members.
  public            GameObject        parent      = null;
  public final      Array<GameObject> children    = new Array<GameObject>();

  // Temporaries.
  private final static  Matrix4       tmpMat      = new Matrix4();
  private final static  Vector3       tmpPos      = new Vector3();
  private final static  Quaternion    tmpRot      = new Quaternion();
  
  public static         Camera        renderCam   = null;

  public GameObject (Model model) {
    super               (model);
    recomputeBounds     ();
  }
  public void recomputeBounds () {
    calculateBoundingBox  (bounds);
    bounds.getCenter      (center);
    bounds.getDimensions  (dimensions);
    radius                = dimensions.len() / 2f;
  }
  
  public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
    if (renderCam.frustum.boundsInFrustum(fakeBounds))
      super.getRenderables (renderables, pool);
    for (final GameObject child : children)
      child.getRenderables (renderables, pool);
  }
  
  public void setParent (final GameObject newParent) {
    unparent            ();
    if ((parent = newParent)==null)
      return;
    parent.children.add (this);
    // Get parent's currently displayed inverse rotation.
    parent.transform
      .getRotation      (tmpRot)
      .conjugate        ();
    // Get parent's currently displayed translation.
    parent.transform
      .getTranslation   (tmpPos);
    currentPosition
      .sub              (tmpPos)
      .mul              (tmpRot);
    startPosition
      .set              (currentPosition);
    currentRotation
      .mulLeft          (tmpRot);
    startRotation
      .set              (currentRotation);
    Communitrix.round(
      targetRotation
        .mulLeft        (tmpRot)
    , 100.0f);
    recomputeFakeBounds ();
  }
  public void unparent () {
    if (parent==null)
      return;
    parent.children.removeValue (this, true);
    // The starting point for all the animations will be the current world transform of the object.
    currentPosition.set(startPosition.set(transform.getTranslation(tmpPos)));
    Communitrix.round(targetPosition.sub(parent.targetPosition), 1.0f);
    currentRotation.set(startRotation.set(transform.getRotation(tmpRot)));
    Communitrix.round(targetRotation.mulLeft(parent.targetRotation).cpy().conjugate(), 100.0f);
    parent                      = null;
    recomputeFakeBounds         ();
  }
  public void addChild (final GameObject child) {
    if (child!=null)
      child.setParent   (this);
  }
  public void removeChild (final GameObject child) {
    if (child!=null)
      child.setParent   (null);
  }
  public void reset () {
    // Reset position.
    Communitrix.round         (transform.getTranslation (currentPosition), 1.0f);
    startPosition.set         (currentPosition);
    targetPosition.set        (currentPosition);
    // Reset rotation.
    Communitrix.round         (transform.getRotation    (currentRotation), 100.0f);
    startRotation.set         (currentRotation);
    targetRotation.set        (currentRotation);
  }
  
  public BaseTween<?> start (final TweenManager tweener, final float duration, final TweenEquation ease) {
    // Prepare this object and all his children.
    prepare             (tweener);
    for (final GameObject child : children)
      child.prepare     (tweener);
    // Animate.
    return Tween
      .to               (this, 0, duration)
      .target           (1.0f)
      .ease             (ease)
      .start            (tweener);
  }
  private void prepare (final TweenManager tweener) {
    // Round targets.
    Communitrix.round   (targetPosition, 1.0f);
    Communitrix.round   (targetRotation, 100.0f);
    // Kill any pending animation involving this object.
    if (tweener!=null)
      tweener.killTarget(this);
    // Store current values into current markers.
    startPosition.set   (currentPosition);
    startRotation.set   (currentRotation);
    recomputeFakeBounds ();
  }
  private void recomputeFakeBounds () {
    // Handle new bounds.
    Communitrix.round(
      fakeBounds
        .set              (bounds)
        .mul              (tmpMat.idt().rotate(targetRotation))
    , 100.0f);
  }
  public void morph (final float alpha) {
    currentPosition
      .set        (startPosition)
      .lerp       (targetPosition, alpha);
    currentRotation
      .set        (startRotation)
      .slerp      (targetRotation, alpha);
    tmpMat
      .set      (currentPosition, currentRotation);
    // No parent, our result is final.
    if (parent==null)
      transform
        .set      (tmpMat);
    // There is a parent, transform accordingly.
    else {
      transform
        .set      (tmpMat.mulLeft(parent.transform));
    }
    for (final GameObject child : children)
      child.morph  (alpha);
  }
}
