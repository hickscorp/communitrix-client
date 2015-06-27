package fr.pierreqr.communitrix.gameObjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import fr.pierreqr.communitrix.networking.shared.SHVector;

public class GameObject extends ModelInstance {
  // Center and dimensions will be re-calculated based on radius.
  public final      Vector3       center, dimensions;
  public final      BoundingBox   bounds;
  public            float         radius;
  
  public final      SHVector      targetPosition  = new SHVector();
  public final      Quaternion    targetRotation  = new Quaternion();
  public            float         slerpFactor;
  
  private final     Vector3       curPos          = new Vector3();
  private final     Quaternion    curRot          = new Quaternion();
  private final     Vector3       curScl          = new Vector3();
  private final     Matrix4       nestedTransform = new Matrix4();

  private           GameObject          parent    = null;
  public final      Array<GameObject>   children  = new Array<GameObject>();
  
  public GameObject (Model model) {
    super           (model);
    center          = new Vector3();
    dimensions      = new Vector3();
    bounds          = new BoundingBox();
    recomputeBounds ();
  }
  public void recomputeBounds () {
    calculateBoundingBox  (bounds);
    for (final GameObject child : children)
      bounds.ext          (child.bounds);
    bounds.getCenter      (center);
    bounds.getDimensions  (dimensions);
    radius                = dimensions.len() / 2f;
  }
  
  public void roundTargetRotation () {
    targetRotation.x    = Math.round(targetRotation.x * 100.0) / 100.0f;
    targetRotation.y    = Math.round(targetRotation.y * 100.0) / 100.0f;
    targetRotation.z    = Math.round(targetRotation.z * 100.0) / 100.0f;
    targetRotation.w    = Math.round(targetRotation.w * 100.0) / 100.0f;
  }
  
  public GameObject getParent () {
    return parent;
  }
  public void setParent (final GameObject newParent) {
    if (newParent==parent)  return;
    this.unparent();
    if (newParent!=parent)
      newParent.addChild(this);
  }
  public void unparent () {
    if (parent!=null)
      parent.removeChild    (this);
    parent                  = null;
  }
  
  public void addChild (final GameObject child) {
    child.parent                = this;
    children.add                (child);
    child.nestedTransform
      .set(child.targetPosition.x, child.targetPosition.y, child.targetPosition.z,
          child.targetRotation.x, child.targetRotation.y, child.targetRotation.z, child.targetRotation.w,
          1, 1, 1);
    recomputeBounds             ();
  }
  public void removeChild (final GameObject child) {
    child.unparent          ();
    children.removeValue    (child, true);
    recomputeBounds             ();
  }
  
  public void updateChildren (final boolean updateCurrentValues) {
    if (updateCurrentValues) {
      transform.getTranslation  (curPos);
      transform.getRotation     (curRot);
      transform.getScale        (curScl);
    }
    for (final GameObject child : children)
      child.transform
        .set        (curPos, curRot, curScl)
        .mul        (child.nestedTransform);
  }
  
  public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
    super.getRenderables(renderables, pool);
    for (final GameObject child : children)
      child.getRenderables(renderables, pool);
  }
  
  // Checks whether the current object is visible or not given a camera.
  public boolean isVisible (final Camera cam) {
    return cam.frustum.boundsInFrustum(bounds);
  }
}
