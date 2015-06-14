package fr.pierreqr.communitrix.gameObjects;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class GameObject extends ModelInstance {
  // Center and dimensions will be re-calculated based on radius.
  public final          Vector3       center        = new Vector3();
  public final          Vector3       dimensions    = new Vector3();
  public                float         radius;

  // Those are temporaries.
  private final static  Vector3       tmpPosition   = new Vector3();
  private final static  BoundingBox   tmpBounds     = new BoundingBox();
  private final static  Quaternion    tmpQuat       = new Quaternion();
  
  public GameObject (Model model) {
    super           (model);
    recomputeBounds ();
  }
  public void recomputeBounds () {
    calculateBoundingBox    (tmpBounds);
    tmpBounds.getCenter     (center);
    tmpBounds.getDimensions (dimensions);
    radius                  = dimensions.len() / 2f;
  }

  // Checks whether the current object is visible or not given a camera.
  public boolean isVisible (final Camera cam) {
    transform.getTranslation(tmpPosition);
    tmpPosition.add(center);
    return cam.frustum.sphereInFrustum(tmpPosition, radius);
  }
  
  public void relativeRotation (final Vector3 position, final Vector3 direction, final float angle) {
    transform.getRotation(tmpQuat).nor();
    transform.getTranslation(position);
    transform.idt();
    transform.rotate(direction, angle);
    transform.rotate(tmpQuat);
    transform.trn(position);
  }
  
  public void relativeTranlation (final Vector3 position, final Vector3 direction) {
    transform.getRotation(tmpQuat).nor();
    transform.getTranslation(position);
    transform.idt();
    transform.translate(direction);
    transform.rotate(tmpQuat);
    transform.trn(position);
  }

}
