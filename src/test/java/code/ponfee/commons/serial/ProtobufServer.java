package code.ponfee.commons.serial;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.serial.PersonProtobuf.Person;

public class ProtobufServer {

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSock = new ServerSocket(3030)) {
            while (true) {
                Socket sock = serverSock.accept();
                byte[] msg = new byte[256];
                sock.getInputStream().read(msg);
                int msgBodyLen = msg[0];
                System.out.println("msg body len:" + msgBodyLen);
                byte[] msgbody = new byte[msgBodyLen];
                System.arraycopy(msg, 1, msgbody, 0, msgBodyLen);

                Person person = Person.parseFrom(msgbody);

                System.out.println("toString:=============================");
                System.out.println(person);
                //System.out.println("toJson:=============================");
                //System.out.println(Jsons.toJson(person));
            }
        }
    }
}
