package fr.pierreqr.communitrix;

import java.util.EnumMap;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;

public class Constants {
  // Possible directions around a cube to check for.
  public enum CubeFace {
    Forward,         Backward,         Top,         Bottom,         Left,         Right,
    ForwardCollides, BackwardCollides, TopCollides, BottomCollides, LeftCollides, RightCollides;
  }
  public enum Key {
    MoveForward, MoveBackward, MoveLeft, MoveRight, MoveUp, MoveDown,
    RotateUp, RotateDown, RotateLeft, RotateRight,
    CycleView, Reset, Validate, Cancel;
  }
  // Key bindings.
  public final static EnumMap<Key, Integer> Keys    = new EnumMap<Key, Integer>(Key.class);
  public final static EnumMap<Key, String>  KeyText = new EnumMap<Key, String>(Key.class);
  static {
  };

  
  // Some rotation constants.
  public final static   Vector3     PositiveX       = new Vector3( 1,  0,  0);
  public final static   Vector3     NegativeX       = new Vector3(-1,  0,  0);
  public final static   Vector3     PositiveY       = new Vector3( 0,  1,  0);
  public final static   Vector3     NegativeY       = new Vector3( 0, -1,  0);
  public final static   Vector3     PositiveZ       = new Vector3( 0,  0,  1);
  public final static   Vector3     NegativeZ       = new Vector3( 0,  0, -1);
  
  static {
    // Bindings configuration.
    Keys.put    (Key.MoveForward,   Input.Keys.W);
    Keys.put    (Key.MoveBackward,  Input.Keys.S);
    Keys.put    (Key.MoveLeft,      Input.Keys.A);
    Keys.put    (Key.MoveRight,     Input.Keys.D);
    Keys.put    (Key.MoveUp,        Input.Keys.O);
    Keys.put    (Key.MoveDown,      Input.Keys.L);
    Keys.put    (Key.RotateUp,      Input.Keys.UP);
    Keys.put    (Key.RotateDown,    Input.Keys.DOWN);
    Keys.put    (Key.RotateLeft,    Input.Keys.LEFT);
    Keys.put    (Key.RotateRight,   Input.Keys.RIGHT);
    Keys.put    (Key.CycleView,     Input.Keys.SPACE);
    Keys.put    (Key.Reset,         Input.Keys.R);
    Keys.put    (Key.Validate,      Input.Keys.ENTER);
    Keys.put    (Key.Cancel,        Input.Keys.ESCAPE);
    // Keys instructions.
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
    KeyText.put (Key.CycleView,     "Cycle Camera");
    KeyText.put (Key.Reset,         "Reset");
    KeyText.put (Key.Validate,      "Validate");
    KeyText.put (Key.Cancel,        "Cancel");
  }
}
