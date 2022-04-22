package libs.database.mongodb.exception;
/**
 * @author Cuong Nguyen Cao
 * @since 1.0
 * @version 1.0
 */
public class NotFoundPrimaryKey extends RuntimeException {
    public NotFoundPrimaryKey(){
        super();
    }
    public NotFoundPrimaryKey(String message){
        super(message);
    }
}
