package com.dating.util;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Map<String, Object> extraData;

    private Result() {
        this.extraData = new HashMap<>();
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public Result<T> put(String key, Object value) {
        this.extraData.put(key, value);
        return this;
    }

    // Getter for extraData if needed
    public Map<String, Object> getExtraData() {
        return extraData;
    }
}