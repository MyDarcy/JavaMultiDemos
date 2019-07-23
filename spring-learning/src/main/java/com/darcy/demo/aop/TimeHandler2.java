package com.darcy.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class TimeHandler2 {
  public void printTime(ProceedingJoinPoint joinPoint) throws Throwable {
    System.out.println("CurrentTime = " + System.currentTimeMillis());
    joinPoint.proceed();
    System.out.println("CurrentTime = " + System.currentTimeMillis());
  }
}