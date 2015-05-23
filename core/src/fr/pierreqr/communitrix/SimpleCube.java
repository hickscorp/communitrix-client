package fr.pierreqr.communitrix;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class SimpleCube extends GameObject {
  // Members.
  public                int           selectedFace  = 0;
  // Temporaries.
  private final static  Vector3       tmpXAxis      = new Vector3();
  private final static  Vector3       tmpYAxis      = new Vector3();
  private final static  Vector3       tmpZAxis      = new Vector3();
  private final static  Vector3       tmpPosition   = new Vector3();
  private final static  Quaternion    tmpRotation   = new Quaternion();
  
  public SimpleCube () throws Exception {
    super(LogicManager.getInstance().getModel("Sphere"));
  }
  
  public void rotate (final Camera cam, final Vector3 axis, final float angle) {
    // Get rotation axis based on camera.
    tmpZAxis.set(cam.direction).scl(-1);
    tmpXAxis.set(tmpZAxis).crs(cam.up);
    tmpYAxis.set(tmpXAxis).crs(cam.direction);
    // Store original position / rotation.
    transform.getTranslation(tmpPosition);
    transform.getRotation(tmpRotation).nor();
    transform.idt();
    // Rotate model.
    if (axis==Vector3.X)
      transform.rotate(tmpXAxis, angle);
    else if (axis==Vector3.Y)
      transform.rotate(tmpYAxis, angle);
    transform.rotate(tmpRotation);
    transform.trn(tmpPosition);    
  }
}
