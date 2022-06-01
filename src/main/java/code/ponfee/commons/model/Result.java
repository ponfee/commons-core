package code.ponfee.commons.model;

import com.google.common.base.Preconditions;

import java.beans.Transient;
import java.util.function.Function;

/**
 * Representing the result-data structure
 * 
 * @see org.springframework.http.ResponseEntity#status(org.springframework.http.HttpStatus)
 * 
 * @param <T> the data type
 * @author Ponfee
 */
public class Result<T> extends ToJsonString implements CodeMsg, java.io.Serializable {

    private static final long serialVersionUID = -2804195259517755050L;
    public static final Result<Void> SUCCESS = new Success();

    private Integer    code; // 状态码
    private boolean success; // 是否成功
    private String      msg; // 返回信息
    private T          data; // 结果数据

    // -------------------------------------------constructor methods
    public Result() {
        // code field is null, for help deserialized
    }

    public Result(CodeMsg cm) {
        this(cm.getCode(), cm.isSuccess(), cm.getMsg(), null);
    }

    public Result(CodeMsg cm, T data) {
        this(cm.getCode(), cm.isSuccess(), cm.getMsg(), data);
    }

    public Result(int code, boolean success) {
        this(code, success, null, null);
    }

    public Result(int code, boolean success, String msg) {
        this(code, success, msg, null);
    }

    public Result(int code, boolean success, String msg, T data) {
        this.code = code;
        this.success = success;
        this.msg = msg;
        this.data = data;
    }

    // -------------------------------------------others methods
    @SuppressWarnings("unchecked")
    public <E> Result<E> cast() {
        return (Result<E>) this;
    }

    public <E> Result<E> copy(E data) {
        return new Result<>(code, success, msg, data);
    }

    public <E> Result<E> map(Function<T, E> mapper) {
        return new Result<>(code, success, msg, mapper.apply(data));
    }

    // ---------------------------------static success methods
    /**
     * Returns success 200 code
     * 
     * @return Result success object
     */
    public static <T> Result<T> success() {
        return (Result<T>) SUCCESS;
    }

    /**
     * Returns success 200 code
     * 
     * @param data the data
     * @param <T>  the data type
     * @return Result success object
     */
    public static <T> Result<T> success(T data) {
        return success(Success.MSG, data);
    }

    /**
     * Returns success 200 code
     * 
     * @param msg  the msg
     * @param data the data
     * @param <T>  the data type
     * @return Result success object
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(Success.CODE, Success.STATE, msg, data);
    }

    /**
     * Returns success specified code
     * 
     * @param code the code
     * @param data the data
     * @param <T>  the data type
     * @return Result success object
     */
    public static <T> Result<T> success(int code, T data) {
        return new Result<>(code, Success.STATE, Success.MSG, data);
    }

    /**
     * Returns success specified code
     * 
     * @param code the code
     * @param msg  the msg
     * @param data the data
     * @param <T>  the data type
     * @return Result success object
     */
    public static <T> Result<T> success(int code, String msg, T data) {
        return new Result<>(code, Success.STATE, msg, data);
    }

    /**
     * Returns success specified CodeMsg
     * 
     * @param cm   the CodeMsg
     * @param data the data
     * @param <T>  the data type
     * @return Result success object
     */
    public static <T> Result<T> success(CodeMsg cm, T data) {
        Preconditions.checkState(cm.isSuccess(), "Invalid success state '" + cm.isSuccess() + "', code=" + cm.getCode());
        return new Result<>(cm.getCode(), cm.isSuccess(), cm.getMsg(), data);
    }

    // ---------------------------------static failure methods
    public static <T> Result<T> failure(CodeMsg cm) {
        return failure(cm, null);
    }

    public static <T> Result<T> failure(CodeMsg cm, T data) {
        Preconditions.checkState(!cm.isSuccess(), "Invalid failure state '" + cm.isSuccess() + "', code=" + cm.getCode());
        return new Result<>(cm.getCode(), cm.isSuccess(), cm.getMsg(), data);
    }

    public static <T> Result<T> failure(int code) {
        return failure(code, null, null);
    }

    public static <T> Result<T> failure(int code, String msg) {
        return failure(code, msg, null);
    }

    public static <T> Result<T> failure(int code, String msg, T data) {
        return new Result<>(code, false, msg, data);
    }

    // -----------------------------------------------of operations
    public static <T> Result<T> of(CodeMsg cm) {
        return new Result<>(cm);
    }

    public static <T> Result<T> of(CodeMsg cm, T data) {
        return new Result<>(cm, data);
    }

    public static <T> Result<T> of(int code, boolean success, String msg) {
        return new Result<>(code, success, msg);
    }

    public static <T> Result<T> of(int code, boolean success, String msg, T data) {
        return new Result<>(code, success, msg, data);
    }

    // -----------------------------------------------database update or delete affected rows
    public static <T> Result<T> assertAffectedOne(int actualAffectedRows) {
        return assertAffectedRows(actualAffectedRows, 1);
    }

    public static <T> Result<T> assertAffectedRows(int actualAffectedRows, int exceptAffectedRows) {
        return actualAffectedRows == exceptAffectedRows ? (Result<T>) SUCCESS : failure(ResultCode.OPS_CONFLICT);
    }

    public static <T> Result<T> assertOperatedState(boolean condition) {
        return condition ? (Result<T>) SUCCESS : failure(ResultCode.OPS_CONFLICT);
    }

    // -------------------------------------------------getter/setter
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
        return msg;
    }

    public T getData() {
        return data;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Transient
    public boolean isFailure() {
        return !success;
    }

    /**
     * Success result
     */
    private static final class Success extends Result<Void> {
        private static final long serialVersionUID = 6740650053476768729L;
        static final int CODE = 200;
        static final boolean STATE = true;
        static final String MSG = "OK";

        Success() {
            super(CODE, STATE, MSG);
        }

        @Override
        public void setCode(int code) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMsg(String msg) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setData(Void data) {
            throw new UnsupportedOperationException();
        }

        private Object readResolve() {
            return SUCCESS;
        }
    }

}
