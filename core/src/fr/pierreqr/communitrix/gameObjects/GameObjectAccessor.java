package fr.pierreqr.communitrix.gameObjects;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.math.Vector3;

public class GameObjectAccessor implements TweenAccessor<GameObject> {
  public final static   int           POSITION_X    = 1;
  public final static   int           POSITION_Y    = 2;
  public final static   int           POSITION_Z    = 4;
  public final static   int           POSITION_XY   = 3;
  public final static   int           POSITION_XZ   = 5;
  public final static   int           POSITION_YZ   = 6;
  public final static   int           POSITION_XYZ  = 7;
  // Those are temporaries.
  private static        int           tmpInt        = 0;
  private final static  Vector3       tmpVec3       = new Vector3();

  @Override
  public int getValues(GameObject obj, int type, float[] retVals) {
    tmpInt       = 0;
    obj.transform.getTranslation  (tmpVec3);
    if (( type & POSITION_X )!=0) retVals[tmpInt++] = tmpVec3.x;
    if (( type & POSITION_Y )!=0) retVals[tmpInt++] = tmpVec3.y;
    if (( type & POSITION_Z )!=0) retVals[tmpInt++] = tmpVec3.z;
    return tmpInt;
  }
  @Override
  public void setValues(GameObject obj, int type, float[] newVals) {
    tmpInt                        = 0;
    obj.transform.getTranslation  (tmpVec3);
    if (( type & POSITION_X )!=0) tmpVec3.x  = newVals[tmpInt++];
    if (( type & POSITION_Y )!=0) tmpVec3.y  = newVals[tmpInt++];
    if (( type & POSITION_Z )!=0) tmpVec3.z  = newVals[tmpInt++];
    obj.transform.setTranslation  (tmpVec3);
  }
}
