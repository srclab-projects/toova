package xyz.srclab.annotation.concurrent;

import javax.annotation.meta.TypeQualifier;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.*;

/**
 * Represents annotated method will return a {@link ThreadSafe} object.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@TypeQualifier
@TypeQualifierDefault({
        ElementType.TYPE,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.CONSTRUCTOR,
        ElementType.LOCAL_VARIABLE,
        ElementType.ANNOTATION_TYPE,
        ElementType.PACKAGE,
        ElementType.TYPE_PARAMETER,
        ElementType.TYPE_USE,
})
@Target({
        ElementType.TYPE,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.CONSTRUCTOR,
        ElementType.LOCAL_VARIABLE,
        ElementType.ANNOTATION_TYPE,
        ElementType.PACKAGE,
        ElementType.TYPE_PARAMETER,
        ElementType.TYPE_USE,
})
public @interface ReturnThreadSafe {

    ThreadSafeWhen when() default ThreadSafeWhen.TRUE;
}
