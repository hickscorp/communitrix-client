package fr.pierreqr.communitrix.gameObjects;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.networking.shared.SHCell;
import fr.pierreqr.communitrix.networking.shared.SHPiece;

public abstract class FacetedObject extends GameObject {
  protected abstract  void            begin           (final SHPiece piece);
  protected abstract  MeshPartBuilder builderFor      (final int x, final int y, final int z, final int index, final int direction);
  protected abstract  Model           end             ();

  protected                           Model           model;

  public FacetedObject  () {
    super               (Communitrix.getInstance().dummyModel);
  }

  public void setFromSharedPiece (final SHPiece piece) {
    clear             ();
    if (piece==null)  return;

    // Cache stuff.
    final float   r   = Communitrix.CellComponentRadius;

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

    begin             (piece);

    // Start building faces.
    for (int iX=0; iX<xSize; ++iX) {
      final int x = iX - xSize/2;
      for (int iY=0; iY<ySize; ++iY) {
        final int y = iY - ySize/2;
        for (int iZ=0; iZ<zSize; ++iZ) {
          final int z = iZ - zSize/2;
          // Retrieve content hint at current position.
          final int index = contents[iX][iY][iZ];
          // Current content is empty, or current content isn't belonging to the part being built.
          if (index==0) continue;
          // Nothing on the left.
          if (iX==0 || contents[iX-1][iY][iZ]==0) {
            builderFor(iX, iY, iZ, index, Communitrix.Left)
              .rect(  x-r, y-r, z+r,
                      x-r, y+r, z+r,
                      x-r, y+r, z-r,
                      x-r, y-r, z-r,
                      1, 0, 0);
          }
          // Nothing on the right.
          if (iX==xSize-1 || contents[iX+1][iY][iZ]==0) {
            builderFor(iX, iY, iZ, index, Communitrix.Right)
              .rect(  x+r, y-r, z-r,
                      x+r, y+r, z-r,
                      x+r, y+r, z+r,
                      x+r, y-r, z+r,
                      -1, 0, 0);
          }
          // Nothing on the top.
          if (iY==0 || contents[iX][iY-1][iZ]==0 ) {
            builderFor(iX, iY, iZ, index, Communitrix.Bottom)
              .rect(  x+r, y-r, z+r,
                      x-r, y-r, z+r,
                      x-r, y-r, z-r,
                      x+r, y-r, z-r,
                      0, 1, 0);
          }
          // Nothing on the bottom.
          if (iY==ySize-1 || contents[iX][iY+1][iZ]==0) {
            builderFor(iX, iY, iZ, index, Communitrix.Top)
              .rect(  x+r, y+r, z-r,
                      x-r, y+r, z-r,
                      x-r, y+r, z+r,
                      x+r, y+r, z+r,
                      0, -1, 0);
          }
          // Nothing in front.
          if (iZ==0 || contents[iX][iY][iZ-1]==0) {
            builderFor(iX, iY, iZ, index, Communitrix.Backward)
              .rect(  x+r, y-r, z-r,
                      x-r, y-r, z-r,
                      x-r, y+r, z-r,
                      x+r, y+r, z-r,
                      0, 0, 1);
          }
          // Nothing behind it.
          if (iZ==zSize-1 || contents[iX][iY][iZ+1]==0) {
            builderFor(iX, iY, iZ, index, Communitrix.Forward)
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

  private void clear () {
    // Remove superfluous nodes.
    nodes.clear     ();
    // Get rid of the model.
    if (model!=null) {
      model.dispose ();
      model         = null;
    }
  }
}
