package com.univ.momcare.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.univ.momcare.security.CurrentUser;
import com.univ.momcare.security.LoginUser;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 标准字段自动填充：create_time / update_time / create_by / update_by / deleted / version。
 * 业务代码不需要手写这些字段，避免各控制器口径不一。
 */
@Component
public class MetaHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject meta) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(meta, "createTime", LocalDateTime.class, now);
        strictInsertFill(meta, "updateTime", LocalDateTime.class, now);
        if (meta.hasSetter("deleted")) {
            strictInsertFill(meta, "deleted", Integer.class, 0);
        }
        if (meta.hasSetter("version")) {
            strictInsertFill(meta, "version", Integer.class, 0);
        }
        Long uid = currentUserId();
        if (uid != null) {
            if (meta.hasSetter("createBy")) {
                strictInsertFill(meta, "createBy", Long.class, uid);
            }
            if (meta.hasSetter("updateBy")) {
                strictInsertFill(meta, "updateBy", Long.class, uid);
            }
        }
    }

    @Override
    public void updateFill(MetaObject meta) {
        strictUpdateFill(meta, "updateTime", LocalDateTime.class, LocalDateTime.now());
        Long uid = currentUserId();
        if (uid != null && meta.hasSetter("updateBy")) {
            strictUpdateFill(meta, "updateBy", Long.class, uid);
        }
    }

    /** 登录接口本身没有上下文，返回 null 即可，不能抛异常 */
    private Long currentUserId() {
        try {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            if (a != null && a.getPrincipal() instanceof LoginUser lu) {
                return lu.getUserId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
