package games.shootingfish.exception;

/**
 * Created by WINDNCC on 7/6/2017.
 */
public class StateException extends RuntimeException {
    StateException(){

    }
    public StateException(String message){
        super(message);
    }
}
