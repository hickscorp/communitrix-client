package fr.pierreqr.communitrix.gameObjects;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.graphics.Camera;

public class CameraAccessor implements TweenAccessor<Camera> {
  public final static   int           POSITION_XYZ  = 1;
  public final static   int           ROTATION_XYZ  = 2;

  @Override
  public int getValues(Camera obj, int type, float[] retVals) {
    switch (type) {
      case POSITION_XYZ:
        retVals[0]  = obj.position.x;
        retVals[1]  = obj.position.y;
        retVals[2]  = obj.position.z;
        return 3;
      case ROTATION_XYZ:
        return 3;
      default:
        assert false;
        return -1;
    }
  }
  @Override
  public void setValues(Camera obj, int type, float[] newVals) {
    switch (type) {
      case POSITION_XYZ:
        obj.position.set(newVals);
        break;
      case ROTATION_XYZ:
        break;
      default:
        assert false;
        break;
    }
  }
}
