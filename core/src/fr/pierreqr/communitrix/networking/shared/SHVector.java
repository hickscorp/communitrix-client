package fr.pierreqr.communitrix.networking.shared;

public class SHVector {
  public    int   x, y, z;
  public SHVector () {}
  public SHVector (final int x, final int y, final int z) {
    this.x  = x; this.y  = y; this.z  = z;
  }
  public SHVector (final com.badlogic.gdx.math.Vector3 v) {
    this.x  = (int)v.x; this.y  = (int)v.y; this.z  = (int)v.z;
  }
  
  public int volume () {
    return x * y * z;
  }
}
