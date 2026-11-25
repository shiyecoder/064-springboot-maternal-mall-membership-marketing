package com.univ.momcare.common;

import lombok.Getter;

/**
 * 业务异常。code 对应需求文档 7.6 的错误码表：
 * 4000 参数校验 4001 状态不允许 4003 数据范围 4009 重复操作 4010 登录失效
 * 4030 无权限 4090 版本冲突 5000 服务器异常
 */
@Getter
public class BizException extends RuntimeException {
    private final int code;

    public BizException(String message) {
        this(4000, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static BizException state(String msg) { return new BizException(4001, msg); }
    public static BizException scope(String msg) { return new BizException(4003, msg); }
    public static BizException dup(String msg) { return new BizException(4009, msg); }
    public static BizException version(String msg) { return new BizException(4090, msg); }
}
