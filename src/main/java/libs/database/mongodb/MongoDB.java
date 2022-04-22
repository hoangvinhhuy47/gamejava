package libs.database.mongodb;

import libs.database.mongodb.annotation.*;
import libs.database.mongodb.exception.NotFoundPrimaryKey;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class MongoDB {

    /**
     *
     * @param document instance of MGDocument
     * @return Map contains value of Document with key in database ex: "_id = 1", "user_name = cuongdeptrai"
     * @throws IllegalAccessException can't access to field in MGDocument
     * @throws NoSuchFieldException field does not exists in MGDocument
     */
    public static Map<String, Object> getDocumentFieldValue(MGDocument document) throws IllegalAccessException, NoSuchFieldException {
        HashMap<String, Object> result = new HashMap<>();
        Class aClass = document.getClass();
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields){
            if (field.isAnnotationPresent(DocumentField.class)){
                if (!field.isAnnotationPresent(DocumentFieldPKDefault.class)){
                    field.setAccessible(true);
                    String docField = field.getAnnotationsByType(DocumentField.class)[0].value();
                    result.put(docField, field.get(document));
                }
            }
        }
        return result;
    }

    public static Field getDocFieldByDBField(MGDocument document, String DBFieldName) throws NoSuchFieldException{
        Field[] fields = document.getClass().getDeclaredFields();
        for (Field field : fields){
            if (field.isAnnotationPresent(DocumentField.class)){
                String annotationField = field.getAnnotation(DocumentField.class).value();
                if (DBFieldName.equals(annotationField)){
                    return field;
                }
            }
        }
        throw new NoSuchFieldException();
    }


    /**
     *
     * @param document instance of MGDocument
     * @return SET list primary key by annotation DocumentFieldPK
     */
    public static Set<String> getDocumentFieldPK(MGDocument document){
        Set<String> result = new HashSet<>();
        Class aClass = document.getClass();
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields){
            if (field.isAnnotationPresent(DocumentFieldPK.class) && field.isAnnotationPresent(DocumentField.class)){
                    String pk = field.getAnnotationsByType(DocumentField.class)[0].value();
                    result.add(pk);
            }
        }
        return result;
    }

    public static Set<String> getDocumentFieldUnique(MGDocument document){
        Set<String> result = new HashSet<>();
        Class aClass = document.getClass();
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields){
            if (field.isAnnotationPresent(DocumentFieldUnique.class) && field.isAnnotationPresent(DocumentField.class)){
                String pk = field.getAnnotationsByType(DocumentField.class)[0].value();
                result.add(pk);
            }
        }
        return result;
    }


    /**
     *
     * @param document instance of MGDocument
     * @param docField field in database ex: "_id", "user_name" define by annotation DocumentField
     * @return Object value of document by field name in database
     * @throws IllegalAccessException cannot access to field in class
     */
    public static Object getFieldValueByName(MGDocument document, String docField) throws IllegalAccessException {
        Class aClass = document.getClass();
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields){
            if (field.isAnnotationPresent(DocumentField.class)){
                String fieldInDB = field.getAnnotationsByType(DocumentField.class)[0].value();
                if (docField.equals(fieldInDB)){
                    field.setAccessible(true);
                    return field.get(document);
                }
            }
        }
        return null;
    }

    /**
     *
     * @param aInstance instance of MGModelAdapter
     * @return collection name define by CollectionClass annotation
     */
    public static String getCollectionName(MGModelAdapter aInstance){
        CollectionClass annotation = aInstance.getClass().getAnnotation(CollectionClass.class);
        return annotation.value();
    }

    public static Bson getPKCondition(MGDocument document) throws NotFoundPrimaryKey {
        Bson getCondition = null;
        for (String docField : MongoDB.getDocumentFieldPK(document)) {
            try {
                if (getCondition == null) {
                    getCondition = eq(docField, MongoDB.getFieldValueByName(document, docField));
                } else {
                    getCondition = and(getCondition, eq(docField, MongoDB.getFieldValueByName(document, docField)));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (getCondition == null) throw new NotFoundPrimaryKey("[getPKCondition in MongoDB] - Not found primary key");
        return getCondition;
    }

}
