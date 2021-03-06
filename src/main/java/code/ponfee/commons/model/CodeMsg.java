package code.ponfee.commons.model;

import java.io.Serializable;

/**
 * The code and message for {@link Result}
 * 
 * @author Ponfee
 */
public interface CodeMsg extends Serializable {

    int getCode();

    String getMsg();

    enum SystemExit implements CodeMsg {
        EXIT(0, "EXIT");

        private final int code;
        private final String msg;

        SystemExit(int code, String msg) {
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
