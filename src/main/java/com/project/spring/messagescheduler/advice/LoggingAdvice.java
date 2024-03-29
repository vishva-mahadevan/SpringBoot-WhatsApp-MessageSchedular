package com.project.spring.messagescheduler.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAdvice {
    private Logger logger= LoggerFactory.getLogger(this.getClass());

    /**
     * Pointcut matches all the service , rest and service
     */
    @Pointcut("within(@org.springframework.stereotype.Service *) || within(@org.springframework.stereotype.Repository *) || within(@org.springframework.web.bind.annotation.RestController *)")
    public void springPointCut(){

    }

    /**
     * Pointcut matches all the application package
     */
    @Pointcut("within(com.project.spring.messagescheduler.service..*) " +
            "|| within(com.project.spring.messagescheduler.controller..*) " +
            "|| within(com.project.spring.messagescheduler.repository..*) " +
            "|| within(com.project.spring.messagescheduler.utils..*)")
    public void applicationPointcut(){

    }

    /**
     * Application Logger helps to manage the logging of all the methods in the application.
     * Before Entering method: what all the arguments passed to which class
     * After Execution: logs the response
     * If Error: throws an Exception
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("springPointCut() && applicationPointcut()")
    public Object applicationLogger(ProceedingJoinPoint joinPoint) throws Throwable {
        if(logger.isDebugEnabled()){
            logger.debug("Enter:{}.{}() with arguments {}",joinPoint.getSignature().getDeclaringTypeName(),joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
        }
        Object result;
        try {
            result=joinPoint.proceed();
            if(logger.isDebugEnabled()){
                logger.debug("Exit:{}.{}() with result {}",joinPoint.getSignature().getDeclaringTypeName(),joinPoint.getSignature().getName(), result.toString());
            }
        }catch (IllegalArgumentException e) {
            logger.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            throw e;
        }
        catch (NullPointerException e){
//            e.printStackTrace();
            throw e;
        }
        return result;

    }

    @AfterThrowing(pointcut = "springPointCut() && applicationPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        logger.error("Exception in {}.{}() with cause = {}", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(), e.getCause() != null ? e.getCause() : "Object returns null");
    }
}
