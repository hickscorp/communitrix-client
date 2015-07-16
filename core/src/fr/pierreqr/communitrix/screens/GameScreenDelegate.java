package fr.pierreqr.communitrix.screens;

import com.badlogic.gdx.math.Vector3;
import fr.pierreqr.communitrix.gameObjects.GameObject;
import fr.pierreqr.communitrix.gameObjects.Piece;

public interface GameScreenDelegate {
  void        translateWithinView   (final GameObject obj, final Vector3 axis, final boolean checkCollisions);
  void        rotateWithinView      (final GameObject obj, final Vector3 axis, final int angle, final boolean checkCollisions);
  Piece       getClickableAt        (final int screenX, final int screenY);
  boolean     handleSelection       (final Piece piece);
  boolean     handleZoom            (int amount);
};
