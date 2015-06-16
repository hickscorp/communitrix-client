package fr.pierreqr.communitrix.gameObjects;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import fr.pierreqr.communitrix.networking.shared.SHVector;

public class GameObject extends ModelInstance {
  // Center and dimensions will be re-calculated based on radius.
  public final          Vector3       center, dimensions;
  public                BoundingBox   bounds;
  public                float         radius;

  public                Matrix4       targetTransform;
  
  public                Vector3       currentAngles = new Vector3();
  public                SHVector      targetAngles  = new SHVector();
  
  // Those are temporaries.
  private final static  Vector3       tmpVec3       = new Vector3();
  private final static  Quaternion    tmpQuat       = new Quaternion();
  
  public GameObject (Model model) {
    super           (model);
    center          = new Vector3();
    dimensions      = new Vector3();
    bounds          = new BoundingBox();
    recomputeBounds ();
  }
  public void recomputeBounds () {
    calculateBoundingBox  (bounds);
    bounds.getCenter      (center);
    bounds.getDimensions  (dimensions);
    radius                = dimensions.len() / 2f;
  }

  // Checks whether the current object is visible or not given a camera.
  public boolean isVisible (final Camera cam) {
    transform.getTranslation(tmpVec3);
    tmpVec3.add(center);
    return cam.frustum.boundsInFrustum(bounds);
    //return cam.frustum.sphereInFrustum(tmpPosition, radius);
  }
  
  public void relativeTranlation (final Vector3 direction) {
    transform
      .getRotation(tmpQuat)
      .nor();
    transform
      .getTranslation(tmpVec3);
    transform
      .idt        ()
      .translate  (direction)
      .rotate     (tmpQuat)
      .trn        (tmpVec3);
  }
  public void relativeRotation (final Vector3 direction, final float angle) {
    transform
      .getRotation(tmpQuat)
      .nor        ();
    transform
      .getTranslation(tmpVec3);
    transform
      .idt        ()
      .rotate     (direction, angle)
      .rotate     (tmpQuat)
      .trn        (tmpVec3);
  }
}
