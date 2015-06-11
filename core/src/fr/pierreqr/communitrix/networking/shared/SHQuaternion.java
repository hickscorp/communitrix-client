package fr.pierreqr.communitrix.networking.shared;

public class SHQuaternion {
  public    float    x, y, z, w;
  public SHQuaternion () {}
  public SHQuaternion (final int x, final int y, final int z, final int w) {
    this.x = x; this.y = y; this.z = z; this.w = w;
  }
  public SHQuaternion (final com.badlogic.gdx.math.Quaternion q) {
    this.x = q.x; this.y = q.y; this.z = q.z; this.w = q.w;
  }
}
