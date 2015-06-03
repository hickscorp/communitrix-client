package fr.pierreqr.communitrix.networking;

public class Quaternion {
  public    float    x, y, z, w;
  public Quaternion () {}
  public Quaternion (final com.badlogic.gdx.math.Quaternion q) {
    x   = q.x;
    y   = q.y;
    z   = q.z;
    w   = q.w;
  }
}
