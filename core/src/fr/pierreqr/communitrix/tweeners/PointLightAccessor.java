package fr.pierreqr.communitrix.tweeners;

import aurelienribon.tweenengine.TweenAccessor;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;

public class PointLightAccessor implements TweenAccessor<PointLight> {
  // Possible orders.
  public final static   int           TransX      = 1;
  public final static   int           TransY      = 2;
  public final static   int           TransZ      = 4;
  // Combinations.
  public final static   int           TransXYZ    = TransX | TransY | TransZ;
  // Those are temporaries.
  private static        int           tmpInt      = 0;

  @Override public int getValues (final PointLight obj, final int type, final float[] retVals) {
    tmpInt  = 0;
    // Handle translations if required.
    if ((type & TransX)!=0)   retVals[tmpInt++] = obj.position.x;
    if ((type & TransY)!=0)   retVals[tmpInt++] = obj.position.y;
    if ((type & TransZ)!=0)   retVals[tmpInt++] = obj.position.z;
    return tmpInt;
  }
  @Override public void setValues (final PointLight obj, final int type, final float[] newVals) {
    tmpInt          = 0;
    // Handle translations, if any.
    if ((type & TransX)!=0)   obj.position.x    = newVals[tmpInt++];
    if ((type & TransY)!=0)   obj.position.y    = newVals[tmpInt++];
    if ((type & TransZ)!=0)   obj.position.z    = newVals[tmpInt++];
  }
}
