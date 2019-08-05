package test.utils;

import code.ponfee.commons.resource.ResourceLoaderFacade;
import code.ponfee.commons.xml.SimpleXmlHandler;

public class SimpleXmlHandlerTest {

    public static void main(String[] args) {
        System.out.println(SimpleXmlHandlerTest.class.getResourceAsStream("signer.xsd"));
        System.out.println(SimpleXmlHandlerTest.class.getClassLoader().getResourceAsStream("/signer.xsd"));
        System.out.println(ClassLoader.getSystemResourceAsStream("/signer.xsd"));
        System.out.println(Thread.currentThread().getContextClassLoader().getResourceAsStream("/signer.xsd"));

        System.out.println(SimpleXmlHandlerTest.class.getResourceAsStream("/signer.xsd"));
        System.out.println(SimpleXmlHandlerTest.class.getClassLoader().getResourceAsStream("signer.xsd"));
        System.out.println(ClassLoader.getSystemResourceAsStream("signer.xsd"));
        System.out.println(Thread.currentThread().getContextClassLoader().getResourceAsStream("signers.xml"));

        System.out.println(ResourceLoaderFacade.getResource("classpath:/signer.xsd"));
        System.out.println(ResourceLoaderFacade.getResource("classpath:/signers.xml"));

        SimpleXmlHandler.validateByXsd(Thread.currentThread().getContextClassLoader().getResourceAsStream("signer.xsd"), Thread.currentThread().getContextClassLoader().getResourceAsStream("signers.xml"));
    }
}
