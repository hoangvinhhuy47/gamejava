package games.shootingfish.exception;

/**
 * Created by WINDNCC on 7/11/2017.
 */
public class RoomException extends RuntimeException {
    RoomException(){
        super();
    }
    public RoomException(String message){
        super(message);
    }
}
