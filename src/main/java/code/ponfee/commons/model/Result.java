package code.ponfee.commons.model;

import java.beans.Transient;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import code.ponfee.commons.json.Jsons;

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

    public Result(CodeMsg cm, String msg) {
        this(cm.getCode(), msg, null);
    }

    public Result(CodeMsg cm) {
        this(cm.getCode(), cm.getMsg(), null);
    }

    @SuppressWarnings("unchecked")
    public <E> Result<E> copy() {
        return copy((E) data);
    }

    public <E> Result<E> copy(E data) {
        return new Result<>(code, msg, data);
    }

    public <E> Result<E> map(Function<T, E> mapper) {
        return new Result<>(code, msg, mapper.apply(data));
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

    public static <T> Result<T> success(CodeMsg cm, T data) {
        return success(cm.getCode(), cm.getMsg(), data);
    }

    public static <T> Result<T> success(int code, String msg, T data) {
        Preconditions.checkState(
            ResultCode.isSuccessCode(code), "Invalid success code: " + code
        );
        return new Result<>(code, msg, data);
    }

    // ---------------------------------static methods/failure methods
    public static <T> Result<T> failure(CodeMsg cm) {
        return failure(cm.getCode(), cm.getMsg(), null);
    }

    public static <T> Result<T> failure(CodeMsg cm, String msg) {
        return failure(cm.getCode(), msg, null);
    }

    public static <T> Result<T> failure(int code, String msg) {
        return failure(code, msg, null);
    }

    public static <T> Result<T> failure(int code, String msg, T data) {
        Preconditions.checkState(
            !ResultCode.isSuccessCode(code), "Invalid failure code: " + code
        );
        return new Result<>(code, msg, data);
    }

    // -----------------------------------------------of operations
    public static Result<Void> of(CodeMsg cm) {
        return new Result<>(cm);
    }

    public static <T> Result<T> of(CodeMsg cm, T data) {
        return new Result<>(cm.getCode(), cm.getMsg(), data);
    }

    public static <T> Result<T> of(int code, String msg) {
        return new Result<>(code, msg);
    }

    public static <T> Result<T> of(int code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    // -----------------------------------------------database update or delete affected rows
    public static Result<Void> assertAffectedOne(int actualAffectedRows) {
        return assertAffectedRows(actualAffectedRows, 1);
    }

    public static Result<Void> assertAffectedRows(int actualAffectedRows, int exceptAffectedRows) {
        return actualAffectedRows == exceptAffectedRows ? SUCCESS : failure(ResultCode.OPS_CONFLICT);
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

    @Transient
    public boolean isSuccess() {
        return ResultCode.isSuccessCode(code);
    }

    @Transient
    public boolean isFailure() {
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
