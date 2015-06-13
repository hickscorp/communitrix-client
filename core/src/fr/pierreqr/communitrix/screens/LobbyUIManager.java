package fr.pierreqr.communitrix.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.networking.commands.tx.TXCombatJoin;
import fr.pierreqr.communitrix.networking.commands.tx.TXRegister;

public class LobbyUIManager {
  private       Communitrix         ctx;
  private       String[]            combats;
  private       LobbyScreen.State   state;
  
  private       Stage               uiStage;
  private       Table               tblMain;
  private       TextField           txtUsername;
  
  public LobbyUIManager () {
    ctx                         = Communitrix.getInstance();
    uiStage                     = new Stage();
    tblMain                     = new Table(ctx.uiSkin);
    tblMain.setFillParent       (true);
    uiStage.addActor            (tblMain);
    Gdx.input.setInputProcessor (uiStage);
    
    txtUsername = new TextField("", ctx.uiSkin);
  }
  
  public void show() {
    uiStage.clear ();
    setState      (state);
  }
  public void hide() {
    uiStage.clear ();
  }
  public void actAndDraw (final float delta) {
    uiStage.act         (delta);
    uiStage.draw        ();
  }
  public void resize (final int width, final int height) {
    uiStage.getViewport().update(width, height, true);
  }
  
  public LobbyUIManager setState (final LobbyScreen.State state) {
    Gdx.app.log       ("LobbyUI", "Changing state to " + state + ".");
    this.state        = state;
    final Skin skin   = tblMain.getSkin();
    tblMain.clear     ();
    switch (state) {
      case Global :
        // Title row.
        tblMain.add     ("Global State.");
        tblMain.row     ();
        // Login row.
        tblMain.add     ("Username");
        tblMain.add     (txtUsername);
        tblMain.row     ();
        
        if (combats==null)
          break;
        final Table tblCombats  = new Table();
        tblCombats.setSkin      (skin);
        for (final String combat : combats) {
          // Button row.
          final TextButton btnJoin = new TextButton("Join", skin);
          btnJoin.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
              ctx
                .networkingManager
                .send(new TXRegister(txtUsername.getText()));
              ctx
                .networkingManager
                .send(new TXCombatJoin(combat));
            }
          });
          tblCombats.add        (combat);
          tblCombats.add        (btnJoin);
          tblCombats.row        ();
        }
        tblMain.add     (tblCombats);
        break;
      case Joined:
        tblMain.add     ("Joined State.");
        break;
      case Starting:
        tblMain.add     ("Starting State.");
        break;
      default :
        break;
    }
    return this;
  }
  
  public LobbyUIManager setCombats (final String[] combats) {
    this.combats    = combats;
    setState        (state);
    return          this;
  }
}
