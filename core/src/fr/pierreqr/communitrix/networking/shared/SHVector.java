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
  
  public SHVector set (final SHVector v) {
    this.x  = v.x;
    this.y  = v.y;
    this.z  = v.z;
    return this;
  }
  public SHVector set (final com.badlogic.gdx.math.Vector3 v) {
    this.x  = Math.round(v.x);
    this.y  = Math.round(v.y);
    this.z  = Math.round(v.z);
    return this;
  }
  public SHVector add (final com.badlogic.gdx.math.Vector3 v) {
    this.x  += v.x;
    this.y  += v.y;
    this.z  += v.z;
    return this;
  }
  
  public String toString () {
    return String.format("(%d, %d, %d)", x, y, z);
  }
}
