package libs.database.mongodb;

import com.mongodb.client.FindIterable;
import org.bson.conversions.Bson;

/**
 * @author Cuong Nguyen Cao
 * @since 1.0
 * @version 1.0
 */
public interface IMongoModel<T extends MGDocument> {

    /**
     * save document (extends MGDocument) to DB
     * @param document wanna save
     * @throws NoSuchFieldException Have field not match between Class and DB
     * @throws IllegalAccessException Field can't access out side class
     */
    public boolean saveToMongo(T document) throws NoSuchFieldException, IllegalAccessException;

    /**
     * updateDB data from DB to document (extends MGDocument) object
     * required primary key
     * @param document object wanna updateDB from db
     * @throws NoSuchFieldException Have field not match between Class and DB
     * @throws IllegalAccessException Field can't access out side class
     */
    public boolean updateFromMongo(T document) throws IllegalAccessException, NoSuchFieldException;

    public boolean updateFromMongoByUniqueField(T document, String ...fieldName) throws NoSuchFieldException, IllegalAccessException;

    public boolean deleteFromMongo(T document);

    public boolean isDBExists(Bson filter);

    public boolean isDBExists(T document);

    public FindIterable find(Bson filter);

    public long getCountCollection();

    long getMaxIDCurrent();
}
