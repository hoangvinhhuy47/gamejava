package libs.database.mongodb.exception;

public class NotFoundUniqueKey extends RuntimeException{
    public NotFoundUniqueKey(){
        super();
    }

    public NotFoundUniqueKey(String message){
        super(message);
    }
}
