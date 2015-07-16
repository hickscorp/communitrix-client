package fr.pierreqr.communitrix.networking.cmd.tx;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import fr.pierreqr.communitrix.networking.cmd.beans.QuaternionBean;
import fr.pierreqr.communitrix.networking.cmd.beans.VectorBean;

public class TXCombatPlayTurn extends TXBase {
  public    int           pieceIndex;
  public    VectorBean      translation;
  public    QuaternionBean  rotation;

  public TXCombatPlayTurn (final int pieceIndex, final Vector3 translation, final Quaternion rotation) {
    super             (Type.CombatPlayTurn);
    this.pieceIndex   = pieceIndex;
    this.translation  = new VectorBean(translation);
    this.rotation     = new QuaternionBean(rotation);
    this.serial       = "PlayTurn";
  }
}
