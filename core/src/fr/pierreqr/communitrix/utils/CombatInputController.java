package fr.pierreqr.communitrix.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.GameObject;

public class CombatInputController extends InputAdapter{
  private final         PerspectiveCamera   camMain;
  private final         Array<GameObject>   instances;
  private               int                 selected  =   -1; 
  private               Vector3             position  =   new Vector3();
  private static final  Quaternion          tmpQuat   =   new Quaternion();
  private static final  Communitrix         ctx       =   Communitrix.getInstance();

  private final         float               speed     =   .5f; 
  private final         Vector3             positiveX =   new Vector3(speed,.0f,.0f);
  private final         Vector3             positiveZ =   new Vector3(.0f,.0f,speed);
  private final         Vector3             negativeX =   new Vector3(-speed,.0f,.0f);
  private final         Vector3             negativeZ =   new Vector3(.0f,.0f,-speed);
  
  public CombatInputController(final PerspectiveCamera camMain, final Array<GameObject> instances) {
    this.camMain = camMain;
    this.instances = instances;
  }
  
  @Override
  public boolean touchDown (int screenX, int screenY, int pointer, int button) {
      selected = getObject(screenX, screenY);
      return selected >= 0;
  }

  @Override
  public boolean touchDragged (int screenX, int screenY, int pointer) {
    if (selected < 0) 
      return false;
    Ray ray = camMain.getPickRay(screenX, screenY);
    final float distance = -ray.origin.y / ray.direction.y;
    position.set(ray.direction).scl(distance).add(ray.origin);
    instances.get(selected).transform.setTranslation(position);
    return true;
  }

  @Override
  public boolean touchUp (int screenX, int screenY, int pointer, int button) {
      return false;
  }
  
  
  public int getObject (int screenX, int screenY) {
      Ray ray = camMain.getPickRay(screenX, screenY);
      int result = -1;
      float distance = -1;
      for (int i = 0; i < instances.size; ++i) {
          final GameObject instance = instances.get(i);
          instance.transform.getTranslation(position);
          position.add(instance.center);
          float dist2 = ray.origin.dst2(position);
          if (distance >= 0f && dist2 > distance) continue;
          if (Intersector.intersectRaySphere(ray, position, instance.radius, null)) {
              result = i;
              distance = dist2;
          }
      }
      return result;
  }
  
  
  public void update(){
    if (Gdx.input.isKeyJustPressed(Keys.RIGHT) && selected >= 0) {
      relativeRotation(Vector3.Y, 90f);
//      tmpQuat.mul(new Quaternion(Vector3.Y, 90));
//      instances.get(selected).transform.set(tmpQuat);
//      ctx.networkingManager.send(new TXCombatPlayTurn("none", tmpQuat, tmpVec3));
    }
    else if (Gdx.input.isKeyJustPressed(Keys.LEFT) && selected >= 0) {
      relativeRotation(Vector3.Y, 90f);
//      tmpQuat.mul(new Quaternion(Vector3.Y, -90));
//      instances.get(selected).transform.set(tmpQuat);
//      ctx.networkingManager.send(new TXCombatPlayTurn("none", tmpQuat, tmpVec3));
    }
    else if (Gdx.input.isKeyJustPressed(Keys.UP) && selected >= 0) {
      relativeRotation(Vector3.X, -90f);
//      tmpQuat.mul(new Quaternion(Vector3.X, -90));
//      instances.get(selected).transform.set(tmpQuat);
//      ctx.networkingManager.send(new TXCombatPlayTurn("none", tmpQuat, tmpVec3));
    }
    else if (Gdx.input.isKeyJustPressed(Keys.DOWN) && selected >= 0) {
      relativeRotation(Vector3.X, 90f);
//      tmpQuat.mul(new Quaternion(Vector3.X, 90));
//      instances.get(selected).transform.set(tmpQuat);
//      ctx.networkingManager.send(new TXCombatPlayTurn("none", tmpQuat, tmpVec3));
    }
    
    if (Gdx.input.isKeyPressed(Keys.W) && selected >= 0) {
      relativeTranlation(positiveZ);
    }
    else if(Gdx.input.isKeyPressed(Keys.S) && selected >= 0) {
      relativeTranlation(negativeZ);
    }
    
    if (Gdx.input.isKeyPressed(Keys.A) && selected >= 0) {
      relativeTranlation(positiveX);
    } 
    else if (Gdx.input.isKeyPressed(Keys.D) && selected >= 0) {
      relativeTranlation(negativeX);
    }
    
  }

  public void relativeRotation(Vector3 direction, float angle){
    instances.get(selected).transform.getRotation(tmpQuat).nor();
    instances.get(selected).transform.getTranslation(position);
    instances.get(selected).transform.idt();
    instances.get(selected).transform.rotate(direction, angle);
    instances.get(selected).transform.rotate(tmpQuat);
    instances.get(selected).transform.trn(position);
  }
  
  public void relativeTranlation(Vector3 direction){
    instances.get(selected).transform.getRotation(tmpQuat).nor();
    instances.get(selected).transform.getTranslation(position);
    instances.get(selected).transform.idt();
    instances.get(selected).transform.translate(direction);
    instances.get(selected).transform.rotate(tmpQuat);
    instances.get(selected).transform.trn(position);
  }
  
}
