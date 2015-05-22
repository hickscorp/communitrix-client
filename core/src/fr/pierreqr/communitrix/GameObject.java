package fr.pierreqr.communitrix;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class GameObject extends ModelInstance {
  // As for any class inheriting GameObject the bounding box will be the same, make this static.
  private final static    BoundingBox   bounds      = new BoundingBox();
  // Center and dimensions will be re-calculated based on radius.
  public final  Vector3         center      = new Vector3();
  public final  Vector3         dimensions  = new Vector3();
  public final  float           radius;
  // Those are temporaries.
  private final static  Vector3 tmpVec3     = new Vector3();
  
  public GameObject (Model model) {
    super(model);
    calculateBoundingBox  (bounds);
    bounds.getCenter      (center);
    bounds.getDimensions  (dimensions);
    radius                = dimensions.len() / 2f;
  }

  // Attaches an external ModelInstance to the root node of this instance.
  public void attachAt (final ModelInstance model, final float x, final float y, final float z) {
    for (final Node node : model.nodes)
      attachAt(node, x, y, z);
  }
  // Attaches an external node to the root node of this instance.
  public void attachAt (final Node node, final float x, final float y, final float z) {
    // Check whether the current root node is already the target's parent or not.
    final Node myNode           = nodes.get(0);
    if (myNode!=node.getParent()) {
      // Attach 
      node.attachTo         (myNode);
      node.translation.set  (x, y, z);
      calculateTransforms   ();
    }
  }
  // Detaches all children of the root node of this instance.
  public void detachAllNodes () {
    Node  myNode  = nodes.get(0);
    while (myNode.hasChildren())
      myNode.removeChild(myNode.getChild(0));
  }
  
  // Checks whether the current object is visible or not given a camera.
  public boolean isVisible (final Camera cam) {
    transform.getTranslation(tmpVec3);
    tmpVec3.add(center);
    return cam.frustum.sphereInFrustum(tmpVec3, radius);
  }
}
