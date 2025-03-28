package net.geant.nmaas.utils.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

/**
 * Aspect responsible for logging information when entering and leaving methods annotated with {@link Loggable}.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final String BEFORE_STRING = "> {0}";
    private static final String BEFORE_WITH_PARAMS_STRING = "> {0} PARAMS {1}";
    private static final String AFTER_RETURNING = "< {0} AND RETURNING {1}";
    private static final String AFTER_RETURNING_VOID = "< {0}";

    public Level loggableToLevel(Loggable loggable) {
        if (loggable != null) {
            switch (loggable.value()) {
                case DEBUG:
                    return Level.DEBUG;
                case ERROR:
                    return Level.ERROR;
                case INFO:
                    return Level.INFO;
                case TRACE:
                    return Level.TRACE;
                case WARN:
                    return Level.WARN;
            }
        }
        return Level.INFO;
    }

    @Before(
            value = "@annotation(trace)",
            argNames = "joinPoint, trace")
    public void before(JoinPoint joinPoint, Loggable loggable) {
        Class<?> clazz = joinPoint.getTarget().getClass();
        Logger logger = LoggerFactory.getLogger(clazz);
        Level level = loggableToLevel(loggable);
        String name = joinPoint.getSignature().getName();

        if (joinPoint.getArgs() != null && joinPoint.getArgs().length == 0) {
            logger.atLevel(level).log(MessageFormat.format(BEFORE_STRING, name));
        } else {
            logger.atLevel(level).log(MessageFormat.format(BEFORE_WITH_PARAMS_STRING, name, constructArgumentsString(joinPoint.getArgs())));
        }
    }

    @AfterReturning(
            value = "@annotation(trace)",
            returning = "returnValue",
            argNames = "joinPoint, trace, returnValue")
    public void afterReturning(JoinPoint joinPoint, Loggable loggable, Object returnValue) {
        Class<?> clazz = joinPoint.getTarget().getClass();
        Logger logger = LoggerFactory.getLogger(clazz);
        String name = joinPoint.getSignature().getName();
        Level level = loggableToLevel(loggable);
        if (joinPoint.getSignature() instanceof MethodSignature signature) {
            Class<?> returnType = signature.getReturnType();
            if (returnType.getName().compareTo("void") == 0) {
                logger.atLevel(level).log(MessageFormat.format(AFTER_RETURNING_VOID, name));
                return;
            }
        }
        logger.atLevel(level).log(MessageFormat.format(AFTER_RETURNING, name, constructArgumentsString(returnValue)));
    }

    private String constructArgumentsString(Object... arguments) {
        StringBuilder buffer = new StringBuilder();
        for (Object object : arguments) {
            buffer.append(object).append(" ");
        }
        return buffer.toString();
    }

}