package com.morgan.test.code.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * 关于代理类的测试，其中CGLib动态代理需要用到cglib-nodep的jar包
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-01-05
 */
public class ProxyTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        testStaticProxy();
        testJDKProxy();
        testCGLibProxy();
    }

    /**
     * 静态代理测试
     */
    private static void testStaticProxy() {
        Request req = new HttpRequest();
        RequestProxy proxy = new RequestProxy();
        proxy.buildProxy(req);
        proxy.request();
    }

    /**
     * CGLib动态代理测试
     */
    private static void testCGLibProxy() {
        Student stu = new Student();
        CGLibProxy proxy = new CGLibProxy();
        Student stuProxy = (Student) proxy.createProxy(stu);
        stuProxy.doSomeThing();
    }

    /**
     * JDK动态代理测试
     */
    private static void testJDKProxy() {
        Request req = new HttpRequest();
        JDKProxy proxy = new JDKProxy();
        Request reqProxy = (Request) proxy.createProxy(req);
        reqProxy.request();
    }

}

/**
 * 被代理对象接口
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-01-05
 */
interface Request {

    public void request();
}

/**
 * 被代理对象类
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-01-05
 */
class HttpRequest implements Request {

    @Override
    public void request() {
        System.out.println("http request......");
    }
}

/**
 * 委托类(静态代理)
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-01-05
 */
class RequestProxy implements Request {

    // 被代理的类
    private Request req;

    public void buildProxy(Request req) {
        this.req = req;
    }

    @Override
    public void request() {
        // 调用之前
        doBefore();
        req.request();
        // 调用之后
        doAfter();
    }

    /**
     * 方法调用后触发
     */
    private void doAfter() {
        System.out.println("after method invoke");
    }

    /**
     * 方法调用前触发
     */
    private void doBefore() {
        System.out.println("before method invoke");
    }
}

/**
 * 动态代理类
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-01-05
 */
class JDKProxy implements InvocationHandler {

    private Object target;

    /**
     * 创建代理，需要把返回值强制转换为被代理的对象
     * 
     * @param target
     *            要代理的对象
     * @return 代理
     */
    public Object createProxy(Object target) {
        this.target = target;
        return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
    }

    /**
     * @param proxy
     *            被代理的类
     * @param method
     *            要调用的方法
     * @param args
     *            方法调用时所需要的参数 可以将InvocationHandler接口的子类想象成一个代理的最终操作类，替换掉ProxySubject
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object retVal = null;
        // 调用之前
        doBefore();
        retVal = method.invoke(target, args);
        // 调用之后
        doAfter();
        return retVal;
    }

    /**
     * 方法调用后触发
     */
    private void doAfter() {
        System.out.println("after method invoke");
    }

    /**
     * 方法调用前触发
     */
    private void doBefore() {
        System.out.println("before method invoke");
    }
}

/**
 * 被代理对象
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-01-05
 */
class Student {

    public void doSomeThing() {
        System.out.println("good good study,day day up...");
    }
}

/**
 * 通过CGLib实现动态代理
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-01-05
 */
class CGLibProxy implements MethodInterceptor {

    // 要代理的原始对象
    private Object obj;
    private Enhancer enhancer = new Enhancer();

    /**
     * 创建代理，需要把返回值强制转换为被代理的对象
     * 
     * @param target
     *            要代理的对象
     * @return 代理
     */
    public Object createProxy(Object target) {
        this.obj = target;
        enhancer.setSuperclass(this.obj.getClass());// 设置代理目标
        enhancer.setCallback(this);// 设置回调
        enhancer.setClassLoader(target.getClass().getClassLoader());
        return enhancer.create();
    }

    /**
     * 在代理实例上处理方法调用并返回结果
     * 
     * @param proxy
     *            代理类
     * @param method
     *            被代理的方法
     * @param params
     *            该方法的参数数组
     * @param methodProxy
     */
    @Override
    public Object intercept(Object proxy, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        Object result = null;
        // 调用之前
        doBefore();
        // 调用原始对象的方法
        result = methodProxy.invokeSuper(proxy, params);
        // 调用之后
        doAfter();
        return result;
    }

    /**
     * 方法调用后触发
     */
    private void doAfter() {
        System.out.println("after method invoke");
    }

    /**
     * 方法调用前触发
     */
    private void doBefore() {
        System.out.println("before method invoke");
    }

}
