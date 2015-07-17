package fr.pierreqr.communitrix.screens;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.Constants;
import fr.pierreqr.communitrix.Constants.Key;
import fr.pierreqr.communitrix.Constants.SkinSize;
import fr.pierreqr.communitrix.ErrorResponder;
import fr.pierreqr.communitrix.networking.cmd.beans.CombatBean;
import fr.pierreqr.communitrix.networking.cmd.tx.TXCombatJoin;
import fr.pierreqr.communitrix.networking.cmd.tx.TXCombatList;
import fr.pierreqr.communitrix.networking.cmd.tx.TXRegister;
import fr.pierreqr.communitrix.screens.MainScreen.State;

public class GameScreenOverlay extends InputAdapter implements ErrorResponder {
  private       Communitrix     ctx;
  private       State           state;
  private final boolean         debug       = false;
  private final int             pad         = 5;

  private final ScreenSharedData          data;
  private final Timer           timer;
  private       Timer.Task      lastTask;
  private final Stage           stage;
  private final Skin            sknMini, sknMedium, sknLarge;
  private final Table           tblMain, tblCombats;
  private final Label           lblTitle, lblStatus;
  private       SelectBox<CombatBean>
                                lstCombats;
  private       Key             currentBinding  = null;

  public GameScreenOverlay (final ScreenSharedData newData) {
    data                    = newData;

    // Cache some global things.
    ctx                     = Communitrix.getInstance();
    sknMini                 = ctx.skins.get(SkinSize.Mini);
    sknMedium               = ctx.skins.get(SkinSize.Medium);
    sknLarge                = ctx.skins.get(SkinSize.Large);
    // Prepare UI timer.
    timer                   = new Timer();
    // Create our flat UI stage.
    stage                   = new Stage();
    // Prepare the main table.
    tblMain                 = new Table(sknMedium);
    tblMain.setFillParent   (true);
    tblMain.pad             (pad);
    tblMain.setDebug        (debug);
    tblMain.top             ();
    stage.addActor          (tblMain);
    // Prepare combats table.
    tblCombats              = new Table(sknMedium);
    tblCombats.pad          (pad);
    tblCombats.setDebug     (debug);
    tblCombats.top          ();
    // Prepare the title field.
    lblTitle                = new Label("", sknLarge);
    // Prepare the status field.
    lblStatus               = new Label("", sknMedium);
  }
  
  public void nextBinding (final Key newBinding) {
    flash             (String.format("Press any key for action %s...", Constants.KeyText.get( currentBinding = newBinding )), Color.GREEN);
  }
  public boolean keyUp (int keycode) {
    if (state!=State.Settings || keycode==Keys.ESCAPE)
      return false;
    
    Constants.Keys.put      (currentBinding, keycode);
    final int ordinal       = currentBinding.ordinal() + 1;
    if (ordinal<Key.values().length)
      nextBinding           (Key.values()[ordinal]);
    else {
      currentBinding        = null;
      flash                 ("All done! Press ESC to exit settings.", Color.GREEN);
    }
    return                  true;
  }

  // ErrorResponder implementation.
  public void setLastError (final int code, final String reason) {
    if (code==0)
      flash     (reason, Color.GREEN);
    else
      flash     (String.format("Error #%d: %s", code, reason), Color.RED);
  }
  private void flash (final String message, final Color color) {
    lblStatus.setText       (message);
    lblStatus.setColor      (color);
    if (lastTask!=null && lastTask.isScheduled())
      lastTask.cancel       ();
    timer.scheduleTask      (lastTask = new Timer.Task() { @Override public void run() { lblStatus.setText(""); } }, 2.0f);
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

  public GameScreenOverlay setState (final State newState) {
    if (state!=newState) {
      // Remove everything from the UI.
      tblMain.clear     ();
      // Add title row.
      tblMain.add       (lblTitle).center().colspan(3);
      tblMain.row       ();
      // Add status row.
      tblMain.add       (lblStatus).center().colspan(5);
      tblMain.row       ();
    }

    switch (state = newState) {
      case Settings:
        lblTitle.setText  ("Settings State");
        nextBinding       (Key.values()[0]);
        break;
      case Global:
        // Title row.
        lblTitle.setText  ("Global State.");
        
        // Login row.
        // Prepare the username input text field.
        final TextField txtUsername = new TextField("Doodloo", sknMedium);
        final TextButton btnSet = new TextButton("Set", sknMedium);
        btnSet.addListener(new ClickListener() {
          @Override public void clicked(final InputEvent e, final float x, final float y) {
            ctx
              .networkingManager
              .send(new TXRegister(txtUsername.getText()));
          }
        });
        tblMain.add       ("Username:").pad(pad).right();
        tblMain.add       (txtUsername).pad(pad).fill();
        tblMain.add       (btnSet).pad(pad).fill();
        tblMain.row       ();
        
        // Combats row.
        if (lstCombats==null) {
          lstCombats              = new SelectBox<CombatBean>(sknMedium);
          lstCombats.setItems     (data.combats);
        }
        // Buttons.
        final HorizontalGroup grpBtns = new HorizontalGroup();
        // Join button.
        final TextButton btnJoin = new TextButton("Join", sknMedium);
        btnJoin.addListener(new ClickListener() {
          @Override public void clicked (final InputEvent e, final float x, final float y) {
            ctx.networkingManager.send(new TXCombatJoin(lstCombats.getSelected().uuid));
          }
        });
        grpBtns.addActor  (btnJoin);
        // Refresh button.
        final TextButton btnRefresh = new TextButton("Refresh", sknMedium);
        btnRefresh.addListener(new ClickListener() {
          @Override public void clicked (final InputEvent e, final float x, final float y) {
            loadCombatList();
          }
        });
        grpBtns.addActor  (btnRefresh);
        
        tblMain.add       ("Combat:").pad(pad).right();
        tblMain.add       (lstCombats).pad(pad).fill();
        tblMain.add       (grpBtns).pad(pad).fill();
        tblMain.row       ();

        // Refresh combats list.
        loadCombatList    ();
        break;
      // Received the list of combats.
      case Joined:
        // Title row.
        if (data.combat.currentTurn==0)
          lblTitle.setText  ("Waiting for other players...");
        else
          lblTitle.setText  ("Starting combat...");
        break;
      case Gaming:
        lblTitle.setText  (String.format("In turn %d.", data.combat.currentTurn));
        break;
      case EndGame:
        lblTitle.setText  (String.format("Game Over"));
        flash             ("Press ESC to go back to the Global state.", Color.GREEN);
        break;
      default :
        break;
    }
    return this;
  }

  public void loadCombatList () {
    flash                       ("Loading combats list...", Color.GREEN);
    lstCombats.setDisabled      (true);
    tblCombats.clear            ();
    ctx.networkingManager.send  (new TXCombatList());
  }
  public void updateCombatList () {
    lstCombats.setItems         (data.combats);
    lstCombats.setDisabled      (false);;
  }
}
