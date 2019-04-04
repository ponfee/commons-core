package code.ponfee.commons.serial;

import java.io.IOException;
import java.net.Socket;

import code.ponfee.commons.serial.PersonProtobuf.Addr;
import code.ponfee.commons.serial.PersonProtobuf.Person;
import code.ponfee.commons.serial.PersonProtobuf.Phone;
import code.ponfee.commons.serial.PersonProtobuf.PhoneType;

/**
 * 
 * @author 
 */
public class ProtobufClient {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 3030);


        Person person = Person.newBuilder()
            .setId(1).setAge(12).setName("ccf")
            .setAddr(Addr.newBuilder().setContry("china").setCity("shenzhen").build())
            .addPhone(Phone.newBuilder().setNumber("13418467597").setType(PhoneType.MOBILE).build())
            .addPhone(Phone.newBuilder().setNumber("0755-41245647").setType(PhoneType.HOME).build())
            .build();
        
        byte[] messageBody = person.toByteArray();

        int headerLen = 1;
        byte[] message = new byte[headerLen + messageBody.length];
        message[0] = (byte) messageBody.length;
        System.arraycopy(messageBody, 0, message, 1, messageBody.length);
        System.out.println("msg len:" + message.length);
        socket.getOutputStream().write(message);
        socket.close();
    }

}
