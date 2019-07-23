package com.darcy.demo.aop;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * https://www.cnblogs.com/xrq730/p/4919025.html
 *
 * AOP 将那些影响了多个类的公共行为封装到一个可重用模块，并将其命名为"Aspect"，即切面。所谓"切面"，简单说就是那些与业务无关，
 * 却为业务模块所共同调用的逻辑或责任封装起来，便于减少系统的重复代码，降低模块之间的耦合度，并有利于未来的可操作性和可维护性。
 *
 * AOP把软件系统分为两个部分：核心关注点和横切关注点。业务处理的主要流程是核心关注点，与之关系不大的部分是横切关注点。
 * 横切关注点的一个特点是，他们经常发生在核心关注点的多处，而各处基本相似，比如权限认证、日志、事物。
 * AOP的作用在于分离系统中的各种关注点，将核心关注点和横切关注点分离开来。
 *
 * 1、横切关注点
 * 对哪些方法进行拦截，拦截后怎么处理，这些关注点称之为横切关注点
 *
 * 2、切面（aspect）
 * 类是对物体特征的抽象，切面就是对横切关注点的抽象
 *
 * 3、连接点（joinpoint）
 * 被拦截到的点，因为Spring只支持方法类型的连接点，所以在Spring中连接点指的就是被拦截到的方法，实际上连接点还可以是字段或者构造器
 *
 * 4、切入点（pointcut）
 * 对连接点进行拦截的定义
 *
 * 5、通知（advice）
 * 所谓通知指的就是指拦截到连接点之后要执行的代码，通知分为前置、后置、异常、最终、环绕通知五类
 *
 * 6、目标对象
 * 代理的目标对象
 *
 * 7、织入（weave）
 * 将切面应用到目标对象并导致代理对象创建的过程
 *
 * 8、引入（introduction）
 * 在不修改代码的前提下，引入可以在运行期为类动态地添加一些方法或字段
 *
 *
 */
public class Main1 {

  public static void main(String[] args) {
    ApplicationContext ctx =
        new ClassPathXmlApplicationContext("aop.xml");

    HelloWorld hw1 = (HelloWorld)ctx.getBean("helloWorldImpl1");
    HelloWorld hw2 = (HelloWorld)ctx.getBean("helloWorldImpl2");
    hw1.printHelloWorld();
    System.out.println();
    hw1.doPrint();

    System.out.println();
    hw2.printHelloWorld();
    System.out.println();
    hw2.doPrint();
  }

}
/* 1. 简单测试 对应aop.xml中的配置
CurrentTime = 1563508948357
Enter HelloWorldImpl1.printHelloWorld()
CurrentTime = 1563508948357

CurrentTime = 1563508948358
Enter HelloWorldImpl1.doPrint()
CurrentTime = 1563508948358

CurrentTime = 1563508948358
Enter HelloWorldImpl2.printHelloWorld()
CurrentTime = 1563508948358

CurrentTime = 1563508948358
Enter HelloWorldImpl2.doPrint()
CurrentTime = 1563508948358
 */

/* 2. around通知;
CurrentTime = 1563849805889
Enter HelloWorldImpl1.printHelloWorld()
CurrentTime = 1563849805889

CurrentTime = 1563849805889
Enter HelloWorldImpl1.doPrint()
CurrentTime = 1563849805889

CurrentTime = 1563849805889
Enter HelloWorldImpl2.printHelloWorld()
CurrentTime = 1563849805890

CurrentTime = 1563849805890
Enter HelloWorldImpl2.doPrint()
CurrentTime = 1563849805890

 */

/* 3. 多个切面
CurrentTime = 1563509370935
Log before method
Enter HelloWorldImpl1.printHelloWorld()
Log after method
CurrentTime = 1563509370936

CurrentTime = 1563509370937
Log before method
Enter HelloWorldImpl1.doPrint()
Log after method
CurrentTime = 1563509370937

CurrentTime = 1563509370938
Log before method
Enter HelloWorldImpl2.printHelloWorld()
Log after method
CurrentTime = 1563509370938

CurrentTime = 1563509370939
Log before method
Enter HelloWorldImpl2.doPrint()
Log after method
CurrentTime = 1563509370939
 */

/* 4. 只想织入接口中的某些方法
CurrentTime = 1563512721478
Enter HelloWorldImpl1.printHelloWorld()
CurrentTime = 1563512721478

Log before method
Enter HelloWorldImpl1.doPrint()
Log after method

CurrentTime = 1563512721478
Enter HelloWorldImpl2.printHelloWorld()
CurrentTime = 1563512721479

Log before method
Enter HelloWorldImpl2.doPrint()
Log after method
 */