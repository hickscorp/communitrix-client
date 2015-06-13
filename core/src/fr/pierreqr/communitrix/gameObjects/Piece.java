package fr.pierreqr.communitrix.gameObjects;

import java.util.HashMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.networking.shared.SHCell;
import fr.pierreqr.communitrix.networking.shared.SHPiece;

public class Piece extends GameObject {
  private static final  String                    LogTag      = "Piece";
  private               HashMap<Integer,Material> materials;
  private               Model                     model;
  
  public Piece () {
    this(null);
  }
  public Piece (final SHPiece piece) {
    super               (Communitrix.getInstance().dummyModel);
    materials           = new HashMap<Integer,Material>(0);
    setFromSharedPiece  (piece);
  }
  
  public void setFromSharedPiece (final SHPiece piece) {
    // Reset.
    clear();
    // No piece?
    if (piece==null) return;

    // Cache stuff.
    final Communitrix ctx     = Communitrix.getInstance();
    final float       radius  = Communitrix.CellComponentRadius;
    prepareMaterials          (piece);
    
    // Compute negative offsets.
    int xOff  = Integer.MAX_VALUE;
    int yOff  = Integer.MAX_VALUE;
    int zOff  = Integer.MAX_VALUE;
    for (final SHCell p : piece.content) {
      xOff  = Math.min(p.x, xOff);
      yOff  = Math.min(p.y, yOff);
      zOff  = Math.min(p.z, zOff);
    }
    // Cache size absolute values.
    final int   xSize = Math.abs(piece.size.x);
    final int   ySize = Math.abs(piece.size.y);
    final int   zSize = Math.abs(piece.size.z);

    // Make the temporary contents array.
    final int[][][]   contents  = new int[xSize][ySize][zSize];
    // Finally build the content array.
    for (final SHCell cell : piece.content) {
      contents[cell.x-xOff][cell.y-yOff][cell.z-zOff] = cell.value;
    }

    // Create as many builders as there are indices.
    HashMap<Integer,MeshBuilder> builders = new HashMap<Integer, MeshBuilder>();
    for (Integer index : materials.keySet()) {
      // Create a new builder.
      final MeshBuilder builder = new MeshBuilder();
      builder.begin                 (Usage.Position | Usage.Normal, GL20.GL_TRIANGLES);
      builders.put                  (index, builder);
    }
    // Start building faces.
    for (int x=0; x<xSize; ++x) {
      for (int y=0; y<ySize; ++y) {
        for (int z=0; z<zSize; ++z) {
          // Retrieve content hint at current position.
          final int index = contents[x][y][z];
          // Current content is empty, or current content isn't belonging to the part being built.
          if (index==0) continue;
          // Get the part builder for the matching index.
          final MeshBuilder mesh = builders.get(index);
          // Nothing on the left.
          if (x==0 || contents[x-1][y][z]==0) {
            mesh.rect(  x-radius, y-radius, z+radius,
                        x-radius, y+radius, z+radius,
                        x-radius, y+radius, z-radius,
                        x-radius, y-radius, z-radius,
                        1, 0, 0);
          }
          // Nothing on the right.
          if (x==xSize-1 || contents[x+1][y][z]==0) {
            mesh.rect(  x+radius, y-radius, z-radius,
                        x+radius, y+radius, z-radius,
                        x+radius, y+radius, z+radius,
                        x+radius, y-radius, z+radius,
                        -1, 0, 0);
          }
          // Nothing on the top.
          if (y==0 || contents[x][y-1][z]==0 ) {
            mesh.rect(  x+radius, y-radius, z+radius,
                        x-radius, y-radius, z+radius,
                        x-radius, y-radius, z-radius,
                        x+radius, y-radius, z-radius,
                        0, 1, 0);
          }
          // Nothing on the bottom.
          if (y==ySize-1 || contents[x][y+1][z]==0) {
            mesh.rect(  x+radius, y+radius, z-radius,
                        x-radius, y+radius, z-radius,
                        x-radius, y+radius, z+radius,
                        x+radius, y+radius, z+radius,
                        0, -1, 0);
          }
          // Nothing in front.
          if (z==0 || contents[x][y][z-1]==0) {
            mesh.rect(  x+radius, y-radius, z-radius,
                        x-radius, y-radius, z-radius,
                        x-radius, y+radius, z-radius,
                        x+radius, y+radius, z-radius,
                        0, 0, 1);
          }
          // Nothing behind it.
          if (z==zSize-1 || contents[x][y][z+1]==0) {
            mesh.rect(  x+radius, y+radius, z+radius,
                        x-radius, y+radius, z+radius,
                        x-radius, y-radius, z+radius,
                        x+radius, y-radius, z+radius,
                        0, 0, -1);
          }
        }
      }
    }
    
    // Start building our final model, which will be the sum of all meshes.
    ctx.modelBuilder.begin    ();
    for (int index : builders.keySet()) {
      ctx.modelBuilder.part(
          String.format("piece%d", index),
          builders.get(index).end(),
          GL20.GL_TRIANGLES,
          materials.get(index));
    }
    model                         = ctx.modelBuilder.end();
    Gdx.app.log                   (LogTag, "New piece model has " + model.nodes.size + " node(s).");
    for (int index=0; index<model.nodes.size; ++index) {
      final   Node  newNode       = model.nodes.get(index);
      newNode.calculateTransforms (true);
      nodes.add                   (newNode);
    }
    recomputeBounds();
  }
  
  // This method takes care of creating the required numbers of materials based on the number of indices
  // in the current piece. When called multiple times, it will transfer existing materials to the new ones
  // to prevent color-swapping (Eg A material used for the previous index will be used for the new index).
  private void prepareMaterials (final SHPiece piece) {
    // Cache our game instance.
    final Communitrix ctx = Communitrix.getInstance();
    // Prepare a new array of materials.
    final HashMap<Integer,Material> newMaterials = new HashMap<Integer, Material>();
    // First, count the unique indices.
    final ColorAttribute      spec    = ColorAttribute.createSpecular(0.7f, 0.7f, 0.7f, 1.0f);
    final FloatAttribute      shine   = FloatAttribute.createShininess(0.5f);
    final BlendingAttribute   blend   = new BlendingAttribute(true, 0.7f);
    for (final SHCell cell : piece.content) {
      final int index = cell.value;
      if (!newMaterials.containsKey(index)) {
        final Material mat = materials.getOrDefault(
            index,
            new Material(
              ColorAttribute.createDiffuse(
                  0.4f + 0.1f*ctx.rand.nextInt(5),
                  0.4f + 0.1f*ctx.rand.nextInt(5),
                  0.4f + 0.1f*ctx.rand.nextInt(5),
                  1.0f),
              spec, shine, blend
            )
          );
        newMaterials.put (cell.value, mat);
      }
    }
    materials.clear();
    materials = newMaterials;
  }
  
  private void clear () {
    // Remove superfluous nodes.
    nodes.clear     ();
    // Get rid of the model.
    if (model!=null) {
      model.dispose ();
      model         = null;
    }
  }
  
  public void dispose() {
    clear           ();
  }
}