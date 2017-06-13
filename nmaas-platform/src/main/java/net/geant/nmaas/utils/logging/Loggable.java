package net.geant.nmaas.utils.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate that {@link LoggingAspect} should be applied for given method.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Loggable {

    LogLevel value();

}
