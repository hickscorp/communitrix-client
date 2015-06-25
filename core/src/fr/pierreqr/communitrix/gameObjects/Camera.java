package fr.pierreqr.communitrix.gameObjects;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

public class Camera extends PerspectiveCamera {
  public final    Vector3     target      = new Vector3();

  public Camera (final float fov, final float viewportWidth, final float viewportHeight) {
    super           (fov, viewportWidth, viewportHeight);
  }
  
  public void lookAt (final Vector3 newTarget) {
    super.lookAt    (newTarget.x, newTarget.y, newTarget.z);
    target.set      (newTarget);
  }
  public void lookAt (final float x, final float y, final float z) {
    super.lookAt    (x, y, z);
    target.set      (x, y, z);
  }
}
