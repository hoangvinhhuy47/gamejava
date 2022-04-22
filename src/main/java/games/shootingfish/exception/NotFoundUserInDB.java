package games.shootingfish.exception;

public class NotFoundUserInDB extends RuntimeException{
    NotFoundUserInDB(){
        super();
    }
    public NotFoundUserInDB(String message){
        super(message);
    }
}
