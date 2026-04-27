/**
  */
package com.example.careermarsaiproject.base;

import com.example.careermarsaiproject.enums.ResultEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;


/**
 * 接口返回对象实体
 *
 * @param <T>
 * @author huanglt
 */
@Slf4j
public final class Result<T> implements Serializable {


    private static final long serialVersionUID = 1L;

   @ApiModelProperty("状态码")
    private Integer code = ResultEnum.ERROR.getCode();


    @ApiModelProperty("提示信息")
    private String msg = null;


    @ApiModelProperty("数据")
    private T data = null;

    public Result() {
    }

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    private Result(int code) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public static <T> Result<T> error(String msg) {
        log.debug("返回错误：code={}, msg={}", ResultEnum.ERROR.getCode(), msg);
        return new Result<T>(ResultEnum.ERROR.getCode(), msg, null);
    }

    public static <T> Result<T> error(T data) {
        return new Result<T>(ResultEnum.ERROR.getCode(), "", data);
    }
    public static <T> Result<T> error(ResultEnum resultEnum) {
        log.debug("返回错误：code={}, msg={}", resultEnum.getCode(), resultEnum.getDesc());
        return new Result<T>(resultEnum.getCode(), resultEnum.getDesc(), null);
    }

    public static <T> Result<T> error(int code, String msg) {
        log.debug("返回错误：code={}, msg={}", code, msg);
        return new Result<T>(code, msg, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>(ResultEnum.SUCCESS.getCode(), "", data);
    }

    public static <T> Result<T> success() {
        return new Result<T>(ResultEnum.SUCCESS.getCode());
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result [code=" + code + ", msg=" + msg + ", data=" + data + "]";
    }

}
