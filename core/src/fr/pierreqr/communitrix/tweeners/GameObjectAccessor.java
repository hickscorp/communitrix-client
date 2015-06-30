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
  // Combinations.
  public final static   int           TransXYZ    = TransX | TransY | TransZ;
  // Those are temporaries.
  private static        int           tmpInt      = 0;
  private final static  Vector3       tmpPos      = new Vector3();
  private final static  Quaternion    tmpRot      = new Quaternion();

  @Override public int getValues (final GameObject obj, final int type, final float[] retVals) {
    tmpInt  = 0;
    // Handle translations if required.
    if ((type & TransX)!=0 || (type & TransY)!=0 || (type & TransZ)!=0) {
      obj.transform.getTranslation(tmpPos);
      if ((type & TransX)!=0)   retVals[tmpInt++] = tmpPos.x;
      if ((type & TransY)!=0)   retVals[tmpInt++] = tmpPos.y;
      if ((type & TransZ)!=0)   retVals[tmpInt++] = tmpPos.z;
    }
    // Handle transitions via SLERP extrapolation.
    if ((type & SLERP)!=0)      retVals[tmpInt++] = obj.slerpFactor;
    return tmpInt;
  }
  @Override public void setValues (final GameObject obj, final int type, final float[] newVals) {
    tmpInt          = 0;
    // Handle translations, if any.
    obj.transform.getTranslation(tmpPos);
    if ((type & TransX)!=0 || (type & TransY)!=0 || (type & TransZ)!=0) {
      if ((type & TransX)!=0)     tmpPos.x  = newVals[tmpInt++];
      if ((type & TransY)!=0)     tmpPos.y  = newVals[tmpInt++];
      if ((type & TransZ)!=0)     tmpPos.z  = newVals[tmpInt++];
    }
    
    // Transitions by SLERP interpolation.
    if ((type & SLERP)!=0) {
      obj.transform
        .getRotation(tmpRot)
        .slerp(obj.targetRotation, ( obj.slerpFactor = newVals[tmpInt++] ));
    }
    else
      tmpRot.set(obj.targetRotation);
    
    obj.transform.set       (tmpPos, tmpRot);
  }
}
