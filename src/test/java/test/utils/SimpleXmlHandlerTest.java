package test.utils;

import code.ponfee.commons.resource.ResourceLoaderFacade;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.xml.SimpleXmlHandler;
import org.apache.commons.io.IOUtils;

import java.io.FileReader;
import java.io.IOException;

public class SimpleXmlHandlerTest {

    public static void main(String[] args) throws IOException {
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

        System.out.println("----validate");
        SimpleXmlHandler.validate(
            IOUtils.toString(new FileReader(MavenProjects.getTestResourcesPath("signers.xml"))),
            IOUtils.toString(new FileReader(MavenProjects.getTestResourcesPath("signer.xsd")))
        );
        System.out.println("----validate done");
    }
}
