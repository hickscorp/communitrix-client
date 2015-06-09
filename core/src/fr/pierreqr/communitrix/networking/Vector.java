package fr.pierreqr.communitrix.networking;

public class Vector {
  public    int   x, y, z;
  public Vector () {}
  public Vector (final int x, final int y, final int z) {
    this.x  = x;
    this.y  = y;
    this.z  = z;
  }
  public Vector (final com.badlogic.gdx.math.Vector3 v) {
    this.x  = (int)v.x;
    this.y  = (int)v.y;
    this.z  = (int)v.z;
  }
  public String toString () {
    return "(" + x + ", " + y + ", " + z + ")";
  }
}
