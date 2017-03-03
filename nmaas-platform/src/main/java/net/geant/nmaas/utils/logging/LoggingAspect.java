package net.geant.nmaas.utils.logging;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Aspect
@Component
public class LoggingAspect {

	private static String BEFORE_STRING = "Entering < {0} >";
	
	private static String BEFORE_WITH_PARAMS_STRING = "Entering < {0} > with params {1}";

	private static String AFTER_THROWING = "Exception thrown < {0} > exception message {1} with params {2}";

	private static String AFTER_RETURNING = "Leaving < {0} > returning {1}";

	private static String AFTER_RETURNING_VOID = "Leaving < {0} >";
	
	public Level loggableToLevel(Loggable loggable){
		if (loggable != null)
		switch (loggable.value()){
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
			logger.log(level, MessageFormat.format(BEFORE_STRING, name));
		} else {
			logger.log(level, MessageFormat.format(BEFORE_WITH_PARAMS_STRING, name, constructArgumentsString(joinPoint.getArgs())));
		}
	}

	@AfterThrowing(
			value = "@annotation(net.geant.nmaas.utils.logging.Loggable)",
			throwing = "throwable",
			argNames = "joinPoint, throwable")
	public void afterThrowing(JoinPoint joinPoint, Throwable throwable) {
		Class<? extends Object> clazz = joinPoint.getTarget().getClass();
		Logger logger = LogManager.getLogger(clazz);
		String name = joinPoint.getSignature().getName();
		logger.log(Level.ERROR, MessageFormat.format(
													AFTER_THROWING,
													name,
													throwable.getMessage(),
													constructArgumentsString(clazz, joinPoint.getArgs())));
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
				logger.log(level, MessageFormat.format(AFTER_RETURNING_VOID,name));
				return;
			}
		}
		logger.log(level, MessageFormat.format(AFTER_RETURNING, name, constructArgumentsString(returnValue)));
	}

	private String constructArgumentsString(Object... arguments) {
		StringBuffer buffer = new StringBuffer();
		for (Object object : arguments) {
			buffer.append(object);
		}
		return buffer.toString();
	}
}
