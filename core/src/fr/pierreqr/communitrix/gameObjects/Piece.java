package fr.pierreqr.communitrix.gameObjects;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.FacetedObject;
import fr.pierreqr.communitrix.networking.shared.SHPiece;

public class Piece extends FacetedObject {
  protected final static  MeshBuilder[]       builders  = new MeshBuilder[6];

  public Piece () {
    super     ();
    if (builders[0]==null) {
      for (int i=0; i<6; i++)
        builders[i]   = new MeshBuilder();
    }
  }
  
  protected void begin (final SHPiece piece) {
    // Prepare mesh builders.
    for (final MeshBuilder builder : builders) {
      builder.begin (Usage.Position | Usage.Normal | Usage.TextureCoordinates, GL20.GL_TRIANGLES);
    }
  }
  protected MeshPartBuilder builderFor (final int x, final int y, final int z, final int index, final int direction) {
    return builders[direction];
  }
  protected Model end () {
    final Communitrix ctx = Communitrix.getInstance();
    ctx.modelBuilder.begin  ();
    for (int i=0; i<builders.length; i++) {
      final MeshBuilder builder = builders[i];
      ctx.modelBuilder.part(
          String.format("node%d", i),
          builder.end(),
          GL20.GL_TRIANGLES,
          Communitrix.faceMaterials[i]);
    }
    return ctx.modelBuilder.end();
  }
}
