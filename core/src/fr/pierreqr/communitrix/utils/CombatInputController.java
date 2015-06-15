package fr.pierreqr.communitrix.utils;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Bounce;
import aurelienribon.tweenengine.equations.Expo;
import aurelienribon.tweenengine.equations.Linear;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.GameObjectAccessor;
import fr.pierreqr.communitrix.gameObjects.Piece;

public class CombatInputController extends InputAdapter{
  private final static  Vector3             positiveX   = new Vector3( 1,  0,  0);
  private final static  Vector3             positiveZ   = new Vector3( 0,  0,  1);
  private final static  Vector3             negativeX   = new Vector3(-1,  0,  0);
  private final static  Vector3             negativeZ   = new Vector3( 0,  0, -1);
  
  private final         PerspectiveCamera   camMain;
  private final         Array<Piece>        instances;
  private final         TweenManager        tweener;
  private               Piece               selection   = null;
  private final         Vector3             dragOffset;
  private final         Vector3             position;
  
  public CombatInputController (final PerspectiveCamera camMain, final Array<Piece> instances, final TweenManager tweener) {
    dragOffset      = new Vector3();
    position        = new Vector3();
    this.camMain    = camMain;
    this.instances  = instances;
    this.tweener    = tweener;
  }
  
  @Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
    // Reset selection.
    selection               = null;
    // Pick a ray from the cam.
    final Ray   ray         = camMain.getPickRay(screenX, screenY);
    // Dist will be a temp, while sDist will be the shortest found distance.
    float       dist, sDist = Float.MAX_VALUE;
    BoundingBox bounds      = null;
    // Iterate through all instances.
    for (final Piece obj : instances) {
      obj.transform
        .getTranslation (position);
      dist              = ray.origin.dst2(position);
      //if (dist<sDist && Intersector.intersectRaySphere(ray, position, obj.radius, null)) {
      if (dist<sDist) {
        bounds          = new BoundingBox(obj.bounds);
        bounds.min.add  (position);
        bounds.max.add  (position);
        if (Intersector.intersectRayBounds(ray, bounds, null)) {
          sDist         = dist;
          selection     = obj;
          position
            .set            (ray.direction)
            .scl            (-ray.origin.y / ray.direction.y)
            .add            (ray.origin);
          selection
            .transform
            .getTranslation (dragOffset)
            .sub            (position);
        }
      }
    }
    return selection!=null;
  }
  @Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
//    if (selection==null)
//      return    false;
//    selection = null;
//    return    true;
    return false;
  }
  @Override public boolean touchDragged (int screenX, int screenY, int pointer) {
    return false;
//    if (selection==null)
//      return false;
//    final Ray   ray   = camMain.getPickRay(screenX, screenY);
//    final float dist  = -ray.origin.y / ray.direction.y;
//    position
//      .set(ray.direction)
//      .scl(dist)
//      .add(ray.origin)
//      .add(dragOffset);
//    selection
//      .transform
//      .setTranslation(position);
//    return true;
  }
  
  private final static Quaternion tmpQuat = new Quaternion();
  private final static Vector3    tmpVec3 = new Vector3();

  private void animateSelectionTranslation (final Vector3 axis) {
    selection.targetTransform
      .getRotation        (tmpQuat)
      .nor                ();
    selection.targetTransform
      .getTranslation     (tmpVec3);
    selection.targetTransform
      .idt                ()
      .translate          (axis)
      .rotate             (tmpQuat)
      .trn                (tmpVec3);
    selection.targetTransform
      .getTranslation     (tmpVec3);
    
    Tween
      .to                 (selection, GameObjectAccessor.TransXZ, 5.0f)
      .target             (tmpVec3.x, tmpVec3.z)
      .ease               (Linear.INOUT)
      .start              (tweener);
  }
  private void animateSelectionRotation (final Vector3 axis, final float angle) {
    selection.targetTransform
      .getRotation(tmpQuat)
      .nor        ();
    selection.targetTransform
      .getTranslation(tmpVec3);
    selection.targetTransform
      .idt        ()
      .rotate     (axis, angle)
      .rotate     (tmpQuat)
      .trn        (tmpVec3);
    

    selection.targetTransform
      .getRotation(tmpQuat)
      .nor();
    int   order   = 0;
    float target  = 0.0f;
    if (axis==Vector3.X) {
      order   = GameObjectAccessor.RotX;
      target  = tmpQuat.getPitch();
    } else if (axis==Vector3.Y) {
      order   = GameObjectAccessor.RotY;
      target  = tmpQuat.getYaw();
    }
    if (target>=180.0f)
      target  -= 360.0f;
    else if (target<=-180.0f)
      target  += 360.0f;
    Tween
      .to                 (selection, order, 0.6f)
      .target             (target)
      .ease               (Expo.OUT)
      .start              (tweener);
    
    //selection.relativeRotation(axis, angle);
  }
  public void update () {
    if (selection==null)
      return;
    
    // Handle relative rotation.
    if (Gdx.input.isKeyJustPressed(Keys.RIGHT))
      animateSelectionRotation(Vector3.Y, 90);
    else if (Gdx.input.isKeyJustPressed(Keys.LEFT))
      animateSelectionRotation(Vector3.Y, -90);
    else if (Gdx.input.isKeyJustPressed(Keys.UP))
      animateSelectionRotation(Vector3.X, 90);
    else if (Gdx.input.isKeyJustPressed(Keys.DOWN))
      animateSelectionRotation(Vector3.X, -90);
    // Handle translation.
    if (Gdx.input.isKeyJustPressed(Keys.W))
      animateSelectionTranslation(positiveZ);
    else if(Gdx.input.isKeyJustPressed(Keys.S))
      animateSelectionTranslation(negativeZ);
    if (Gdx.input.isKeyJustPressed(Keys.A))
      animateSelectionTranslation(positiveX);
    else if (Gdx.input.isKeyJustPressed(Keys.D))
      animateSelectionTranslation(negativeX);
  }
}
