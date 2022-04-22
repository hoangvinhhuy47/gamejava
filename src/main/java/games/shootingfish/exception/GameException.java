package games.shootingfish.exception;

/**
 * Created by WINDNCC on 7/11/2017.
 */
public class GameException extends RuntimeException {
    GameException(){
        super();
    }
    GameException(String message){
        super(message);
    }
}
