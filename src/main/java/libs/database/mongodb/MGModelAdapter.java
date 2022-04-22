package libs.database.mongodb;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldUnique;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import libs.database.mongodb.exception.NullCollection;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * @author Cuong Nguyen Cao
 * @since 1.0
 * @version 1.0
 */

public abstract class MGModelAdapter<T extends MGDocument> implements IMongoModel<T> {
    protected MongoCollection<Document> mgCollection;
    protected MGModelAdapter(MongoDatabase database){
        //Set this use (Collection)
        mgCollection = database.getCollection(MongoDB.getCollectionName(this));
    }

    public void dropCollection() {
        mgCollection.drop();
    }

    synchronized public long getNextId(){
        return this.getMaxIDCurrent() + 1;
    }

    @Override
    public long getMaxIDCurrent() {
        Document document = getDocumentWithIdMax();//mgCollection.find().sort(new Document("_id", -1)).first();
        if (document != null && document.containsKey("_id")) {
            try {
                return document.getLong("_id");
            } catch (ClassCastException ex){
                return document.getInteger("_id");
            }
        }
        return 0;
    }

    protected Document getDocumentWithIdMax() {
        return mgCollection.find().sort(new Document("_id", -1)).first();
    }

    @Override
    public long getCountCollection() {
        return mgCollection.count();
    }

    public MongoCollection<Document> getMgCollection() {
        return mgCollection;
    }

    @Override
    public  boolean saveToMongo(T document) {
        try{
            if (mgCollection != null && !document.changed.isEmpty()){
                //Convert MGDocument to BsonDocument
                Document document2 = new Document();
                MongoDB.getDocumentFieldValue(document).forEach(document2::append);
                //Check if this document exists in database -> do updateDB
                Bson checkExistsCond = null;
                for (String pk : MongoDB.getDocumentFieldPK(document)){
                    Bson bsonx = eq(pk, document2.get(pk));
                    if (checkExistsCond == null) {
                        checkExistsCond = bsonx;
                    } else {
                        checkExistsCond = and(checkExistsCond, bsonx);
                    }
                }
                if (mgCollection.find(checkExistsCond).first() != null){
                    mgCollection.updateOne(checkExistsCond, new Document("$set", document.getChanged()), new UpdateOptions().upsert(true));
                } else {
                    //if document not exists in db do insert
                    mgCollection.insertOne(document2);
                }
                return true;
            }
            throw new NullCollection("[saveToMongo in MGModelAdapter] - You must set Collection for MGModelAdapter before use it!");
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateFromMongo(T document) throws NotFoundDocumentInDatabase, NullCollection {
        try {
            if (mgCollection == null) throw new NullCollection("[updateFromMongo in MGModelAdapter] - You must set Collection for MGModelAdapter before use it!");
            Bson getCondition = MongoDB.getPKCondition(document);
            Document getDocument = mgCollection.find(getCondition).first();
            if (getDocument == null) throw new NotFoundDocumentInDatabase("[updateFromMongo in MGModelAdapter] - Not found Document with condition " + getCondition.toString());
            Field[] fields = document.getClass().getDeclaredFields();
            for (Field field : fields)
            {
                if (field.isAnnotationPresent(DocumentField.class) && getDocument.containsKey(field.getAnnotation(DocumentField.class).value())){
                    field.setAccessible(true);
                    field.set(document, getDocument.get(field.getAnnotation(DocumentField.class).value()));
                }
            }
            return true;
        } catch (IllegalAccessException e){
            return false;
        }

    }

    public void createByDocument(T t, Document document) throws IllegalAccessException {
        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields){
            if (field.isAnnotationPresent(DocumentField.class)){
                field.setAccessible(true);
                field.set(t, document.get(field.getAnnotation(DocumentField.class).value()));
            }
        }
    }

    @Override
    public boolean updateFromMongoByUniqueField(T document, String ...DBFieldName) throws NoSuchFieldException, IllegalAccessException {
        //try{
            if (mgCollection == null) throw new NullCollection("[updateFromMongoByUniqueField in MGModelAdapter] - You must set Collection for MGModelAdapter before use it!");
            //Lấy cái field tương ứng với DBFieldName trong cái document ra
            Bson getCondition = null;
            for (String fieldCheckStr : DBFieldName) {
                    Field fieldCheck = MongoDB.getDocFieldByDBField(document, fieldCheckStr);
                    //Nếu field name đưa vào không phải là unique
                    if (!fieldCheck.isAnnotationPresent(DocumentFieldUnique.class)) return false;
                    //Ngược lại
                    fieldCheck.setAccessible(true);
                    if (getCondition == null){
                        getCondition = eq(fieldCheckStr, fieldCheck.get(document));
                    } else {
                        getCondition = and(getCondition, eq(fieldCheckStr, fieldCheck.get(document)));
                    }
            }

            Document getDocument = mgCollection.find(getCondition).first();
            if (getDocument == null) throw new NotFoundDocumentInDatabase("[updateFromMongoByUniqueField in MGModelAdapter] - Not found Document with condition " + getCondition.toString());

            //Thực hiện đẩy dữ liệu từ DB sang object
            Field[] fields = document.getClass().getDeclaredFields();
            for (Field field : fields){
                if (field.isAnnotationPresent(DocumentField.class) && getDocument.containsKey(field.getAnnotation(DocumentField.class).value())){
                    field.setAccessible(true);
                    field.set(document, getDocument.get(field.getAnnotation(DocumentField.class).value()));
                }
            }
            return true;
        //}
//        catch (IllegalAccessException | NoSuchFieldException e) {
//            e.printStackTrace();
//            return false;
//        }
    }

    @Override
    public boolean deleteFromMongo(T document) {
        if (mgCollection == null) throw new NullCollection("[updateFromMongo in MGModelAdapter] - You must set Collection for MGModelAdapter before use it!");
        Bson getCondition = MongoDB.getPKCondition(document);
        Document getDocument = mgCollection.find(getCondition).first();
        if (getDocument == null) throw new NotFoundDocumentInDatabase("[updateFromMongo in MGModelAdapter] - Not found Document with condition " + getCondition.toString());
        return mgCollection.deleteOne(getCondition).getDeletedCount() > 0;
    }

    @Override
    public boolean isDBExists(Bson filter){
        return mgCollection.find(filter).first() != null;
    }

    @Override
    public boolean isDBExists(T document) {
        Bson getCondition = MongoDB.getPKCondition(document);
        return isDBExists(getCondition);
    }

    @Override
    public FindIterable<Document> find(Bson filter) {
        return mgCollection.find(filter);
    }
}
