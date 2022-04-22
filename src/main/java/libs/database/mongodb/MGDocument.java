package libs.database.mongodb;

import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import libs.database.mongodb.exception.NullCollection;
import org.bson.Document;

public abstract class MGDocument {
    protected Document changed = new Document();

    Document getChanged() {
        return changed;
    }

    /**
     * update from db to this document
     * @return instance of MGDocument
     * @throws IllegalAccessException can't access to field in object
     */
    public abstract MGDocument updateDB() throws IllegalAccessException, NullCollection, NotFoundDocumentInDatabase;
    public abstract MGDocument saveDB() throws IllegalAccessException, NoSuchFieldException;//{
        //throw new SaveToMongoNotSupport("Need implement this method to save!");
    //}
}
