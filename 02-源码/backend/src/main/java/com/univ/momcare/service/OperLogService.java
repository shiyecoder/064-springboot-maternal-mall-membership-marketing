package com.univ.momcare.service;

import com.univ.momcare.entity.SysOperLog;
import com.univ.momcare.mapper.SysOperLogMapper;
import com.univ.momcare.security.CurrentUser;
import com.univ.momcare.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** 关键写操作留痕：状态流转、发券、人工修正、规则变更都会调用 */
@Service
@RequiredArgsConstructor
public class OperLogService {

    private final SysOperLogMapper operLogMapper;

    /** 常用入口：操作人取当前登录人，未登录（定时任务）记为空 */
    public void log(String module, String type, String bizNo, String content) {
        LoginUser u = null;
        try {
            u = CurrentUser.get();
        } catch (Exception ignored) {
            // 定时任务场景无登录上下文
        }
        log(module, type, bizNo, content, u == null ? null : u.getUserId(),
                u == null ? "系统" : u.getRealName());
    }

    public void log(String module, String type, String bizNo, String content, Long operId, String operName) {
        SysOperLog l = new SysOperLog();
        l.setModule(module);
        l.setOperType(type);
        l.setBizNo(bizNo);
        l.setContent(content != null && content.length() > 480 ? content.substring(0, 480) : content);
        l.setOperId(operId);
        l.setOperName(operName);
        l.setOperTime(LocalDateTime.now());
        l.setIp("127.0.0.1");
        operLogMapper.insert(l);
    }
}
