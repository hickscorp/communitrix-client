package fr.pierreqr.communitrix.tweeners;

import aurelienribon.tweenengine.TweenAccessor;
import fr.pierreqr.communitrix.gameObjects.GameObject;

public class GameObjectAccessor implements TweenAccessor<GameObject> {
  @Override public int getValues (final GameObject obj, final int type, final float[] retVals) {
    retVals[0]          = 0.0f;
    return 1;
  }
  @Override public void setValues (final GameObject obj, final int type, final float[] newVals) {
    obj.anim.morph      (newVals[0]);
  }
}
