package fr.pierreqr.communitrix.screens;

import com.badlogic.gdx.utils.Array;
import fr.pierreqr.communitrix.gameObjects.Piece;
import fr.pierreqr.communitrix.networking.cmd.beans.CombatBean;

public class ScreenSharedData {
  // The last loaded list of combats.
  public final  Array<CombatBean>       combats       = new Array<CombatBean>();
  // The current combat we've joined.
  public        CombatBean              combat        = null;
  
  // The piece being played / that was played this turn.
  public        Piece                 playedPiece   = null;
  // The currently selected piece and informations about it.
  public        Piece                 selectedPiece = null;
  public        boolean               isColliding   = false;
  
}
