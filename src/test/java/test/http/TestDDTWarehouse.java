package test.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import code.ponfee.commons.http.ContentType;
import code.ponfee.commons.http.Http;
import code.ponfee.commons.io.Files;

@SuppressWarnings("unchecked")
public class TestDDTWarehouse {

    @Test
    public void test0() {
        List<Object> resp = Http.post("http://10.118.58.74:8080/battleRoom/shouSkuInfo2")
                                .accept(ContentType.APPLICATION_JSON) // @ResponseBody
                                .request(List.class);
        System.out.println(resp);
    }
    
    @Test
    public void test1() {
        Map<String, Object> resp = Http.post("http://10.118.58.74:8080/battleRoom/importsku")
                                       .addPart("skuFile", "importsku.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new File("d:/大屏批量配置-data-2.xlsx"))
                                       .contentType(ContentType.MULTIPART_FORM_DATA, "UTF-8") // <input type="file" name="upload" />
                                       .accept(ContentType.APPLICATION_JSON) // @ResponseBody
                                       .request(Map.class);
        System.out.println(resp);
    }

    @Test
    public void test2() {
        Http.post("http://10.118.58.74:8080/battleRoom/exportsku").addParam("fileId","ad").download("d:/abc2dd.xlsx");
    }

    public static void main(String[] args) throws FileNotFoundException {
        InputStream in = new FileInputStream("d:/abc2dd.csv");
        try (InputStream input = in;
            BufferedInputStream buffInput = new BufferedInputStream(input); 
            OutputStream output = new FileOutputStream("D:/1111.csv"); 
            BufferedOutputStream buffOutput = new BufferedOutputStream(output)
        ) {
           byte[] buffer = new byte[8192];
           for (int len; (len = buffInput.read(buffer)) != Files.EOF;) {
               buffOutput.write(buffer, 0, len);
           }
       } catch (IOException e) {
       }
    }
}
