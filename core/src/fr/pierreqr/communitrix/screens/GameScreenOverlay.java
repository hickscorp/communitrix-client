package fr.pierreqr.communitrix.screens;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.Constants.SkinSize;
import fr.pierreqr.communitrix.ErrorResponder;
import fr.pierreqr.communitrix.networking.cmd.beans.CombatBean;
import fr.pierreqr.communitrix.networking.cmd.beans.PlayerBean;
import fr.pierreqr.communitrix.networking.cmd.tx.TXCombatJoin;
import fr.pierreqr.communitrix.networking.cmd.tx.TXCombatList;
import fr.pierreqr.communitrix.networking.cmd.tx.TXRegister;
import fr.pierreqr.communitrix.screens.MainScreen.State;

public class GameScreenOverlay extends InputAdapter implements ErrorResponder {
  private       Communitrix     ctx;
  private       State           state;
  private final boolean         debug       = false;
  private final int             pad         = 3;

  private final MainScreen      screen;
  private final Timer           timer;
  private       Timer.Task      lastTask;
  public  final Stage           stage;
  private final Skin            sknMini, sknMedium, sknLarge;
  private final Table           tblMain, tblCombats;
  private final Label           lblTitle, lblStatus;
  private       SelectBox<CombatBean>
                                lstCombats;

  public GameScreenOverlay (final MainScreen _screen) {
    screen                  = _screen;
    
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
    
    Window wndPlayers = new Window("Players", sknMini);
    List<PlayerBean> lstPlayers = new List<PlayerBean>(sknMini);
    wndPlayers.addActor(lstPlayers);
    wndPlayers.setDebug(false);
    stage.addActor(wndPlayers);
    
  }
  
  // ErrorResponder implementation.
  public void showMessage (final MessageType type, final String message) {
    final Color color;
    switch (type) {
      case Debug:
        color       = Color.BLUE;
        break;
      case Message:
        color       = Color.WHITE;
        break;
      case Warning:
        color       = Color.ORANGE;
        break;
      default:
        color       = Color.RED;
    }
    lblStatus.setText       (message);
    lblStatus.setColor      (color);
    if (lastTask!=null && lastTask.isScheduled())
      lastTask.cancel       ();
    timer.scheduleTask      (lastTask = new Timer.Task() { @Override public void run() { lblStatus.setText(""); } }, 2.0f);
  }

  public void actAndDraw (final float delta) {
    stage.act         (delta);
    stage.draw        ();
  }
  public void resize (final int w, final int h) {
    stage.getViewport().update(w, h, true);
  }

  public GameScreenOverlay setState (final State newState) {
    if (newState==state)
      return this;
    // Remove everything from the UI.
    tblMain.clear     ();
    // Add title row.
    tblMain.add       (lblTitle).center().colspan(4);
    tblMain.row       ();
    // Add status row.
    tblMain.add       (lblStatus).center().colspan(4);
    tblMain.row       ();

    switch (state = newState) {
      case Settings:
        lblTitle.setText  ("Settings State");
        break;
      
      case Global:
        Table grpBtns;

        // Title row.
        lblTitle.setText  ("Global State.");
        
        // Login row.
        final TextField txtUsername = new TextField("Doodloo", sknMedium);
        final TextButton btnSet = new TextButton("Set", sknMedium);
        btnSet.addListener(new ClickListener() {
          @Override public void clicked(final InputEvent e, final float x, final float y) {
            ctx
              .net
              .send(new TXRegister(txtUsername.getText()));
          }
        });
        grpBtns           = new Table(sknMedium).left();
        grpBtns.add       (btnSet).pad(pad).width(100);

        tblMain.add       ("Username:").pad(pad).right();
        tblMain.add       (txtUsername).pad(pad).fill();
        tblMain.add       (grpBtns).pad(pad).fill();
        tblMain.row       ();
        
        // Combats row.
        if (lstCombats==null) {
          lstCombats              = new SelectBox<CombatBean>(sknMedium);
          lstCombats.setItems     (screen.combats);
        }
        // Join button.
        final TextButton btnJoin = new TextButton("Join", sknMedium);
        btnJoin.addListener(new ClickListener() {
          @Override public void clicked (final InputEvent e, final float x, final float y) {
            if (lstCombats!=null && lstCombats.getSelected()!=null)
              ctx.net.send(new TXCombatJoin(lstCombats.getSelected().uuid));
          }
        });
        // Refresh button.
        final TextButton btnRefresh = new TextButton("Refresh", sknMedium);
        btnRefresh.addListener(new ClickListener() {
          @Override public void clicked (final InputEvent e, final float x, final float y) {
            loadCombatList();
          }
        });
        
        grpBtns           = new Table(sknMedium).left();
        grpBtns.add       (btnJoin).pad(pad).width(100);
        grpBtns.add       (btnRefresh).pad(pad).width(100);
        
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
        if (screen.combat.currentTurn==0)
          lblTitle.setText  ("Waiting for other players...");
        else
          lblTitle.setText  ("Starting combat...");
        break;
      case Gaming:
        lblTitle.setText    (String.format("In turn %d.", screen.combat.currentTurn));
        break;
      case EndGame:
        lblTitle.setText    (String.format("Game Over"));
        ctx.showMessage     (MessageType.Warning, "Press ESC to go back to the Global state.");
        break;
      default :
        break;
    }
    return this;
  }

  public void loadCombatList () {
    ctx.showMessage             (MessageType.Message, "Loading combats list...");
    lstCombats.setVisible       (false);
    lstCombats.setDisabled      (true);
    tblCombats.clear            ();
    ctx.net.send  (new TXCombatList());
  }
  public void updateCombatList () {
    lstCombats.setItems         (screen.combats);
    lstCombats.setVisible       (true);
    lstCombats.setDisabled      (false);
  }
}
