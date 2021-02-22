package net.geant.nmaas.utils.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

/**
 * Aspect responsible for logging information when entering and leaving methods annotated with {@link Loggable}.
 */
@Aspect
@Component
public class LoggingAspect {

	private static String beforeString = "> {0}";
	
	private static String beforeWithParamsString = "> {0} PARAMS {1}";

	private static String afterReturning = "< {0} AND RETURNING {1}";

	private static String afterReturningVoid = "< {0}";
	
	public Level loggableToLevel(Loggable loggable){
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
				case OFF:
					return Level.OFF;
				case ALL:
					return Level.ALL;
				case FATAL:
					return Level.FATAL;
			}
		}
		return Level.INFO;
	}

	@Before(
			value = "@annotation(trace)",
			argNames = "joinPoint, trace")
	public void before(JoinPoint joinPoint, Loggable loggable) {
		Class<? extends Object> clazz = joinPoint.getTarget().getClass();
		Logger logger = LogManager.getLogger(clazz);
		Level level = loggableToLevel(loggable);
		String name = joinPoint.getSignature().getName();
		
		if (joinPoint.getArgs() != null && joinPoint.getArgs().length == 0) {
			logger.log(level, MessageFormat.format(beforeString, name));
		} else {
			logger.log(level, MessageFormat.format(beforeWithParamsString, name, constructArgumentsString(joinPoint.getArgs())));
		}
	}

	@AfterReturning(
			value = "@annotation(trace)",
			returning = "returnValue",
			argNames = "joinPoint, trace, returnValue")
	public void afterReturning(JoinPoint joinPoint, Loggable loggable, Object returnValue) {
		Class<? extends Object> clazz = joinPoint.getTarget().getClass();
		Logger logger = LogManager.getLogger(clazz);
		String name = joinPoint.getSignature().getName();
		Level level = loggableToLevel(loggable);
		if (joinPoint.getSignature() instanceof MethodSignature) {
			MethodSignature signature = (MethodSignature) joinPoint
					.getSignature();
			Class<?> returnType = signature.getReturnType();
			if (returnType.getName().compareTo("void") == 0) {
				logger.log(level, MessageFormat.format(afterReturningVoid,name));
				return;
			}
		}
		logger.log(level, MessageFormat.format(afterReturning, name, constructArgumentsString(returnValue)));
	}

	private String constructArgumentsString(Object... arguments) {
		StringBuilder buffer = new StringBuilder();
		for (Object object : arguments) {
			buffer.append(object).append(" ");
		}
		return buffer.toString();
	}
}
