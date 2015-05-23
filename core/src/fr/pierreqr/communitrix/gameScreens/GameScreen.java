package fr.pierreqr.communitrix.gameScreens;

import fr.pierreqr.communitrix.LogicManager;

public interface GameScreen {
  void      create    (final LogicManager logicManager);
  void      dispose   ();
  void      resize    (final int width, final int height);
  void      pause     ();
  void      resume    ();
  void      render    ();
}
