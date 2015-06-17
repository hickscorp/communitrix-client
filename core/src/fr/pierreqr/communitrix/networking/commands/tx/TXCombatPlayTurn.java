package fr.pierreqr.communitrix.networking.commands.tx;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import fr.pierreqr.communitrix.networking.shared.SHQuaternion;
import fr.pierreqr.communitrix.networking.shared.SHVector;

public class TXCombatPlayTurn extends TXBase {
  public    int           pieceIndex;
  public    SHQuaternion  rotation;
  public    SHVector      translation;

  public TXCombatPlayTurn (final int pieceIndex, final Quaternion rotation, final Vector3 translation) {
    super         (Tx.CombatPlayTurn);
    this.pieceIndex   = pieceIndex;
    this.rotation     = new SHQuaternion(rotation);
    this.translation  = new SHVector(translation);
  }
}
