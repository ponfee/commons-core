package code.ponfee.commons.model;

import java.beans.Transient;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.reflect.Fields;

/**
 * 返回结果数据结构体封装类
 * 
 * @see org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).build()
 * 
 * @param <T>
 * @author Ponfee
 */
public class Result<T> implements java.io.Serializable {

    private static final long serialVersionUID = -2804195259517755050L;
    public static final Result<Void> SUCCESS = new SuccessResult();

    private Integer code; // 状态码
    private String  msg;  // 返回信息
    private T       data; // 结果数据

    // -------------------------------------------constructor methods
    public Result() {} // code is null

    public Result(int code, String msg) {
        this(code, msg, null);
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result(ResultCode code, String msg) {
        this(code.getCode(), msg, null);
    }

    public Result(ResultCode code) {
        this(code.getCode(), code.getMsg(), null);
    }

    @SuppressWarnings("unchecked")
    public <E> Result<E> copy() {
        return copy((E) data);
    }

    public <E> Result<E> copy(E data) {
        return new Result<>(code, msg, data);
    }

    public <E> Result<E> transform(Function<T, E> transformer) {
        return new Result<>(code, msg, transformer.apply(data));
    }

    // ---------------------------------static methods/success methods
    public static Result<Void> success() {
        return SUCCESS;
    }

    public static <T> Result<T> success(T data) {
        return success("OK", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(SUCCESS.getCode(), msg, data);
    }

    // ---------------------------------static methods/failure methods
    public static <T> Result<T> failure(Enum<?> em) {
        return failure(
            (int) Fields.get(em, "code"), (String) Fields.get(em, "msg"), null
        );
    }

    public static <T> Result<T> failure(ResultCode code) {
        return failure(code.getCode(), code.getMsg(), null);
    }

    public static <T> Result<T> failure(ResultCode code, String msg) {
        return failure(code.getCode(), msg, null);
    }

    public static <T> Result<T> failure(int code, String msg) {
        return failure(code, msg, null);
    }

    public static <T> Result<T> failure(int code, String msg, T data) {
        Preconditions.checkState(
            code != SUCCESS.getCode(), "invalid failure code: " + code
        );
        return new Result<>(code, msg, data);
    }

    // ----------------------------------------------of operations
    public static Result<Void> of(boolean status, ResultCode resultCode) {
        return status ? SUCCESS : failure(resultCode);
    }

    public static <T> Result<T> of(boolean status, ResultCode resultCode, T data) {
        return status ? success(data) : failure(resultCode);
    }

    // -----------------------------------------------database update or delete affected rows
    public static Result<Void> assertAffectedOne(int actualAffectedRows) {
        return assertAffectedRows(actualAffectedRows, 1);
    }

    public static Result<Void> assertAffectedRows(int actualAffectedRows, int exceptAffectedRows) {
        return of(actualAffectedRows == exceptAffectedRows, ResultCode.OPS_CONFLICT);
    }

    // -------------------------------------------------getter/setter
    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(T data) {
        this.data = data;
    }

    public @Transient boolean isSuccess() {
        return ResultCode.isSuccessCode(code);
    }

    public @Transient boolean isFailure() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return this.toJson();
    }

    public String toJson() {
        return Jsons.NON_NULL.string(this);
    }

    /**
     * 成功结果
     */
    private static final class SuccessResult extends Result<Void> {
        private static final long serialVersionUID = 6740650053476768729L;

        SuccessResult() {
            super(ResultCode.OK);
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
        public void setData(Void void0) {
            throw new UnsupportedOperationException();
        }
    }

}
