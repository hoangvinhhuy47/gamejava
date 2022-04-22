package libs.database.mongodb.exception;
/**
 * @author Cuong Nguyen Cao
 * @since 1.0
 * @version 1.0
 */
public class NotFoundDocumentInDatabase extends RuntimeException{
    public NotFoundDocumentInDatabase(){
        super();
    }

    public NotFoundDocumentInDatabase(String message){
        super(message);
    }

}
