package fr.pierreqr.communitrix.networking.cmd.beans;

public class CellBean extends VectorBean {
  public    int   x, y, z, value;
  public CellBean () {
    super       ();
  }
  public CellBean (final int x, final int y, final int z, final int newValue) {
    super   (x, y, z);
    value   = newValue;
  }
}
