package fr.pierreqr.communitrix.gameObjects;

import java.util.Random;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.utils.Array;

import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.networking.Vector;

public class FuelCell extends GameObject {
  // Internal components.
  private         int[][][]       contents;
  private         int             width, height, depth;
  private         Model           model                   = null;
  private         int             numIndices;
  private         boolean         noContent               = true;
  // "Empty" model to start from.
  private static  Model           dummyModel;
  private static  Array<Material> partMaterials;
  // Random generator.
  private static  Random          rand                    = new Random();
  
  public FuelCell (final int w, final int h, final int d, final int ni, final boolean randomize) {
    super           (initDummyModel());
    numIndices      = ni;
    contents        = new int[width = w][height = h][depth = d];
    noContent       = !randomize;
    if (randomize)
      randomize       ();
    else
      updateMesh      ();
  }
  
  private static Model initDummyModel () {
    if ( dummyModel==null ) {
      Communitrix ctx = Communitrix.getInstance();
      // Prepare our dummy model.
      dummyModel      = ctx.modelBuilder.createRect(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ctx.defaultMaterial, Usage.Position | Usage.Normal);
      // Prepare our many materials.
      BlendingAttribute   blend   = new BlendingAttribute();
      partMaterials   = new Array<Material>();
      for (int i=0; i<10; ++i)
        partMaterials.add (
            new Material(
                ColorAttribute.createDiffuse(
                    0.6f + 0.01f*rand.nextInt(40),
                    0.6f + 0.01f*rand.nextInt(40),
                    0.6f + 0.01f*rand.nextInt(40),
                    0.8f)));
      for (final Material mat : partMaterials)
        mat.set(blend);
    }
    return dummyModel;
  }
  
  public void setContents (final Vector[] points) {
    noContent   = points.length==0;
    for (int x=0; x<width; ++x)
      for (int y=0; y<height; ++y)
        for (int z=0; z<depth; ++z)
          contents[x][y][z]     = 0;
    int xOff  = 0;
    int yOff  = 0;
    int zOff  = 0;
    for (final Vector p : points) {
      xOff  = Math.min(p.x, xOff);
      yOff  = Math.min(p.y, yOff);
      zOff  = Math.min(p.z, zOff);
    }
    for (final Vector p : points)
      contents[p.x-xOff][p.y-yOff][p.z-zOff] = 1;
    updateMesh();
  }
  public void randomize () {
    // Remove any existing part from the model instance.
    for (int x=0; x<width; ++x)
      for (int y=0; y<height; ++y)
        for (int z=0; z<depth; ++z)
          contents[x][y][z]     = rand.nextInt(100)>=65 ? rand.nextInt(numIndices)+1 : 0;
    // Update the mesh.
    updateMesh  ();
  }

  private void clearMesh () {
    // Remove superfluous nodes.
    if (nodes.size>1)
      nodes.removeRange (1, nodes.size-1);
    // Remove all children of first node.
    final Node  node    = nodes.get(0);
    while (node.hasChildren())
      node.removeChild  (node.getChild(0));
    // Get rid of the model.
    if (model!=null) {
      model.dispose     ();
      model             = null;
    }
  }
  public void updateMesh () {
    // Cache actual game instance.
    Communitrix     ctx     = Communitrix.getInstance();
    // Prepare variables.
    final float     radius  = Communitrix.CellComponentRadius;
    clearMesh               ();
    
    if (!noContent) {
      // Start a new part.
      ctx.modelBuilder.begin  ();
  
      // We will create as many "parts" as there are different materials (numIndices).
      for (int index=1; index<numIndices+1; ++index) {
        // Create a mesh part builder.
        MeshPartBuilder part  = ctx.modelBuilder.part(
            String.format("part%d", index),
            GL20.GL_TRIANGLES,
            Usage.Position | Usage.Normal,
            partMaterials.get(index));
        
        // Start building faces.
        for (int x=0; x<width; ++x) {
          for (int y=0; y<height; ++y) {
            for (int z=0; z<depth; ++z) {
              // Retrieve content hint at current position.
              final int       i = contents[x][y][z];
              // Current content is empty, or current content isn't belonging to the part being built.
              if (i==0 || i!=index) continue;
              
              // Nothing on the left.
              if (x==0 || contents[x-1][y][z]==0) {
                part.rect(  x-radius, y-radius, z+radius,
                            x-radius, y+radius, z+radius,
                            x-radius, y+radius, z-radius,
                            x-radius, y-radius, z-radius,
                             -1,   0,   0);
              }
              // Nothing on the right.
              if (x==width-1 || contents[x+1][y][z]==0) {
                part.rect(  x+radius, y-radius, z-radius,
                            x+radius, y+radius, z-radius,
                            x+radius, y+radius, z+radius,
                            x+radius, y-radius, z+radius,
                              1,  0,   0);
              }
              // Nothing on the top.
              if (y==0 || contents[x][y-1][z]==0 ) {
                part.rect(  x+radius, y-radius, z+radius,
                            x-radius, y-radius, z+radius,
                            x-radius, y-radius, z-radius,
                            x+radius, y-radius, z-radius,
                              0,   0,  -1);
              }
              // Nothing on the bottom.
              if (y==height-1 || contents[x][y+1][z]==0) {
                part.rect(  x+radius, y+radius, z-radius,
                            x-radius, y+radius, z-radius,
                            x-radius, y+radius, z+radius,
                            x+radius, y+radius, z+radius,
                              0,   0,   1);
              }
              // Nothing in front.
              if (z==0 || contents[x][y][z-1]==0) {
                part.rect(  x+radius, y-radius, z-radius,
                            x-radius, y-radius, z-radius,
                            x-radius, y+radius, z-radius,
                            x+radius, y+radius, z-radius,
                              0,   1,   0);
              }
              // Nothing behind it.
              if (z==depth-1 || contents[x][y][z+1]==0) {
                part.rect(  x+radius, y+radius, z+radius,
                            x-radius, y+radius, z+radius,
                            x-radius, y-radius, z+radius,
                            x+radius, y-radius, z+radius,
                              0,  -1,   0);
              }
            }
          }
        }
      }
      // Assign new model to variable, and set nodes.
      model                   = ctx.modelBuilder.end();
      nodes.get(0).addChild   (model.nodes.get(0));
    }
    // Recompute object bounds, so it can correctly get picked up by culling math.
    recomputeBounds();
  }
  
  public void dispose () {
    clearMesh     ();
    contents      = null;
  }
}
