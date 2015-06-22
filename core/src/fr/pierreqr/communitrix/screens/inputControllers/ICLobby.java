package fr.pierreqr.communitrix.screens.inputControllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import fr.pierreqr.communitrix.Communitrix;
import fr.pierreqr.communitrix.gameObjects.Piece;
import fr.pierreqr.communitrix.screens.SCLobby.CameraState;

public class ICLobby extends InputAdapter{
  public interface ICLobbyDelegate {
    Camera        getCamera       ();
    CameraState   getCameraState  ();
    void          setCameraState  (final CameraState state);
    Array<Piece>  getClickables   ();
    Array<Piece>  getPieces       ();
    Piece         getUnit         ();
    Piece         getTarget       ();
    int           getTurn         ();
    void          cyclePieces     (final int pieceIndex);
    void          translatePiece  (final Piece piece, final Vector3 axis);
    void          rotatePiece     (final Piece piece, final Vector3 axis, final int angle);
    void          playPiece       (final Piece piece);
  };
  
  private final         ICLobbyDelegate     delegate;
  private               Piece               selection         = null;
  private               Piece               previousSelection = null;
  private               int                 firstPieceIndex   = 0;
  private final         Vector3             dragOffset;
  private final         Vector3             position;
  private final         Vector3             selectionPos      = new Vector3(0,5,0);
  private               Vector3             vectorX           = new Vector3();
  private               Vector3             vectorY           = new Vector3();
  private final         Quaternion          tmpQuat           = new Quaternion();
  private final         Quaternion          tmpQuat2          = new Quaternion();
  private               boolean             pieceLocked       = false;
  
  public ICLobby (final ICLobbyDelegate delegate) {
    dragOffset      = new Vector3();
    position        = new Vector3();
    this.delegate   = delegate;
  }
  
