package libs.database.mongodb.exception;
/**
 * @author Cuong Nguyen Cao
 * @since 1.0
 * @version 1.0
 */
public class NullCollection extends RuntimeException {
    public NullCollection(){
        super();
    }
    public NullCollection(String message){
        super(message);
    }
}
