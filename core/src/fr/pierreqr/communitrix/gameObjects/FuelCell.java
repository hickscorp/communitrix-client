package fr.pierreqr.communitrix.gameObjects;

import java.util.Random;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import fr.pierreqr.communitrix.Communitrix;

public class FuelCell extends GameObject {
  // Internal components.
  public          int[][][]       contents;
  public          int             width, height, depth;
  public          Model           model;
  // "Empty" model to start from.
  public static   Model           dummyModel;
  // Random generator.
  private static  Random          rand                    = new Random();
  
  public FuelCell (final int w, final int h, final int d, final boolean randomize) {
    super           (initDummyModel());
    contents        = new int[width = w][height = h][depth = d];
    if (randomize)
      randomize       ();
    else {
      clear           ();
      updateMesh      ();
    }
  }
  private static Model initDummyModel () {
    if ( dummyModel==null ) {
      Communitrix ctx = Communitrix.getInstance();
      dummyModel      = ctx.modelBuilder.createRect(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ctx.defaultMaterial, Usage.Position | Usage.Normal);
    }
    return dummyModel;
  }
  
  public void randomize () {
    // Remove any existing part from the model instance.
    clear       ();
    for (int x=0; x<width; ++x)
      for (int y=0; y<height; ++y)
        for (int z=0; z<depth; ++z)
          contents[x][y][z]     = rand.nextInt(100)>60 ? 1 : 0;
    // Update the mesh.
    updateMesh  ();
  }
  public void clear () {
    // Remove superfluous nodes.
    if (nodes.size>1)
      nodes.removeRange(1, nodes.size-1);
    // Remove all children of first node.
    final Node  node    = nodes.get(0);
    while (node.hasChildren())
      node.removeChild(node.getChild(0));
  }
  public void updateMesh () {
    final Node          node    = nodes.get(0);
    final float         radius  = Communitrix.CELL_COMPONENT_RADIUS;
    
    Communitrix.getInstance().modelBuilder.begin();
    MeshPartBuilder part  = Communitrix.getInstance().modelBuilder.part(
        "mainPart",
        GL20.GL_TRIANGLES,
        Usage.Position | Usage.Normal,
        Communitrix.getInstance().defaultMaterial);
    
    for (int x=0; x<width; ++x) {
      for (int y=0; y<height; ++y) {
        for (int z=0; z<depth; ++z) {
          // Current slot is not empty!
          if (contents[x][y][z]!=0) {
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
    
    final Model   newModel  = Communitrix.getInstance().modelBuilder.end();
    if (model!=null)        model.dispose();
    node.addChild           ( ( model = newModel ).nodes.get(0));
  }
  
  public void dispose () {
    if (model!=null)  model.dispose();
  }
}
