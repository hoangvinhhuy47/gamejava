package libs.database.mongodb.exception;

public class SaveToMongoNotSupport extends RuntimeException{
    public SaveToMongoNotSupport(){
        super();
    }
    public SaveToMongoNotSupport(String message){
        super(message);
    }
}
