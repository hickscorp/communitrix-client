package fr.pierreqr.communitrix;

import java.util.HashMap;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;

public class Constants {
  // Skin sizes.
  public enum SkinSize { Mini, Medium, Large };
  
  // Cube textures.
  public enum CubeFace {
    Forward,         Backward,         Top,         Bottom,         Left,         Right,
    ForwardCollides, BackwardCollides, TopCollides, BottomCollides, LeftCollides, RightCollides;
  };
  
  // All key bindings.
  public static class Key {
      public final static int   Alt           = 0;
      public final static int   MoveForward   = 1;
      public final static int   MoveBackward  = 2;
      public final static int   MoveLeft      = 3;
      public final static int   MoveRight     = 4;
      public final static int   MoveUp        = 5;
      public final static int   MoveDown      = 6;
      public final static int   RotateUp      = 7;
      public final static int   RotateDown    = 8;
      public final static int   RotateLeft    = 9;
      public final static int   RotateRight   = 10;
      public final static int   CycleView     = 11;
      public final static int   Reset         = 12;
      public final static int   Validate      = 13;
      public final static int   Cancel        = 14;
      public final static int   Count         = 15;
  };
  public final static     int[] Keys          = new int[Key.Count];
  static {
    Keys[Key.Alt]           = Input.Keys.SHIFT_LEFT;
    Keys[Key.MoveForward]   = Input.Keys.W;
    Keys[Key.MoveBackward]  = Input.Keys.S;
    Keys[Key.MoveLeft]      = Input.Keys.A;
    Keys[Key.MoveRight]     = Input.Keys.D;
    Keys[Key.MoveUp]        = Input.Keys.O;
    Keys[Key.MoveDown]      = Input.Keys.L;
    Keys[Key.RotateUp]      = Input.Keys.UP;
    Keys[Key.RotateDown]    = Input.Keys.DOWN;
    Keys[Key.RotateLeft]    = Input.Keys.LEFT;
    Keys[Key.RotateRight]   = Input.Keys.RIGHT;
    Keys[Key.CycleView]     = Input.Keys.SPACE;
    Keys[Key.Reset]         = Input.Keys.R;
    Keys[Key.Validate]      = Input.Keys.ENTER;
    Keys[Key.Cancel]        = Input.Keys.ESCAPE;
  }
  // Keys instructions.
  public final static HashMap<Number, String> KeyText = new HashMap<Number, String>();
  static {
    KeyText.put (Key.Alt,           "Alt. Mode");
    KeyText.put (Key.MoveForward,   "Move Forward");
    KeyText.put (Key.MoveBackward,  "Move Backward");
    KeyText.put (Key.MoveLeft,      "Move Left");
    KeyText.put (Key.MoveRight,     "Move Right");
    KeyText.put (Key.MoveUp,        "Move Up");
    KeyText.put (Key.MoveDown,      "Move Down");
    KeyText.put (Key.RotateUp,      "Rotate Up");
    KeyText.put (Key.RotateDown,    "Rotate Down");
    KeyText.put (Key.RotateLeft,    "Rotate Left");
    KeyText.put (Key.RotateRight,   "Rotate Right");
    KeyText.put (Key.CycleView,     "Cycle View");
    KeyText.put (Key.Reset,         "Reset");
    KeyText.put (Key.Validate,      "Validate");
    KeyText.put (Key.Cancel,        "Cancel");
  };
  
  // Some rotation constants.
  public final static HashMap<Number, Vector3> Directions = new HashMap<Number, Vector3>();
  static {
    Directions.put(Key.MoveForward,   new Vector3( 0,  0,  1));
    Directions.put(Key.MoveBackward,  new Vector3( 0,  0, -1));
    Directions.put(Key.MoveLeft,      new Vector3( 1,  0,  0));
    Directions.put(Key.MoveRight,     new Vector3(-1,  0,  0));
    Directions.put(Key.MoveUp,        new Vector3( 0,  1,  0));
    Directions.put(Key.MoveDown,      new Vector3( 0, -1,  0));
  };
}
