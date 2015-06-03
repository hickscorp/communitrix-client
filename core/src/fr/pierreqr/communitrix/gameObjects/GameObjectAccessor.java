package fr.pierreqr.communitrix.gameObjects;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class GameObjectAccessor implements TweenAccessor<GameObject> {
  // Possible orders.
  public final static   int           TransX      = 1;
  public final static   int           TransY      = 2;
  public final static   int           TransZ      = 4;
  public final static   int           RotX        = 8;
  public final static   int           RotY        = 16;
  public final static   int           RotZ        = 32;
  // Combinations. Only related are grouped, but it's possible to combine tranlation and rotations.
  public final static   int           TransXY     = TransX | TransY;
  public final static   int           TransXZ     = TransX | TransZ;
  public final static   int           TransYZ     = TransX | TransZ;
  public final static   int           TransXYZ    = TransX | TransY | TransZ;
  public final static   int           RotXY       = RotX | RotY;
  public final static   int           RotXZ       = RotX | RotZ;
  public final static   int           RotYZ       = RotX | RotZ;
  public final static   int           RotXYZ      = RotX | RotY | RotZ;
  // Those are temporaries.
  private static        int           tmpInt      = 0;
  private final static  Vector3       tmpVec3     = new Vector3();
  private final static  Quaternion    tmpQuat     = new Quaternion();

  @Override
  public int getValues(final GameObject obj, final int type, final float[] retVals) {
    tmpInt  = 0;
    // Handle translations if required.
    if ((type & TransX)!=0 || (type & TransY)!=0 || (type & TransZ)!=0) {
      obj.transform.getTranslation(tmpVec3);
      if (( type & TransX )!=0) retVals[tmpInt++] = tmpVec3.x;
      if (( type & TransY )!=0) retVals[tmpInt++] = tmpVec3.y;
      if (( type & TransZ )!=0) retVals[tmpInt++] = tmpVec3.z;
    }
    // Handle rotations if required.
    if ((type & RotX)!=0 || (type & RotY)!=0 || (type & RotZ)!=0) {
      obj.transform.getRotation(tmpQuat);
      if (( type & RotX )!=0)   retVals[tmpInt++] = tmpQuat.getPitch();
      if (( type & RotY )!=0)   retVals[tmpInt++] = tmpQuat.getYaw();
      if (( type & RotZ )!=0)   retVals[tmpInt++] = tmpQuat.getRoll();
    }
    return tmpInt;
  }
  @Override
  public void setValues(final GameObject obj, final int type, float[] newVals) {
    tmpInt                        = 0;
    // Handle translations, if any.
    obj.transform.getTranslation(tmpVec3);
    if (( type & TransX )!=0)   tmpVec3.x  = newVals[tmpInt++];
    if (( type & TransY )!=0)   tmpVec3.y  = newVals[tmpInt++];
    if (( type & TransZ )!=0)   tmpVec3.z  = newVals[tmpInt++];
    // Handle rotations, if any.
    if ((type & RotX)!=0 || (type & RotY)!=0 || (type & RotZ)!=0) {
      if (( type & RotX )!=0)   obj.transform.setToRotation(Vector3.X, newVals[tmpInt++]);
      if (( type & RotY )!=0)   obj.transform.setToRotation(Vector3.Y, newVals[tmpInt++]); 
      if (( type & RotZ )!=0)   obj.transform.setToRotation(Vector3.Z, newVals[tmpInt++]);
    }
    obj.transform.setTranslation  (tmpVec3);
  }
}
