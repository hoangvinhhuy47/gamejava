package games.core.exception;

public class BaseException extends RuntimeException {
    private int errorCode;
    public BaseException(){
        super();
    }
    public BaseException(String message){
        super(message);
    }

    public BaseException(int errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
