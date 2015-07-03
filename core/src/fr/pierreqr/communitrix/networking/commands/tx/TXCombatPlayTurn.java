package fr.pierreqr.communitrix.networking.commands.tx;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import fr.pierreqr.communitrix.networking.shared.SHQuaternion;
import fr.pierreqr.communitrix.networking.shared.SHVector;

public class TXCombatPlayTurn extends TXBase {
  public    int           pieceIndex;
  public    SHVector      translation;
  public    SHQuaternion  rotation;

  public TXCombatPlayTurn (final int pieceIndex, final Vector3 translation, final Quaternion rotation) {
    super             (Tx.CombatPlayTurn);
    this.pieceIndex   = pieceIndex;
    this.translation  = new SHVector(translation);
    this.rotation     = new SHQuaternion(rotation);
    this.serial       = "PlayTurn";
  }
}
