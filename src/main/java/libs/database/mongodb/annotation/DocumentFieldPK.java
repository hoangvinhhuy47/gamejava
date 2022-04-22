package libs.database.mongodb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>require <i>DocumentField</i> annotation to define this field delegate for which field in database before use this annotation</b>
 * @author Cuong Nguyen Cao
 * @since 1.0
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DocumentFieldPK {
}
