package fr.pierreqr.communitrix.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import fr.pierreqr.communitrix.Constants.SkinSize;
import fr.pierreqr.communitrix.networking.cmd.rx.RXBase;

public class StartupScreen extends BaseScreen {
  public final      Stage       stage;
  public final      Table       tblMain;
  public final      Label       lblTitle, lblStatus;

  public StartupScreen () {
    super                   ();
    
    // Prepare flat UI.
    stage                   = new Stage();
    // Prepare the main table.
    tblMain                 = new Table(ctx.skins.get(SkinSize.Medium));
    tblMain.setFillParent   (true);
    tblMain.pad             (5);
    tblMain.setDebug        (false);
    tblMain.top             ();
    stage.addActor          (tblMain);
    
    // Create title label.
    lblTitle                = new Label("Communitrix Startup", ctx.skins.get(SkinSize.Large));
    tblMain.add             (lblTitle);
    tblMain.row             ();
    // Create status label.
    lblStatus               = new Label("Please wait...", ctx.skins.get(SkinSize.Medium));
    tblMain.add             (lblStatus);
    tblMain.row             ();
  }
  
  @Override public void show () {}
  @Override public void hide () {}

  @Override public void pause () {}
  @Override public void resume () {}

  public void resize (final int w, final int h) {
    stage.getViewport().update(w, h, true);
  }
  @Override public void render (float delta) {
    stage.act         (delta);
    stage.draw        ();
  }

  @Override public void dispose () {}

  @Override public void showMessage (final MessageType type, final String message) {}

  @Override  public void onServerMessage (RXBase baseCmd) {
    switch (baseCmd.type) {
      case Connecting:
        lblStatus.setColor    (Color.WHITE);
        lblStatus.setText     ("Connecting to server...");
        break;
      case Connected:
        lblStatus.setColor    (Color.GREEN);
        lblStatus.setText     ("Connected to the server, now loading...");
        break;
      case Disconnected:
        lblStatus.setColor    (Color.ORANGE);
        lblStatus.setText     ("Disconnected.");
        break;
      case Error:
        lblStatus.setColor    (Color.RED);
        lblStatus.setText     ("Error!");
        break;
      default:
    }
  }

  @Override public boolean keyDown (final int keycode) {
    return false;
  }
  @Override public boolean keyUp (final int keycode) {
    return false;
  }
  @Override public boolean keyTyped (final char character) {
    return false;
  }
  @Override public boolean touchDown (final int x, final int y, final int ptr, final int btn) {
    return false;
  }
  @Override public boolean touchUp (final int x, final int y, final int ptr, final int btn) {
    return false;
  }
  @Override public boolean touchDragged (final int x, final int y, final int ptr) {
    return false;
  }
  @Override public boolean mouseMoved (final int x, final int y) {
    return false;
  }
  @Override public boolean scrolled (final int amount) {
    return false;
  }
}
