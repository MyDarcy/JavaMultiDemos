package com.darcy.demo.aop;

import org.springframework.stereotype.Component;

@Component
public class HelloWorldImpl1 implements HelloWorld {
  public void printHelloWorld() {
    System.out.println("Enter HelloWorldImpl1.printHelloWorld()");
  }

  public void doPrint() {
    System.out.println("Enter HelloWorldImpl1.doPrint()");
    return ;
  }
}
