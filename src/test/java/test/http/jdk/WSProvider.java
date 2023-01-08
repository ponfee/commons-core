package test.http.jdk;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import cn.ponfee.commons.ws.JAXWS;

@WebService(targetNamespace = "http://jdk6.webservice/demo", serviceName = "HelloService")
public class WSProvider {

    @WebResult(name = "Greetings") //自定义该方法返回值在WSDL中相关的描述  
    @WebMethod
    public String sayHi(@WebParam(name = "MyName") String name) {
        return "Hi," + name; //@WebParam是自定义参数name在WSDL中相关的描述  
    }

    @Oneway //表明该服务方法是单向的,既没有返回值,也不应该声明检查异常  
    @WebMethod(action = "printSystemTime", operationName = "printSystemTime") //自定义该方法在WSDL中相关的描述  
    public void printTime() {
        System.out.println(System.currentTimeMillis());
    }

    private static class WSPublisher implements Runnable {
        public void run() {
            //发布WSProvider到http://localhost:8889/demo/WSProvider这个地址,之前必须调用wsgen命令   
            //生成服务类WSProvider的支持类,命令如下:  
            //wsgen -cp . test.http.jdk.WSProvider  
            JAXWS.publish("http://localhost:8889/demo/WSProvider", new WSProvider());

            // 访问  http://localhost:8889/demo/WSProvider?wsdl
        }
    }

    public static void main(String[] args) {
        Thread wsPublisher = new Thread(new WSPublisher());
        wsPublisher.start();
    }

}
