package fr.pierreqr.communitrix.gameObjects;

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
  public final      Vector3       center, dimensions;
  public final      BoundingBox   bounds;
  public final      BoundingBox   fakeBounds;
  public            float         radius;
  
  public final      Vector3       startPosition   = new Vector3();
  public final      Vector3       targetPosition  = new Vector3();
  public final      Quaternion    startRotation   = new Quaternion();
  public final      Quaternion    targetRotation  = new Quaternion();
  public            float         lerpFactor, slerpFactor;
  
  public GameObject (Model model) {
    super           (model);
    center          = new Vector3();
    dimensions      = new Vector3();
    bounds          = new BoundingBox();
    fakeBounds      = new BoundingBox();
    recomputeBounds ();
  }
  public void recomputeBounds () {
    calculateBoundingBox  (bounds);
    bounds.getCenter      (center);
    bounds.getDimensions  (dimensions);
    radius                = dimensions.len() / 2f;
  }
  
  private final static Matrix4 tmpMat = new Matrix4();
  public void prepareTweening (final TweenManager tweener) {
    tweener.killTarget        (this);
    lerpFactor                = 0.0f;
    slerpFactor               = 0.0f;
    transform.getTranslation  (startPosition);
    transform.getRotation     (startRotation);
    roundTargetRotation       ();
    fakeBounds
      .set                    (bounds)
      .mul                    (tmpMat.idt().rotate(targetRotation));
  }
  private void roundTargetRotation () {
    targetRotation.nor        ();
    targetRotation.x    = Math.round(targetRotation.x * 100.0) / 100.0f;
    targetRotation.y    = Math.round(targetRotation.y * 100.0) / 100.0f;
    targetRotation.z    = Math.round(targetRotation.z * 100.0) / 100.0f;
    targetRotation.w    = Math.round(targetRotation.w * 100.0) / 100.0f;
  }
  
  // Checks whether the current object is visible or not given a camera.
  public boolean isVisible (final Camera cam) {
    return cam.frustum.boundsInFrustum(bounds);
  }
}
