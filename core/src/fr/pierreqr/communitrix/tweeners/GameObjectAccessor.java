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
  public final static   int           ScaleX      = 8;
  public final static   int           ScaleY      = 16;
  public final static   int           ScaleZ      = 32;
  public final static   int           SLERP       = 64;
  // Combinations.
  public final static   int           TransXYZ    = TransX | TransY | TransZ;
  public final static   int           ScaleXYZ    = ScaleX | ScaleY | ScaleZ;
  // Those are temporaries.
  private static        int           tmpInt      = 0;
  private final static  Vector3       tmpPos      = new Vector3();
  private final static  Vector3       tmpScl      = new Vector3();
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
    // Handle scaling if required.
    if ((type & ScaleX)!=0 || (type & ScaleY)!=0 || (type & ScaleZ)!=0) {
      obj.transform.getScale(tmpScl);
      if ((type & ScaleX)!=0)   retVals[tmpInt++] = tmpScl.x;
      if ((type & ScaleY)!=0)   retVals[tmpInt++] = tmpScl.y;
      if ((type & ScaleZ)!=0)   retVals[tmpInt++] = tmpScl.z;
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
    
    // Handle translations, if any.
    if ((type & ScaleX)!=0 || (type & ScaleY)!=0 || (type & ScaleZ)!=0) {
      obj.transform.getScale(tmpScl);
      if ((type & ScaleX)!=0)     tmpScl.x  = newVals[tmpInt++];
      if ((type & ScaleY)!=0)     tmpScl.y  = newVals[tmpInt++];
      if ((type & ScaleZ)!=0)     tmpScl.z  = newVals[tmpInt++];
    }
    else
      tmpScl.set(1, 1, 1);

    // Transitions by SLERP interpolation.
    if ((type & SLERP)!=0) {
      obj.transform
        .getRotation(tmpRot)
        .slerp(obj.targetRotation, ( obj.slerpFactor = newVals[tmpInt++] )).nor();
    }
    else
      tmpRot.set(obj.targetRotation);
    
    obj.transform.set(tmpPos, tmpRot, tmpScl);
  }
}
