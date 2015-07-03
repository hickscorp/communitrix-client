package fr.pierreqr.communitrix.networking.shared;

import com.badlogic.gdx.math.Vector3;

public class SHVector {
  public    int   x, y, z;
  public SHVector () {}
  public SHVector (final int newX, final int newY, final int newZ) {
    set (newX, newY, newZ);
  }
  public SHVector (final com.badlogic.gdx.math.Vector3 v) {
    set (v);
  }
  
  public int volume () {
    return x * y * z;
  }
  
  public SHVector set (final int newX, final int newY, final int newZ) {
    x  = newX;
    y  = newY;
    z  = newZ;
    return this;
  }
  public SHVector set (final Vector3 v) {
    x = Math.round(v.x);
    y = Math.round(v.y);
    z = Math.round(v.z);
    return this;
  }
  public SHVector set (final SHVector v) {
    x  = v.x;
    y  = v.y;
    z  = v.z;
    return this;
  }
  
  public String toString () {
    return String.format("[%d, %d, %d]", x, y, z);
  }
}
