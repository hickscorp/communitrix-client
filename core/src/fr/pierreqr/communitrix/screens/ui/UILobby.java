package fr.pierreqr.communitrix.screens.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.ErrorResponder;
import fr.pierreqr.communitrix.networking.commands.tx.TXCombatJoin;
import fr.pierreqr.communitrix.networking.commands.tx.TXCombatList;
import fr.pierreqr.communitrix.networking.commands.tx.TXRegister;
import fr.pierreqr.communitrix.screens.SCLobby;

public class UILobby extends InputAdapter implements ErrorResponder {
  private       Communitrix         ctx;
  private       SCLobby.State   state;
  private final boolean             debug       = true;
  private final int                 pad         = 5;

  private       String[]            combats;
  
  private final Timer               timer;
  private final Stage               stage;
  private final Skin                skin;
  private final Table               tblMain, tblCombats;
  private final Label               lblTitle, lblStatus;
  private final TextField           txtUsername;

  public UILobby () {
    // Cache some global things.
    ctx                     = Communitrix.getInstance();
    skin                    = ctx.uiSkin;
    // Prepare UI timer.
    timer                   = new Timer();
    // Create our flat UI stage.
    stage                   = new Stage();
    // Prepare the main table.
    tblMain                 = new Table(ctx.uiSkin);
    tblMain.setFillParent   (true);
    tblMain.pad             (pad);
    tblMain.setDebug        (debug);
    tblMain.top             ();
    stage.addActor          (tblMain);
    // Prepare combats table.
    tblCombats              = new Table(ctx.uiSkin);
    tblCombats.pad          (pad);
    tblCombats.setDebug     (debug);
    tblCombats.top          ();
    // Prepare the title field.
    lblTitle                = new Label("", skin);
    lblTitle.setFontScale   (1.3f);
    // Prepare the status field.
    lblStatus               = new Label("", skin);
    lblStatus.setColor      (Color.RED);
    // Prepare the username input text field.
    txtUsername             = new TextField("Doodloo", ctx.uiSkin);
  }

  // ErrorResponder implementation.
  public void setLastError (final int code, final String reason) {
    lblStatus.setText (String.format("Error #%d: %s", code, reason));
    timer.scheduleTask(new Timer.Task() { @Override public void run() { lblStatus.setText(""); } }, 3.0f);
  }
  
  // Getters
  public Stage getStage() { return stage; }

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

  public UILobby setState (final SCLobby.State state) {
    Gdx.app.log       ("LobbyUI", "Changing state to " + state + ".");
    this.state        = state;
    // Remove everything from the UI.
    tblMain.clear     ();
    // Add title row.
    tblMain.add       (lblTitle).colspan(2).center();
    tblMain.row       ();
    // Add status row.
    tblMain.add       (lblStatus).colspan(2).center();
    tblMain.row       ();

    switch (state) {
      case Global:
        // Title row.
        lblTitle.setText  ("Global State.");
        // Login row.
        tblMain.add       ("Username:").pad(pad).right();
        tblMain.add       (txtUsername).pad(pad).left();
        tblMain.row       ();
        // Add the combats table to the main table.
        tblMain.add       (tblCombats).colspan(2).pad(5);
        tblMain.row       ();
        // Refresh combats list.
        updateCombats     ();
        break;
      case Joined:
        // Clear combats table.
        tblCombats.clear  ();
        // Title row.
        lblTitle.setText  ("Joined state.");
        break;
      case Starting:
        // Title row.
        lblTitle.setText  ("Starting combat...");
        break;
      default :
        break;
    }
    return this;
  }

  public UILobby setCombats (final String[] combats) {
    this.combats    = combats;
    updateCombats   ();
    return          this;
  }

  public void updateCombats () {
    // Remove combats list.
    tblCombats.clear  ();
    // Add the combats list.
    if (combats!=null) {
      lblStatus.setText(String.format("Loaded %d combat(s).", combats.length));
      for (final String combat : combats) {
        // Button row.
        final TextButton btnJoin = new TextButton("Join", skin);
        btnJoin.addListener(new ClickListener() {
          @Override public void clicked(final InputEvent e, final float x, final float y) {
            ctx
              .networkingManager
              .send(new TXRegister(txtUsername.getText()))
              .send(new TXCombatJoin(combat));
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

  public UILobby loadCombatList () {
    lblStatus.setText  ("Loading combats list...");
    ctx
      .networkingManager
      .send           (new TXCombatList());
    return            this;
  }
}
