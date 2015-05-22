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
  
  // Attaches an external node to the root node of this instance.
  public void attachAt (Node node, final float x, final float y, final float z) {
    if (nodes.get(0)!=node.getParent()) {
      node.attachTo(nodes.get(0));
      node.translation.set(x, y, z);
      calculateTransforms();
    }
  }
  // Detaches all children of the root node of this instance.
  public void detachAllNodes () {
    Node  myNode  = nodes.get(0);
    while (myNode.hasChildren())
      myNode.removeChild(myNode.getChild(0));
  }
  
  public boolean isVisible (final Camera cam) {
    transform.getTranslation(tmpVec3);
    tmpVec3.add(center);
    return cam.frustum.sphereInFrustum(tmpVec3, radius);
  }
}
