package fr.pierreqr.communitrix.gameObjects;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class FuelCell extends GameObject {
  // Internal components.
  public          int[][][]       contents;
  public          int             width, height, depth;
  // Random generator.
  private static  Random          rand                    = new Random();
  
  public FuelCell (final Model model, final int w, final int h, final int d, final boolean randomize) {
    super           (model);
    contents        = new int[width = w][height = h][depth = d];
    clear           ();
    if (randomize)  randomize();
    updateMesh      ();
  }
  
  public void randomize () {
    for (int x=0; x<width; ++x)
      for (int y=0; y<height; ++y)
        for (int z=0; z<depth; ++z)
          contents[x][y][z]     = rand.nextInt(100)>95 ? 1 : 0;
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
    ModelBuilder  b       = new ModelBuilder();
    Material      mtl     = new Material(ColorAttribute.createDiffuse(Color.WHITE));
    final Node    node    = nodes.get(0);
    final float   u       = 0.5f;
    
    b.begin();
    MeshPartBuilder part  = b.part(
        "mainPart",
        GL20.GL_TRIANGLES,
        Usage.Position | Usage.Normal,
        mtl);
    
    for (int x=0; x<width; ++x) {
      for (int y=0; y<height; ++y) {
        for (int z=0; z<depth; ++z) {
          // Current slot is not empty!
          if (contents[x][y][z]!=0) {
            // Nothing on the left.
            if (x==0 || contents[x-1][y][z]==0) {
              part.rect(  x-u, y-u, z+u,
                          x-u, y+u, z+u,
                          x-u, y+u, z-u,
                          x-u, y-u, z-u,
                           -1,   0,   0);
            }
            // Nothing on the right.
            if (x==width-1 || contents[x+1][y][z]==0) {
              part.rect(  x+u, y-u, z-u,
                          x+u, y+u, z-u,
                          x+u, y+u, z+u,
                          x+u, y-u, z+u,
                            1,  0,   0);
            }
            // Nothing on the top.
            if (y==0 || contents[x][y-1][z]==0 ) {
              part.rect(  x+u, y-u, z+u,
                          x-u, y-u, z+u,
                          x-u, y-u, z-u,
                          x+u, y-u, z-u,
                            0,   0,  -1);
            }
            // Nothing on the bottom.
            if (y==height-1 || contents[x][y+1][z]==0) {
              part.rect(  x+u, y+u, z-u,
                          x-u, y+u, z-u,
                          x-u, y+u, z+u,
                          x+u, y+u, z+u,
                            0,   0,   1);
            }
            // Nothing in front.
            if (z==0 || contents[x][y][z-1]==0) {
              part.rect(  x+u, y-u, z-u,
                          x-u, y-u, z-u,
                          x-u, y+u, z-u,
                          x+u, y+u, z-u,
                            0,   1,   0);
            }
            // Nothing behind it.
            if (z==depth-1 || contents[x][y][z+1]==0) {
              part.rect(  x+u, y+u, z+u,
                          x-u, y+u, z+u,
                          x-u, y-u, z+u,
                          x+u, y-u, z+u,
                            0,  -1,   0);
            }
          }
        }
      }
    }
    
    
    node.addChild(b.end().nodes.get(0));
  }
}
