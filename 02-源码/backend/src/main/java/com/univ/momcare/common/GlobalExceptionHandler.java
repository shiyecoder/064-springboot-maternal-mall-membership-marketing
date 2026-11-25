package com.univ.momcare.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理：把异常统一转成 {code,message} 结构，前端按 code 分支处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<Void> handleBiz(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public R<Void> handleValid(Exception e) {
        String msg;
        if (e instanceof MethodArgumentNotValidException me) {
            msg = me.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage).collect(Collectors.joining("；"));
        } else {
            msg = ((BindException) e).getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage).collect(Collectors.joining("；"));
        }
        return R.fail(4000, msg == null || msg.isEmpty() ? "参数校验失败" : msg);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleDenied(AccessDeniedException e) {
        return R.fail(4030, "无接口权限");
    }

    /** 静态资源缺失必须是 404，否则前端 img 会把错误 JSON 当图片导致破图难排查 */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<Void> handleNoResource(NoResourceFoundException e) {
        return R.fail(4040, "资源不存在");
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleOther(Exception e, HttpServletRequest req) {
        log.error("未捕获异常 {}", req.getRequestURI(), e);
        return R.fail(5000, "服务器异常：" + e.getMessage());
    }
}
