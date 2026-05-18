package com.livestock.config;

import com.livestock.common.Result;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(DuplicateKeyException e) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("uk_site_code")) {
            return Result.error("场地编号已存在");
        }
        if (msg != null && msg.contains("uk_username")) {
            return Result.error("用户名已存在");
        }
        if (msg != null && msg.contains("uk_batch_no")) {
            return Result.error("批次编号已存在");
        }
        return Result.error("数据重复，请检查输入");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrity(DataIntegrityViolationException e) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("doesn't have a default value")) {
            return Result.error("请填写完整信息，必要字段不能为空");
        }
        return Result.error("数据保存失败，请检查输入");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return Result.error("请求数据格式错误，请检查输入");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        e.printStackTrace();
        String msg = e.getMessage();
        if (msg == null) {
            msg = e.getClass().getSimpleName();
        }
        return Result.error("服务器内部错误：" + msg);
    }
}
