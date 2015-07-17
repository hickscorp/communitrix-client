package fr.pierreqr.communitrix.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import fr.pierreqr.communitrix.Constants;
import fr.pierreqr.communitrix.Constants.Key;

public class GameScreenController extends InputAdapter {
  private final       GameScreenDelegate       delegate;
  public              boolean               alt               = false;
  
  public GameScreenController (final GameScreenDelegate newDelegate) {
    delegate   = newDelegate;
  }
  
  @Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
    return delegate.handleSelection(delegate.getClickableAt(screenX, screenY));
  }
  @Override public boolean scrolled (int amount) {
    return delegate.handleZoom(amount);
  }
  
  public final boolean[] keys = new boolean[Key.values().length];
  public boolean[] updateKeys () {
    boolean newAlt = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT);
    // We just switched to ALT mode.
    if (newAlt && !alt)       alt  = true;
    else if (alt && !newAlt)  alt = false;
    // Read all required keys.
    for (final Key key : Key.values())
      keys[key.ordinal()]  = Gdx.input.isKeyJustPressed(Constants.Keys.get(key));
    return                keys;
  }
}