  @Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
    // Reset selection.
    previousSelection = selection == delegate.getUnit() ? null : selection;
    selection               = null;
    // Pick a ray from the cam.
    final Ray   ray         = delegate.getCamera().getPickRay(screenX, screenY);
    // Dist will be a temp, while sDist will be the shortest found distance.
    float       dist, sDist = Float.MAX_VALUE;
    BoundingBox bounds      = null;
    // Iterate through all instances.
    final Array<Piece> all  = delegate.getClickables();
    for (final Piece obj : all) {
      obj.transform
        .getTranslation (position);
      dist              = ray.origin.dst2(position);
      //if (dist<sDist && Intersector.intersectRaySphere(ray, position, obj.radius, null)) {
      if (dist<sDist) {
        bounds          = new BoundingBox(obj.bounds);
        bounds.min.add  (position);
        bounds.max.add  (position);
        if (Intersector.intersectRayBounds(ray, bounds, null)) {
          sDist         = dist;
          
          selection     = obj;
          position
            .set            (ray.direction)
            .scl            (-ray.origin.y / ray.direction.y)
            .add            (ray.origin);
          selection
            .transform
            .getTranslation (dragOffset)
            .sub            (position);
        }
      }
    }
    if (selection==delegate.getTarget() && delegate.getCameraState() != CameraState.Target) {
      delegate.setCameraState(CameraState.Target);
    }
    else if(selection == delegate.getUnit() && delegate.getCameraState() != CameraState.Unit
            && previousSelection != null){
      // TODO move the selected piece close to the unit so that the player can manipulate both easily
      delegate.setCameraState(CameraState.Unit);
      delegate.translatePiece(previousSelection, selectionPos.sub(previousSelection.transform.getTranslation(position)));
      selectionPos.add(position);
    }
    return selection!=null;
  }
  
  public void update () {
    if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)){
      selection     = null;
      pieceLocked   = false;
      if(delegate.getCameraState() != CameraState.Pieces){
        delegate.setCameraState(CameraState.Pieces);
        delegate.cyclePieces(firstPieceIndex);
      }
    }
    
    if (selection==null) {
      if (Gdx.input.isKeyJustPressed(Keys.A)) {
        firstPieceIndex++;
        if (firstPieceIndex>=delegate.getPieces().size)
          firstPieceIndex  = 0;
        delegate.cyclePieces(firstPieceIndex);
      }
      else if (Gdx.input.isKeyJustPressed(Keys.D)) {
        firstPieceIndex--;
        if (firstPieceIndex<0)
          firstPieceIndex  = delegate.getPieces().size-1;
        delegate.cyclePieces(firstPieceIndex);
      }
    }
    // There is a selection, behave accordingly.
    else {
      // Handle relative rotation.
      if (Gdx.input.isKeyJustPressed(Keys.RIGHT)){
        calculateVectorYfor(selection);
        delegate.rotatePiece(selection, vectorY, 90);
      }else if (Gdx.input.isKeyJustPressed(Keys.LEFT)){
        calculateVectorYfor(selection);
        delegate.rotatePiece(selection, vectorY, -90);
      }
      if (Gdx.input.isKeyJustPressed(Keys.UP)){
        calculateVectorXfor(selection);
        delegate.rotatePiece(selection, vectorX, 90);
      }else if (Gdx.input.isKeyJustPressed(Keys.DOWN)){
        calculateVectorXfor(selection);
        delegate.rotatePiece(selection, vectorX, -90);
      }
      
      // Handle translation.
      if (selection!=delegate.getTarget() && selection != delegate.getUnit()) {
        if (Gdx.input.isKeyJustPressed(Keys.W))
          delegate.translatePiece(selection, Communitrix.PositiveZ);
        else if(Gdx.input.isKeyJustPressed(Keys.S))
          delegate.translatePiece(selection, Communitrix.NegativeZ);
        if (Gdx.input.isKeyJustPressed(Keys.A))
          delegate.translatePiece(selection, Communitrix.PositiveX);
        else if (Gdx.input.isKeyJustPressed(Keys.D))
          delegate.translatePiece(selection, Communitrix.NegativeX);
        if (Gdx.input.isKeyJustPressed(Keys.O))
          delegate.translatePiece(selection, Communitrix.PositiveY);
        else if (Gdx.input.isKeyJustPressed(Keys.L))
          delegate.translatePiece(selection, Communitrix.NegativeY);
        
        // Other actions.
        if (Gdx.input.isKeyJustPressed(Keys.ENTER) && 
            (delegate.getCameraState() == CameraState.Unit ||delegate.getTurn() == 1))
          if(!pieceLocked){
            pieceLocked = true;
            if(delegate.getTurn() != 1){
              //adds selected piece to the node of the unit
              delegate.getUnit().nodes.addAll(selection.nodes);
              // Calculates the quaternion to transform the unit rotation into the idt saves it to tmpQuat.
              delegate.getUnit().transform.getRotation(tmpQuat).conjugate().mul(tmpQuat2.idt());
              // Saves the selected piece translation and rotation in the world.
              selection.transform.getRotation(tmpQuat2);
              selection.transform.getTranslation(position).sub(delegate.getUnit().transform.getTranslation(vectorX));
              // Transforms the world rotation and position of the piece to local coords in the unit.
              delegate.getUnit().nodes.get(delegate.getUnit().nodes.size -1).globalTransform
                .set(position.mul(tmpQuat), tmpQuat.mul(tmpQuat2));
              delegate.getUnit().recomputeBounds();
              
              delegate.cyclePieces(firstPieceIndex);
              selection = delegate.getUnit();
            }
          }
          else{
            delegate.playPiece(selection);
            selection = null;
            pieceLocked = false;
          }
      }
    }
  }
  
  public void calculateVectorXfor(Piece obj){
    vectorX = obj.transform.getRotation(tmpQuat).transform(Vector3.Y);
//    vectorX = Vector3.Y.mul(obj.transform.getRotation(tmpQuat));
  }
  public void calculateVectorYfor(Piece obj){
    vectorY = obj.transform.getRotation(tmpQuat).transform(Vector3.X);
//    vectorY = Vector3.X.mul(obj.transform.getRotation(tmpQuat));
  }
  
}
