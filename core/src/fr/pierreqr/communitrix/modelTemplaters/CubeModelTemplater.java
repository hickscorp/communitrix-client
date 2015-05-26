package fr.pierreqr.communitrix.modelTemplaters;

import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import fr.pierreqr.communitrix.Communitrix;

public class CubeModelTemplater implements ModelTemplater {
  public Model build (final ModelBuilder b) {
    return b.createBox(2, 2, 2, Communitrix.getInstance().defaultMaterial, Usage.Position | Usage.Normal);
    //b.begin();
    //b.part(
    //    "face1",
    //    GL20.GL_TRIANGLES,
    //    Usage.Position | Usage.Normal,
    //    mtlDefault)
    //  .rect(  -1, -1, -1,
    //          -1,  1, -1,
    //           1,  1, -1,
    //           1, -1, -1,
    //           0,  0, -1);
    //b.part(
    //    "face2",
    //    GL20.GL_TRIANGLES,
    //    Usage.Position | Usage.Normal,
    //    mtlDefault)
    //  .rect(
    //          -1,  1,  1, 
    //          -1, -1,  1,
    //           1, -1,  1,
    //           1,  1,  1,
    //           0,  0,  1);
    //b.part(
    //    "face3",
    //    GL20.GL_TRIANGLES,
    //    Usage.Position | Usage.Normal,
    //    mtlDefault)
    //  .rect(
    //          -1, -1,  1,
    //          -1, -1, -1,
    //           1, -1, -1,
    //           1, -1,  1,
    //           0, -1,  0);
    //b.part(
    //    "face4",
    //    GL20.GL_TRIANGLES,
    //    Usage.Position | Usage.Normal,
    //    mtlDefault)
    //  .rect(
    //          -1,  1, -1,
    //          -1,  1,  1,
    //           1,  1,  1,
    //           1,  1, -1,
    //           0,  1,  0);
    //b.part(
    //    "face5",
    //    GL20.GL_TRIANGLES,
    //    Usage.Position | Usage.Normal,
    //    mtlDefault)
    //  .rect(
    //          -1, -1,  1,
    //          -1,  1,  1,
    //          -1,  1, -1,
    //          -1, -1, -1,
    //          -1,  0,  0);
    //b.part(
    //    "face6",
    //    GL20.GL_TRIANGLES,
    //    Usage.Position | Usage.Normal,
    //    mtlDefault)
    //  .rect(
    //           1, -1, -1,
    //           1,  1, -1,
    //           1,  1,  1,
    //           1, -1,  1,
    //           1,  0,  0);
    //  
    //return b.end();
  }
  public void dispose () {
  }
}
