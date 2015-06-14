package fr.pierreqr.communitrix.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.networking.commands.tx.TXCombatJoin;
import fr.pierreqr.communitrix.networking.commands.tx.TXCombatList;
import fr.pierreqr.communitrix.networking.commands.tx.TXRegister;

public class LobbyUIManager extends InputAdapter {
  private       Communitrix         ctx;
  private       LobbyScreen.State   state;
  private final boolean             debug       = false;
  private final int                 pad         = 5;

  private       String[]            combats;

  private       Stage               stage;
  private       Skin                skin;
  private       Table               tblMain, tblCombats;
  private       Label               lblTitle;
  private       TextField           txtUsername;

  public LobbyUIManager () {
    // Cache some global things.
    ctx                     = Communitrix.getInstance();
    skin                    = ctx.uiSkin;
    // Create our flat UI stage.
    stage                   = new Stage();
    // Prepare main root table.
    stage.addActor          (tblMain = new Table());
    // Prepare the main table.
    tblMain                 = new Table(ctx.uiSkin);
    tblMain.setFillParent   (true);
    tblMain.pad             (pad);
    tblMain.setDebug        (debug);
    tblMain.top();
    stage.addActor          (tblMain);
    lblTitle                = new Label("Please wait...", ctx.uiSkin);
    txtUsername             = new TextField("Doodloo", ctx.uiSkin);
  }
  public Stage getStage() {
    return stage;
  }

  public void show() {
    setState          (state);
  }
  public void hide() {
    tblMain.clear     ();
  }
  public void actAndDraw (final float delta) {
    stage.act         (delta);
    stage.draw        ();
  }
  public void resize (final int width, final int height) {
    stage.getViewport().update(width, height, true);
  }

  public LobbyUIManager setState (final LobbyScreen.State state) {
    Gdx.app.log                 ("LobbyUI", "Changing state to " + state + ".");
    this.state                  = state;
    // Remove everything from the UI.
    tblMain.clear               ();
    switch (state) {
      case Global:
        // Title row.
        tblMain.add       ("Global State.").colspan(2).pad(pad).center();
        tblMain.row       ();
        // Login row.
        tblMain.add       ("Username:").pad(pad);
        tblMain.add       (txtUsername).pad(pad);
        tblMain.row       ();
        // Refresh combats list.
        updateCombats     ();
        break;
      case Joined:
        // Clear combats table.
        tblCombats.clear  ();
        tblCombats        = null;
        // Title row.
        tblMain.add       ("Joined state.").pad(pad).expandX().left();
        tblMain.row       ().left();
        break;
      case Starting:
        // Title row.
        tblMain.add       ("Starting combat. Please stand by...").pad(pad).expandX().left();
        tblMain.row       ().left();
        break;
      default :
        break;
    }
    return this;
  }

  public LobbyUIManager setCombats (final String[] combats) {
    this.combats    = combats;
    updateCombats   ();
    return          this;
  }

  public void updateCombats () {
    if (tblCombats==null) {
      // Add the combat table to the root table.
      tblCombats              = new Table();
      tblCombats.pad          (5);
      tblCombats.setSkin      (skin);
      tblCombats.setDebug     (debug);
      tblMain.add             (tblCombats).colspan(2).pad(5);
    }
    else
      tblCombats.clear  ();

    // Add title row.
    tblCombats.add(lblTitle).center().getActor();
    tblCombats.row();

    // Add the combats list.
    if (combats!=null) {
      lblTitle.setText(String.format("Combat List: %d", combats.length));
      for (final String combat : combats) {
        // Button row.
        final TextButton btnJoin = new TextButton("Join", skin);
        btnJoin.addListener(new ClickListener() {
          @Override public void clicked(final InputEvent e, final float x, final float y) {
            ctx
              .networkingManager
              .send(new TXCombatJoin(combat))
              .send(new TXRegister(txtUsername.getText()));
          }
        });
        tblCombats.add        (combat).pad(pad).right();
        tblCombats.add        (btnJoin).pad(pad).left();
        tblCombats.row        ();
      }
    }

    // Add the refresh button to the combat screen.
    final TextButton btnRefresh = new TextButton("Refresh", skin);
    btnRefresh.addListener(new ClickListener() {
      @Override public void clicked(final InputEvent e, final float x, final float y) {
        loadCombatList();
      }
    });
    tblCombats.add            (btnRefresh).colspan(2).center();
    tblCombats.row            ();
  }

  public LobbyUIManager loadCombatList () {
    lblTitle.setText  ("Loading combats list...");
    ctx
      .networkingManager
      .send           (new TXCombatList());
    return            this;
  }
}
