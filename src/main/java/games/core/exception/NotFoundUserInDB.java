package games.core.exception;

public class NotFoundUserInDB extends BaseException{
    NotFoundUserInDB(){
        super();
    }
    public NotFoundUserInDB(String message){
        super(message);
    }
}
