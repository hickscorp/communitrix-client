package fr.pierreqr.communitrix.gameObjects;

import aurelienribon.tweenengine.TweenAccessor;
import com.badlogic.gdx.math.Vector3;

public class GameObjectAccessor implements TweenAccessor<GameObject> {
  public final static   int           POSITION_XYZ  = 1;
  public final static   int           ROTATION_XYZ  = 2;
  // Those are temporaries.
  private final static  Vector3       tmpVec3       = new Vector3();

  @Override
  public int getValues(GameObject obj, int type, float[] retVals) {
    switch (type) {
      case POSITION_XYZ:
        obj.transform.getTranslation(tmpVec3);
        retVals[0]  = tmpVec3.x;
        retVals[1]  = tmpVec3.y;
        retVals[2]  = tmpVec3.z;
        return 3;
      case ROTATION_XYZ:
        return 3;
      default:
        assert false;
        return -1;
    }
  }
  @Override
  public void setValues(GameObject obj, int type, float[] newVals) {
    switch (type) {
      case POSITION_XYZ:
        obj.transform.setTranslation(tmpVec3.set(newVals));
        break;
      case ROTATION_XYZ:
        break;
      default:
        assert false;
        break;
    }
  }
}