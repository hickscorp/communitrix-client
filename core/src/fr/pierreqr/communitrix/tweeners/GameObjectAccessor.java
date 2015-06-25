package fr.pierreqr.communitrix.tweeners;

import aurelienribon.tweenengine.TweenAccessor;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import fr.pierreqr.communitrix.gameObjects.GameObject;

public class GameObjectAccessor implements TweenAccessor<GameObject> {
  // Possible orders.
  public final static   int           TransX      = 1;
  public final static   int           TransY      = 2;
  public final static   int           TransZ      = 4;
  public final static   int           SLERP       = 8;
  // Those are temporaries.
  private static        int           tmpInt      = 0;
  private final static  Vector3       tmpVec3     = new Vector3();
  private final static  Quaternion    tmpQuat     = new Quaternion();

  @Override public int getValues (final GameObject obj, final int type, final float[] retVals) {
    tmpInt  = 0;
    // Handle translations if required.
    if ((type & TransX)!=0 || (type & TransY)!=0 || (type & TransZ)!=0) {
      obj.transform.getTranslation(tmpVec3);
      if ((type & TransX)!=0) retVals[tmpInt++] = tmpVec3.x;
      if ((type & TransY)!=0) retVals[tmpInt++] = tmpVec3.y;
      if ((type & TransZ)!=0) retVals[tmpInt++] = tmpVec3.z;
    }
    // Handle transitions via SLERP extrapolation.
    if ((type & SLERP)!=0)
      retVals[tmpInt++] = obj.slerpFactor;
    return tmpInt;
  }
  @Override
  public void setValues (final GameObject obj, final int type, final float[] newVals) {
    tmpInt                        = 0;
    // Handle translations, if any.
    obj.transform.getTranslation  (tmpVec3);
    if ((type & TransX)!=0)     tmpVec3.x  = newVals[tmpInt++];
    if ((type & TransY)!=0)     tmpVec3.y  = newVals[tmpInt++];
    if ((type & TransZ)!=0)     tmpVec3.z  = newVals[tmpInt++];
    // Transitions by SLERP interpolation.
    if ((type & SLERP)!=0) {
      obj.transform
        .getRotation(tmpQuat)
        .slerp(obj.targetRotation, ( obj.slerpFactor = newVals[tmpInt++] ));
      obj.transform.set(tmpVec3, tmpQuat);
    }
    // No rotation, just translate.
    else
      obj.transform.setTranslation(tmpVec3);
  }
}
