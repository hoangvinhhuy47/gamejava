package games.shootingfish.exception;

public class NullPlayerException extends RuntimeException {
    public NullPlayerException(){
        super();
    }

    public NullPlayerException(String message){
        super(message);
    }
}
