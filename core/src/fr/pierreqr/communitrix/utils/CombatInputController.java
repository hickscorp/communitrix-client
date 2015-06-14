package fr.pierreqr.communitrix.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import fr.pierreqr.communitrix.gameObjects.GameObject;

public class CombatInputController extends InputAdapter{
  private final static  float               speed       = 0.5f;
  private final static  Vector3             positiveX   = new Vector3(speed, 0, 0);
  private final static  Vector3             positiveZ   = new Vector3(0, 0, speed);
  private final static  Vector3             negativeX   = new Vector3(-speed, 0, 0);
  private final static  Vector3             negativeZ   = new Vector3(0, 0, -speed);

  private final         PerspectiveCamera   camMain;
  private final         Array<GameObject>   instances;
  private               GameObject          selection   = null;
  private final         Vector3             dragOffset  = new Vector3();
  private final         Vector3             position    = new Vector3();
  
  public CombatInputController(final PerspectiveCamera camMain, final Array<GameObject> instances) {
    this.camMain    = camMain;
    this.instances  = instances;
  }
  
  @Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
    // Pick a ray from the cam.
    final Ray   ray         = camMain.getPickRay(screenX, screenY);
    // Dist will be a temp, while sDist will be the shortest found distance.
    float       dist, sDist = Float.MAX_VALUE;
    // Iterate through all instances.
    for (final GameObject obj : instances) {
      obj.transform.getTranslation(position);
      position.add      (obj.center);
      dist              = ray.origin.dst2(position);
      if (dist<sDist && Intersector.intersectRaySphere(ray, position, obj.radius, null)) {
        selection       = obj;
        sDist           = dist;
        // Store the translation for a late computing of the drag offset.
        obj.transform
        .getTranslation (dragOffset);
      }
    }
    if (selection!=null) {
      position
        .set(ray.direction)
        .scl(-ray.origin.y / ray.direction.y)
        .add(ray.origin);
      dragOffset.sub(position);
      return true;
    }
    else
      return false;
  }
  @Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
    if (selection==null)
      return    false;
    selection = null;
    return    true;
  }
  @Override public boolean touchDragged (int screenX, int screenY, int pointer) {
    if (selection==null)
      return false;
    final Ray   ray   = camMain.getPickRay(screenX, screenY);
    final float dist  = -ray.origin.y / ray.direction.y;
    position
      .set(ray.direction)
      .scl(dist)
      .add(ray.origin)
      .add(dragOffset);
    selection
      .transform
      .setTranslation(position);
    return true;
  }

  public void update () {
    if (selection==null)
      return;

    // Handle relative rotation.
    if (Gdx.input.isKeyJustPressed(Keys.RIGHT))
      selection.relativeRotation(position, Vector3.Y, 90f);
    else if (Gdx.input.isKeyJustPressed(Keys.LEFT))
      selection.relativeRotation(position, Vector3.Y, 90f);
    else if (Gdx.input.isKeyJustPressed(Keys.UP))
      selection.relativeRotation(position, Vector3.X, -90f);
    else if (Gdx.input.isKeyJustPressed(Keys.DOWN))
      selection.relativeRotation(position, Vector3.X, 90f);
    // Handle translation.
    if (Gdx.input.isKeyJustPressed(Keys.W))
      selection.relativeTranlation(position, positiveZ);
    else if(Gdx.input.isKeyJustPressed(Keys.S))
      selection.relativeTranlation(position, negativeZ);
    if (Gdx.input.isKeyJustPressed(Keys.A))
      selection.relativeTranlation(position, positiveX);
    else if (Gdx.input.isKeyJustPressed(Keys.D))
      selection.relativeTranlation(position, negativeX);
  }
}
