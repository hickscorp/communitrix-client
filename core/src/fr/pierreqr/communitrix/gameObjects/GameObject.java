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
  public final          Vector3             center      = new Vector3();
  public final          Vector3             dimensions  = new Vector3();
  public final          BoundingBox         bounds      = new BoundingBox();
  public                float               radius;
  // If this object needs to be animated, store its properties.
  public final          Animator            anim        = new Animator(this);

  // Temporaries.
  private final static  Matrix4             tmpMat      = new Matrix4();
  private final static  Vector3             tmpPos      = new Vector3();
  private final static  Quaternion          tmpRot      = new Quaternion();

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
  
  // Checks whether the current object is visible or not given a camera.
  public boolean isVisible (final Camera cam) {
    return cam.frustum.boundsInFrustum(anim.fakeBounds);
  }
  
  public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
    super.getRenderables (renderables, pool);
    for (final Animator child : anim.children)
      child.target.getRenderables (renderables, pool);
  }
  
  // Proxy some of the animator method.
  public GameObject getParent () {
    return anim.parent.target;
  }
  public void setParent (final GameObject newParent, boolean applyTransforms) {
    anim.setParent      (newParent.anim, applyTransforms);
  }
  public void unparent (boolean applyTransforms) {
    anim.unparent       (applyTransforms);
  }
  public void addChild (final GameObject child, boolean applyTransforms) {
    anim.addChild       (child.anim, applyTransforms);
  }
  public void removeChild (final GameObject child, boolean applyTransforms) {
    anim.removeChild    (child.anim, applyTransforms);
  }
  
  public static class Animator {
    private final     GameObject    target;
    private final     Matrix4       transform;
    // Whenever an object is animated, its bounds will be recomputed into fakeBounds.
    public  final     BoundingBox   fakeBounds      = new BoundingBox();
    // For animating purposes, we store starting position / rotation.
    public  final     Vector3       startPosition   = new Vector3();
    public  final     Quaternion    startRotation   = new Quaternion();
    // The current properties represent where the object is right now.
    public  final     Vector3       currentPosition = new Vector3();
    public  final     Quaternion    currentRotation = new Quaternion();
    // The target properties represent where the object will end up being.
    public  final     Vector3       targetPosition  = new Vector3();
    public  final     Quaternion    targetRotation  = new Quaternion();
    
    // Parenting options.
    public            Animator        parent      = null;
    public final      Array<Animator> children    = new Array<Animator>();

    public Animator (final GameObject owner) {
      target      = owner;
      transform   = owner.transform;
    }

    public Animator getParent () {
      return parent;
    }
    public void setParent (final Animator newParent, boolean applyTransforms) {
      unparent                      (applyTransforms);
      if ((parent = newParent)==null)
        return;
      parent.children.add           (this);
      if (applyTransforms) {
        parent.transform
          .getRotation              (tmpRot)
          .conjugate                ();
        currentPosition
          .sub                      (parent.transform.getTranslation(tmpPos))
          .mul                      (tmpRot);
        startPosition
          .set                      (currentPosition);
        
        currentRotation
          .mulLeft                  (tmpRot);
        startRotation
          .set                      (currentRotation);
        Communitrix.round(
          targetRotation
            .mul                      (tmpRot)
        );
      }
    }
    public void unparent (boolean applyTransforms) {
      if (parent==null)           return;
      parent.children.removeValue (this, true);
      if (applyTransforms) {
        // The starting point for all the animations will be the current world transform of the object.
        currentPosition.set(startPosition.set(transform.getTranslation(tmpPos)));
        currentRotation.set(startRotation.set(transform.getRotation(tmpRot)));
        Communitrix.round(targetPosition.sub(parent.targetPosition));
        Communitrix.round(targetRotation.set(parent.targetRotation));
      }
      parent                      = null;
    }
    public void addChild (final Animator child, boolean applyTransforms) {
      if (child!=null)
        child.setParent   (this, applyTransforms);
    }
    public void removeChild (final Animator child, boolean applyTransforms) {
      if (child!=null)
        child.setParent   (null, applyTransforms);
    }
    
    public void reset () {
      // Reset starting, current and target values.
      Communitrix.round         (transform.getTranslation  (currentPosition));
      Communitrix.round         (transform.getRotation     (currentRotation));
      startPosition.set         (currentPosition);
      startRotation.set         (currentRotation);
      targetPosition.set        (currentPosition);
      targetRotation.set        (currentRotation);
    }
    
    public BaseTween<?> start (final TweenManager tweener, final float duration, final TweenEquation ease) {
      // Prepare this object and all his children.
      prepare             (tweener);
      for (final Animator child : children)
        child.prepare     (tweener);
      // Finally animate.
      return Tween
        .to               (target, 0, duration)
        .target           (1.0f)
        .ease             (ease)
        .start            (tweener);
    }
    private void prepare (final TweenManager tweener) {
      // Round targets.
      Communitrix.round   (targetPosition);
      Communitrix.round   (targetRotation);
      // Kill any pending animation involving this object.
      tweener.killTarget  (target);
      // Store current values into current markers.
      startPosition.set   (currentPosition);
      startRotation.set   (currentRotation);
      // Handle new bounds.
      Communitrix.round(
        fakeBounds
          .set              (target.bounds)
          .mul              (tmpMat.idt().rotate(targetRotation))
      );
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
      for (final Animator child : children)
        child.morph  (alpha);
    }
  }
}
