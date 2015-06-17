package fr.pierreqr.communitrix.screens.inputControllers;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Expo;
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
import fr.pierreqr.communitrix.gameObjects.GameObjectAccessor;
import fr.pierreqr.communitrix.gameObjects.Piece;

public class ICLobby extends InputAdapter{
  private final static  Vector3             positiveX   = new Vector3( 1,  0,  0);
  private final static  Vector3             negativeX   = new Vector3(-1,  0,  0);
  private final static  Vector3             positiveY   = new Vector3( 0,  1,  0);
  private final static  Vector3             negativeY   = new Vector3( 0, -1,  0);
  private final static  Vector3             positiveZ   = new Vector3( 0,  0,  1);
  private final static  Vector3             negativeZ   = new Vector3( 0,  0, -1);
  
  private final         PerspectiveCamera   camMain;
  private final         Array<Piece>        instances;
  private final         TweenManager        tweener;
  private               Piece               selection   = null;
  private final         Vector3             dragOffset;
  private final         Vector3             position;
  
  public ICLobby (final PerspectiveCamera camMain, final Array<Piece> instances, final TweenManager tweener) {
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
    
    final int   order;
    final float target;
    if (axis==positiveX || axis==negativeX) {
      order               = GameObjectAccessor.TransX;
      target              = tmpVec3.x;
    }
    else if (axis==positiveY || axis==negativeY) {
      order               = GameObjectAccessor.TransY;
      target              = tmpVec3.y;
    }
    else {
      order               = GameObjectAccessor.TransZ;
      target              = tmpVec3.z;
    }
    
    Tween
      .to                 (selection, order, 0.2f)
      .target             (target)
      .ease               (Expo.OUT)
      .start              (tweener);
  }
  private void animateSelectionRotation (final Vector3 axis, final int angle) {
    int         order   = 0;
    int         target  = 0;
    if (axis==Vector3.X) {
     order    = GameObjectAccessor.RotX;
     target   = selection.targetAngles.x += angle;
    }
    else if (axis==Vector3.Y) {
      order   = GameObjectAccessor.RotY; 
      target  = selection.targetAngles.y += angle;
    }
    else if (axis==Vector3.Z) {
      order   = GameObjectAccessor.RotZ;
      target  = selection.targetAngles.z += angle;
    }
    
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
    if (Gdx.input.isKeyJustPressed(Keys.UP))
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
    if (Gdx.input.isKeyJustPressed(Keys.O))
      animateSelectionTranslation(positiveY);
    else if (Gdx.input.isKeyJustPressed(Keys.L))
      animateSelectionTranslation(negativeY);
  }
}
