package fr.pierreqr.communitrix.networking;

import fr.pierreqr.communitrix.networking.cmd.rx.RXBase;

public interface NetworkDelegate {
  boolean   onServerMessage       (final RXBase cmd);
}
