package games.shootingfish.exception;

/**
 * Created by WINDNCC on 7/11/2017.
 */
public class TableException extends RuntimeException {
    TableException(){
        super();
    }
    public TableException(String message){
        super(message);
    }
}
