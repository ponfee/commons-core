package code.ponfee.commons.model;

import java.io.Serializable;

/**
 * It a code and message for result
 * 
 * @author Ponfee
 */
public interface CodeMsg extends Serializable {

    int getCode();

    String getMsg();

    static enum CodeMsgEnum implements CodeMsg {
        EXIT(0, "EXIT");

        private final int code;
        private final String msg;

        CodeMsgEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getMsg() {
            return msg;
        }
    }

}
