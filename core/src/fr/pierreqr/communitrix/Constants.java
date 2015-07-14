package fr.pierreqr.communitrix;

import java.util.EnumMap;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;

public class Constants {
  // Skin sizes.
  public enum SkinSize {
    Mini, Medium, Large
  };
  
  // Cube textures.
  public enum CubeFace {
    Forward,         Backward,         Top,         Bottom,         Left,         Right,
    ForwardCollides, BackwardCollides, TopCollides, BottomCollides, LeftCollides, RightCollides;
  };
  
  // All key bindings.
  public enum Key {
    MoveForward, MoveBackward, MoveLeft, MoveRight, MoveUp, MoveDown,
    RotateUp, RotateDown, RotateLeft, RotateRight,
    CycleView, Reset, Validate, Cancel;
  };
  // Key bindings.
  public final static EnumMap<Key, Integer> Keys    = new EnumMap<Key, Integer>(Key.class);
  static {
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
  };
  // Keys instructions.
  public final static EnumMap<Key, String>  KeyText = new EnumMap<Key, String>(Key.class);
  static {
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
  public final static EnumMap<Key, Vector3> Directions = new EnumMap<Key, Vector3>(Key.class);
  static {
    Directions.put(Key.MoveForward,   new Vector3( 0,  0,  1));
    Directions.put(Key.MoveBackward,  new Vector3( 0,  0, -1));
    Directions.put(Key.MoveLeft,      new Vector3( 1,  0,  0));
    Directions.put(Key.MoveRight,     new Vector3(-1,  0,  0));
    Directions.put(Key.MoveUp,        new Vector3( 0,  1,  0));
    Directions.put(Key.MoveDown,      new Vector3( 0, -1,  0));
  };
}
