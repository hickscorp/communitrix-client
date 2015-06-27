package fr.pierreqr.communitrix.tweeners;

import com.badlogic.gdx.math.Vector3;
import aurelienribon.tweenengine.TweenAccessor;
import fr.pierreqr.communitrix.gameObjects.Camera;

public class CameraAccessor implements TweenAccessor<Camera> {
  // Possible orders.
  public final static   int           CameraTrans  = 1;
  public final static   int           TargetTrans  = 2;
  
  private static        int           tmpInt       = 0;
  
  // Combinations. Only related are grouped, but it's possible to combine translation and rotations.
  public final static   int           Trans        = CameraTrans | TargetTrans;

  @Override public int getValues (final Camera obj, final int type, final float[] retVals) {
    tmpInt  = 0;
    // Handle translations if required.
    if ((type & CameraTrans)!=0) {
      retVals[tmpInt++] = obj.position.x;
      retVals[tmpInt++] = obj.position.y;
      retVals[tmpInt++] = obj.position.z;
    }
    // Handle rotations if required.
    if ((type & TargetTrans)!=0) {
      retVals[tmpInt++] = obj.target.x;
      retVals[tmpInt++] = obj.target.y;
      retVals[tmpInt++] = obj.target.z;
    }
    return tmpInt;
  }
  @Override
  public void setValues (final Camera obj, final int type, final float[] newVals) {
    tmpInt = 0;
    // Handle translations, if any.
    if (( type & CameraTrans )!=0)
      obj.position.set  (newVals[tmpInt++], newVals[tmpInt++], newVals[tmpInt++]);
    if ((type & TargetTrans)!=0)
      obj.lookAt        (newVals[tmpInt++], newVals[tmpInt++], newVals[tmpInt++]);
    // Prevent the camera from rolling too much.
    obj.up.set          (Vector3.Y);
    obj.update          ();
  }
}
