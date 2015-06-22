package fr.pierreqr.communitrix.gameObjects;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import aurelienribon.tweenengine.TweenAccessor;

public class PerspectiveCameraAccessor implements TweenAccessor<PerspectiveCamera> {
  // Possible orders.
  public final static   int           TransX      = 1;
  public final static   int           TransY      = 2;
  public final static   int           TransZ      = 4;
  // Combinations. Only related are grouped, but it's possible to combine translation and rotations.
  public final static   int           TransXY     = TransX | TransY;
  public final static   int           TransXZ     = TransX | TransZ;
  public final static   int           TransYZ     = TransX | TransZ;
  public final static   int           TransXYZ    = TransX | TransY | TransZ;
  // Those are temporaries.
  private static        int           tmpInt      = 0;
  private final static  Vector3       tmpVec3     = new Vector3();

  @Override public int getValues (final PerspectiveCamera obj, final int type, final float[] retVals) {
    tmpInt  = 0;
    // Handle translations if required.
    if ((type & TransX)!=0 || (type & TransY)!=0 || (type & TransZ)!=0) {
      tmpVec3.x = obj.position.x;
      tmpVec3.y = obj.position.y;
      tmpVec3.z = obj.position.z;
      if (( type & TransX )!=0) retVals[tmpInt++] = tmpVec3.x;
      if (( type & TransY )!=0) retVals[tmpInt++] = tmpVec3.y;
      if (( type & TransZ )!=0) retVals[tmpInt++] = tmpVec3.z;
    }
    
    return tmpInt;
  }
  @Override
  public void setValues (final PerspectiveCamera obj, final int type, final float[] newVals) {
    tmpInt                        = 0;
    // Handle translations, if any.
    tmpVec3.x = obj.position.x;
    tmpVec3.y = obj.position.y;
    tmpVec3.z = obj.position.z;
    if (( type & TransX )!=0)     tmpVec3.x  = newVals[tmpInt++];
    if (( type & TransY )!=0)     tmpVec3.y  = newVals[tmpInt++];
    if (( type & TransZ )!=0)     tmpVec3.z  = newVals[tmpInt++];
 
    obj.position.set(tmpVec3);
  }
}
