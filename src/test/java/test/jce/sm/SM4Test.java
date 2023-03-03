package test.jce.sm;

import java.util.Base64;

import cn.ponfee.commons.jce.sm.SM4;
import cn.ponfee.commons.util.MavenProjects;

public class SM4Test {

    public static void main(String[] args) {
        //byte[] data = Files.toString(MavenProjects.getMainJavaFile(SM4.class)).replaceAll("\r|\n", "").getBytes();
        byte[] data = MavenProjects.getMainJavaFileAsString(SM4.class).substring(0, 997).getBytes();
        byte[] key = "1234567785465466".getBytes();
        byte[] iv = "1a345677b546d4de".getBytes();
        System.out.println(new String(SM4.decrypt( key, iv, Base64.getDecoder().decode("+31e6VuKcGDl4qG5rxfiYy35LFwmbS4VY4AF/t7lmeu2wjEUneKEVWTEPBnaSo3+lRKsqfBVp4khbD830Qiy8R66AdHm1/ato7OzepfCxAs=")))+"|");
        System.out.println(new String(SM4.decrypt( key, iv, Base64.getDecoder().decode("A5/GbdIvh2v44y8izoqzhu6ne96tkt/xI0ZBkFaPyX+rD6G/+MuyARV4lawjH/Cy6N+vVRYTwb6T5l4dMTJ7mEN1X0XiYxlrbX3PDGd96OM=")))+"|");

        byte[] encrypted = SM4.encrypt(key, data);
        System.out.println(data.length + "-->" + encrypted.length + "\t|" + new String(SM4.decrypt(key, encrypted)) + "|");

        encrypted = SM4.encrypt(key, iv, data);
        System.out.println(Base64.getEncoder().encodeToString(encrypted));
        System.out.println(data.length + "-->" + encrypted.length + "\t|" + new String(SM4.decrypt(key, iv, encrypted)) + "|");

        encrypted = SM4.encrypt(false, key, data);
        System.out.println(data.length + "-->" + encrypted.length + "\t|" + new String(SM4.decrypt(false, key, encrypted)) + "|");

        encrypted = SM4.encrypt(false, key, iv, data);
        System.out.println(data.length + "-->" + encrypted.length + "\t|" + new String(SM4.decrypt(false, key, iv, encrypted)) + "|");
    }
}
