package fr.pierreqr.communitrix.networking.shared;

public class SHCell extends SHVector {
  public    int   x, y, z, value;
  public SHCell () {
    super       ();
  }
  public SHCell (final int x, final int y, final int z, final int value) {
    super       (x, y, z);
    this.value  = value;
  }
}
