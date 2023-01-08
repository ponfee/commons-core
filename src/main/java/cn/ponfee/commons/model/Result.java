/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.model;

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

    // -----------------------------------------------constructor methods

    public Result() {
        // No operation: retain no-arg constructor for help deserialization
    }

    public Result(int code, boolean success, String msg, T data) {
        this.code = code;
        this.success = success;
        this.msg = msg;
        this.data = data;
    }

    // -----------------------------------------------others methods

    @SuppressWarnings("unchecked")
    public <E> Result<E> cast() {
        return (Result<E>) this;
    }

    public <E> Result<E> from(E data) {
        return new Result<>(code, success, msg, data);
    }

    public <E> Result<E> convert(Function<T, E> mapper) {
        return new Result<>(code, success, msg, mapper.apply(data));
    }

    // -----------------------------------------------static success methods

    /**
     * Returns success 200 code
     * 
     * @return Result success object
     */
    public static Result<Void> success() {
        return SUCCESS;
    }

    /**
     * Returns success 200 code
     * 
     * @param data the data
     * @param <T>  the data type
     * @return Result success object
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(Success.CODE, true, Success.MSG, data);
    }

    // -----------------------------------------------static failure methods

    public static <T> Result<T> failure(CodeMsg cm) {
        if (cm.isSuccess()) {
            throw new IllegalStateException("Failure state must be 'false'.");
        }
        return new Result<>(cm.getCode(), cm.isSuccess(), cm.getMsg(), null);
    }

    public static <T> Result<T> failure(int code) {
        return new Result<>(code, false, null, null);
    }

    public static <T> Result<T> failure(int code, String msg) {
        return new Result<>(code, false, msg, null);
    }

    // -----------------------------------------------of operations

    public static <T> Result<T> of(CodeMsg cm) {
        return new Result<>(cm.getCode(), cm.isSuccess(), cm.getMsg(), null);
    }

    public static <T> Result<T> of(CodeMsg cm, T data) {
        return new Result<>(cm.getCode(), cm.isSuccess(), cm.getMsg(), data);
    }

    public static <T> Result<T> of(int code, boolean success, String msg) {
        return new Result<>(code, success, msg, null);
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

    public static <T> Result<T> assertOperatedState(boolean state) {
        return state ? (Result<T>) SUCCESS : failure(ResultCode.OPS_CONFLICT);
    }

    // -----------------------------------------------getter/setter

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Transient
    public boolean isFailure() {
        return !success;
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

    // -----------------------------------------------static class

    /**
     * Success result
     */
    private static final class Success extends Result<Void> {
        private static final long serialVersionUID = 6740650053476768729L;
        private static final int CODE = ResultCode.OK.getCode();
        private static final String MSG = "OK";

        private Success() {
            super(CODE, true, MSG, null);
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
