package com.univ.momcare.service;

import com.univ.momcare.entity.SysMessage;
import com.univ.momcare.mapper.SysMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** 站内消息：状态流转、审批、超时预警统一在此推送 */
@Service
@RequiredArgsConstructor
public class MsgService {

    private final SysMessageMapper msgMapper;

    public void send(Long receiverId, String title, String content, String bizType, Long bizId, String level) {
        if (receiverId == null) return;
        SysMessage m = new SysMessage();
        m.setReceiverId(receiverId);
        m.setTitle(title.length() > 110 ? title.substring(0, 110) : title);
        m.setContent(content != null && content.length() > 480 ? content.substring(0, 480) : content);
        m.setBizType(bizType);
        m.setBizId(bizId);
        m.setMsgLevel(level == null ? "普通" : level);
        m.setReadFlag(0);
        m.setCreateTime(LocalDateTime.now());
        msgMapper.insert(m);
    }
}
