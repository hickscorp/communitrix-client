package fr.pierreqr.communitrix.gameObjects;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquation;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class GameObject extends ModelInstance {
  // Center and dimensions will be re-calculated based on radius.
  public final          Vector3             center      = new Vector3();
  public final          Vector3             dimensions  = new Vector3();
  public final          BoundingBox         bounds      = new BoundingBox();
  public                float               radius;
  // If this object needs to be animated, store its properties.
  public final          Animator            anim        = new Animator(this);

  // Temporaries.
  private final static  Matrix4     tmpMat        = new Matrix4();
  private final static  Vector3     tmpPos        = new Vector3();
  private final static  Quaternion  tmpRot        = new Quaternion();

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
    return cam.frustum.boundsInFrustum(bounds);
  }
  
  
  public static class Animator {
    private final     GameObject    target;
    // Whenever an object is animated, its bounds will be recomputed into fakeBounds.
    public  final     BoundingBox   fakeBounds      = new BoundingBox();
    // For animating purposes, we store starting position / rotation.
    private final     Vector3       startPosition   = new Vector3();
    private final     Quaternion    startRotation   = new Quaternion();
    // The target properties represent where the object will end up being.
    public  final     Vector3       targetPosition  = new Vector3();
    public  final     Quaternion    targetRotation  = new Quaternion();
    
    public Animator (final GameObject owner) {
      target  = owner;
    }
    public void reset () {
      startPosition.set( target.transform.getTranslation(targetPosition) );
      startRotation.set( target.transform.getRotation   (targetRotation) );
    }
    
    public BaseTween<?> start (final TweenManager tweener, final float duration, final TweenEquation ease) {
      tweener.killTarget              (target);
      // Store current values into start markers.
      target.transform.getTranslation (startPosition);
      target.transform.getRotation    (startRotation).nor();
      // Normalize.
      targetPosition.x    = Math.round(targetPosition.x * 10.0f) / 10.0f;
      targetPosition.y    = Math.round(targetPosition.y * 10.0f) / 10.0f;
      targetPosition.z    = Math.round(targetPosition.z * 10.0f) / 10.0f;
      targetRotation.x    = Math.round(targetRotation.x * 100.0f) / 100.0f;
      targetRotation.y    = Math.round(targetRotation.y * 100.0f) / 100.0f;
      targetRotation.z    = Math.round(targetRotation.z * 100.0f) / 100.0f;
      targetRotation.w    = Math.round(targetRotation.w * 100.0f) / 100.0f;
      targetRotation.nor  ();
      // Store end markers.
      fakeBounds 
        .set                          (target.bounds)
        .mul                          (tmpMat.idt().rotate(targetRotation));
      return Tween
        .to                           (target, 0, duration)
        .target                       (1.0f)
        .ease                         (ease)
        .start                        (tweener);
    }
    public void morph (final float alpha) {
      tmpPos
        .set      (startPosition)
        .lerp     (targetPosition, alpha);
      tmpRot
        .set      (startRotation)
        .slerp    (targetRotation, alpha)
        .nor      ();
      target.transform
        .set      (tmpPos, tmpRot);
    }
  }
}
