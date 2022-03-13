package code.ponfee.commons.model;

/**
 * The code and message for {@link Result}
 *
 * @author Ponfee
 */
public interface CodeMsg {

    int getCode();

    boolean isSuccess();

    String getMsg();

    /**
     * <p>中止当前运行的Java虚拟机，返回值给调用方(如bash)</p>
     * <p>0正常退出；非0异常退出；</p>
     * 
     * @see System#exit(int)
     */
    enum SystemExit implements CodeMsg {

        SUCCESS(0),
        FAILURE(1),
        ;

        private final int code;
        private final boolean success;

        SystemExit(int code) {
            this.code = code;
            this.success = code == 0;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public boolean isSuccess() {
            return success;
        }

        @Override
        public String getMsg() {
            return super.name();
        }
    }

}
