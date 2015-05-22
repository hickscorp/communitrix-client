package fr.pierreqr.communitrix;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class SimpleCube extends GameObject {
  // Members.
  public                int           selectedFace  = 0;
  // Temporaries.
  private final static  Vector3       tmpXAxis      = new Vector3();
  private final static  Vector3       tmpYAxis      = new Vector3();
  private final static  Vector3       tmpZAxis      = new Vector3();
  private final static  Vector3       tmpPosition   = new Vector3();
  private final static  Quaternion    tmpRotation   = new Quaternion();
  // The model based on which to build our instances.
  private static        Model         mdlCube;
  
  public SimpleCube () {
    super(init());
  }
  
  public void rotate (final Camera cam, final Vector3 axis, final float angle) {
    // Get rotation axis based on camera.
    tmpZAxis.set(cam.direction).scl(-1);
    tmpXAxis.set(tmpZAxis).crs(cam.up);
    tmpYAxis.set(tmpXAxis).crs(cam.direction);
    // Store original position / rotation.
    transform.getTranslation(tmpPosition);
    transform.getRotation(tmpRotation).nor();
    transform.idt();
    // Rotate model.
    if (axis==Vector3.X)
      transform.rotate(tmpXAxis, angle);
    else if (axis==Vector3.Y)
      transform.rotate(tmpYAxis, angle);
    transform.rotate(tmpRotation);
    transform.trn(tmpPosition);    
  }
  
  private static Model init () {
    if (mdlCube==null) {
      ModelBuilder mdlBdrCube = new ModelBuilder();
      mdlBdrCube.begin();
      mdlBdrCube.part(
          "face1",
          GL20.GL_TRIANGLES,
          Usage.Position | Usage.Normal,
          new Material(ColorAttribute.createDiffuse(Color.RED)))
        .rect(  -1, -1, -1,
                -1,  1, -1,
                 1,  1, -1,
                 1, -1, -1,
                 0,  0, -1);
      mdlBdrCube.part(
          "face2",
          GL20.GL_TRIANGLES,
          Usage.Position | Usage.Normal,
          new Material(ColorAttribute.createDiffuse(Color.GREEN)))
        .rect(
                -1,  1,  1, 
                -1, -1,  1,
                 1, -1,  1,
                 1,  1,  1,
                 0,  0,  1);
      mdlBdrCube.part(
          "face3",
          GL20.GL_TRIANGLES,
          Usage.Position | Usage.Normal,
          new Material(ColorAttribute.createDiffuse(Color.BLUE)))
        .rect(
                -1, -1,  1,
                -1, -1, -1,
                 1, -1, -1,
                 1, -1,  1,
                 0, -1,  0);
      mdlBdrCube.part(
          "face4",
          GL20.GL_TRIANGLES,
          Usage.Position | Usage.Normal,
          new Material(ColorAttribute.createDiffuse(Color.PURPLE)))
        .rect(
                -1,  1, -1,
                -1,  1,  1,
                 1,  1,  1,
                 1,  1, -1,
                 0,  1,  0);
      mdlBdrCube.part(
          "face5",
          GL20.GL_TRIANGLES,
          Usage.Position | Usage.Normal,
          new Material(ColorAttribute.createDiffuse(Color.PINK)))
        .rect(
                -1, -1,  1,
                -1,  1,  1,
                -1,  1, -1,
                -1, -1, -1,
                -1,  0,  0);
      mdlBdrCube.part(
          "face6",
          GL20.GL_TRIANGLES,
          Usage.Position | Usage.Normal,
          new Material(ColorAttribute.createDiffuse(Color.ORANGE)))
        .rect(
                 1, -1, -1,
                 1,  1, -1,
                 1,  1,  1,
                 1, -1,  1,
                 1,  0,  0);
      mdlCube = mdlBdrCube.end();
    }
    return mdlCube;
  }

}
