package fr.pierreqr.communitrix.gameObjects;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.Constants.CubeFace;
import fr.pierreqr.communitrix.networking.shared.SHCell;
import fr.pierreqr.communitrix.networking.shared.SHPiece;

public abstract class FacetedObject extends GameObject {
  protected abstract  void            begin           (final SHPiece piece);
  protected abstract  MeshPartBuilder builderFor      (final int x, final int y, final int z, final int index, final int direction);
  protected abstract  Model           end             ();
  
  private             Model           model;
  public              SHPiece         sharedPiece     = new SHPiece();

  public FacetedObject () {
    super (Communitrix.getInstance().dummyModel);
  }
  
  private static final float r = 0.47f;
  public void setFromSharedPiece (final SHPiece newPiece) {
    clear               ();
    sharedPiece         = newPiece;
    if (newPiece==null) return;

    // Cache stuff.
    final SHPiece p   = sharedPiece;
    // Make the temporary contents array.
    final int[][][]   contents  = new int[p.size.x][p.size.y][p.size.z];
    // Finally build the content array.
    for (final SHCell cell : newPiece.content)
      contents[cell.x-p.min.x][cell.y-p.min.y][cell.z-p.min.z] = cell.value;
    // Start building faces.
    begin             (newPiece);
    for (int iX=0; iX<p.size.x; ++iX) {
      final int x = iX + p.min.x;
      for (int iY=0; iY<p.size.y; ++iY) {
        final int y = iY + p.min.y;
        for (int iZ=0; iZ<p.size.z; ++iZ) {
          final int z = iZ + p.min.z;
          // Retrieve content hint at current position.
          final int index = contents[iX][iY][iZ];
          // Current content is empty, or current content isn't belonging to the part being built.
          if (index==0) continue;
          // Nothing on the left.
          if (iX==0 || contents[iX-1][iY][iZ]==0) {
            builderFor(iX, iY, iZ, index, CubeFace.Left.ordinal())
              .rect(  x-r, y-r, z+r,
                      x-r, y+r, z+r,
                      x-r, y+r, z-r,
                      x-r, y-r, z-r,
                      1, 0, 0);
          }
          // Nothing on the right.
          if (iX==p.size.x-1 || contents[iX+1][iY][iZ]==0) {
            builderFor(iX, iY, iZ, index, CubeFace.Right.ordinal())
              .rect(  x+r, y-r, z-r,
                      x+r, y+r, z-r,
                      x+r, y+r, z+r,
                      x+r, y-r, z+r,
                      -1, 0, 0);
          }
          // Nothing on the top.
          if (iY==0 || contents[iX][iY-1][iZ]==0 ) {
            builderFor(iX, iY, iZ, index, CubeFace.Bottom.ordinal())
              .rect(  x+r, y-r, z+r,
                      x-r, y-r, z+r,
                      x-r, y-r, z-r,
                      x+r, y-r, z-r,
                      0, 1, 0);
          }
          // Nothing on the bottom.
          if (iY==p.size.y-1 || contents[iX][iY+1][iZ]==0) {
            builderFor(iX, iY, iZ, index, CubeFace.Top.ordinal())
              .rect(  x+r, y+r, z-r,
                      x-r, y+r, z-r,
                      x-r, y+r, z+r,
                      x+r, y+r, z+r,
                      0, -1, 0);
          }
          // Nothing in front.
          if (iZ==0 || contents[iX][iY][iZ-1]==0) {
            builderFor(iX, iY, iZ, index, CubeFace.Backward.ordinal())
              .rect(  x+r, y-r, z-r,
                      x-r, y-r, z-r,
                      x-r, y+r, z-r,
                      x+r, y+r, z-r,
                      0, 0, 1);
          }
          // Nothing behind it.
          if (iZ==p.size.z-1 || contents[iX][iY][iZ+1]==0) {
            builderFor(iX, iY, iZ, index, CubeFace.Forward.ordinal())
              .rect(  x+r, y+r, z+r,
                      x-r, y+r, z+r,
                      x-r, y-r, z+r,
                      x+r, y-r, z+r,
                      0, 0, -1);
          }
        }
      }
    }
    
    // Start building our final model, which will be the sum of all meshes.
    nodes.addAll            (( model = end() ).nodes);
    recomputeBounds         ();
  }
  
  private static final Vector3  tmpVec    = new Vector3();
  private static final Matrix4  tmpMat    = new Matrix4();
  public boolean collidesWith (final FacetedObject other) {
    if (sharedPiece==null || other.sharedPiece==null)
      return false;
    tmpMat.set          (targetPosition, targetRotation);
    for (final SHCell v : sharedPiece.content) {
      tmpVec
        .set            (v.x, v.y, v.z)
        .mul            (tmpMat);
      for (final SHCell c : other.sharedPiece.content)
        if (c.x==Math.round(tmpVec.x) && c.y==Math.round(tmpVec.y) && c.z==Math.round(tmpVec.z))
          return true;
    }
    return false;
  }
  public void switchToRegularMaterials () {
    for (final Node node : nodes) {
      int i = 0;
      for (final NodePart part : node.parts) {
        part.material   = Communitrix.faceMaterials[i];
        i++;
      }
    }
  }
  public void switchToCollisionMaterials () {
    for (final Node node : nodes) {
      int i = 0;
      for (final NodePart part : node.parts) {
        part.material   = Communitrix.faceMaterials[6+i];
        i++;
      }
    }
  }

  public void clear () {
    sharedPiece     = null;
    // Remove superfluous nodes.
    nodes.clear     ();
    // Get rid of the model.
    if (model!=null) {
      model.dispose ();
      model         = null;
    }
  }
}
