package fr.pierreqr.communitrix.networking.cmd.beans;

import com.badlogic.gdx.math.Vector3;

public class VectorBean {
  public    int   x, y, z;
  public VectorBean () {}
  public VectorBean (final int newX, final int newY, final int newZ) {
    set (newX, newY, newZ);
  }
  public VectorBean (final com.badlogic.gdx.math.Vector3 v) {
    set (v);
  }
  
  public int volume () {
    return x * y * z;
  }
  
  public VectorBean set (final int newX, final int newY, final int newZ) {
    x  = newX;
    y  = newY;
    z  = newZ;
    return this;
  }
  public VectorBean set (final Vector3 v) {
    x = Math.round(v.x);
    y = Math.round(v.y);
    z = Math.round(v.z);
    return this;
  }
  public VectorBean set (final VectorBean v) {
    x  = v.x;
    y  = v.y;
    z  = v.z;
    return this;
  }
  
  public String toString () {
    return String.format("[%d, %d, %d]", x, y, z);
  }
}
