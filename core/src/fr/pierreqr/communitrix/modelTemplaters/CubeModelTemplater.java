package fr.pierreqr.communitrix.modelTemplaters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class CubeModelTemplater implements ModelTemplater {
  public Model build (final ModelBuilder b) {
    b.begin();
    b.part(
        "face1",
        GL20.GL_TRIANGLES,
        Usage.Position | Usage.Normal,
        new Material(ColorAttribute.createDiffuse(Color.RED)))
      .rect(  -1, -1, -1,
              -1,  1, -1,
               1,  1, -1,
               1, -1, -1,
               0,  0, -1);
    b.part(
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
    b.part(
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
    b.part(
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
    b.part(
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
    b.part(
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
      
    return b.end();
  }
}
