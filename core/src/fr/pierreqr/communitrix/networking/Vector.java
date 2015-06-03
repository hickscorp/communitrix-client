package fr.pierreqr.communitrix.networking;

public class Vector {
  public    int   x, y, z;
  public Vector () {}
  public Vector (final com.badlogic.gdx.math.Vector3 v) {
    x   = (int)v.x;
    y   = (int)v.y;
    z   = (int)v.z;
  } 
}
