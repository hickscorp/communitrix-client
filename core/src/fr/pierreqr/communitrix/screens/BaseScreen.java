package fr.pierreqr.communitrix.screens;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Disposable;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.ErrorResponder;
import fr.pierreqr.communitrix.networking.NetworkingManager.NetworkDelegate;

public abstract class BaseScreen implements Disposable, Screen, ErrorResponder, NetworkDelegate, InputProcessor {
  
  protected final   Communitrix   ctx;
  
  protected BaseScreen () {
    ctx             = Communitrix.getInstance();
  }
}
