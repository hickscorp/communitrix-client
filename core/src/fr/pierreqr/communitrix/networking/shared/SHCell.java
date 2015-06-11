package fr.pierreqr.communitrix.networking.shared;

public class SHCell {
  public    int   x, y, z, value;
  public SHCell () {}
  public SHCell (final int x, final int y, final int z, final int value) {
    this.x      = x;
    this.y      = y;
    this.z      = z;
    this.value  = value;
  }
  public String toString () {
    return "(" + x + ", " + y + ", " + z + "=" + value + ")";
  }
}
