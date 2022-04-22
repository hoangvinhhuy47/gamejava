package games.core.exception;

public class NullUserException extends BaseException {
    public NullUserException(){
        super();
    }
    public NullUserException(String message){
        super(message);
    }
}
