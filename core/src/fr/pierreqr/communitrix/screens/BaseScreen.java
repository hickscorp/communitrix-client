package fr.pierreqr.communitrix.screens;

import com.badlogic.gdx.Screen;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.ErrorResponder;
import fr.pierreqr.communitrix.networking.NetworkDelegate;

public abstract class BaseScreen implements Screen, ErrorResponder, NetworkDelegate {
  protected Communitrix ctx = Communitrix.getInstance();
}
