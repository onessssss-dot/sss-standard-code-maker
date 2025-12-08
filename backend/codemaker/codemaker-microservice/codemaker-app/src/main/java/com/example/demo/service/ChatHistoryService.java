package com.example.demo.service;

import com.example.demouser.model.dto.chathistor.ChatHistoryQueryRequest;
import com.example.demouser.model.entity.ChatHistory;
import com.example.demouser.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 *  服务层。
 *
 * @author <a>SSS</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加消息记录
     * @param appId
     * @param message
     * @param messageType
     * @param userId
     * @return
     */
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 根据应用id删除对话历史
     * @param appId
     * @return
     */
    boolean deleteByAppId(Long appId);

    /**
     * 构造查询条件
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 游标查看历史对话记录
     *
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param loginUser
     * @return
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);


    /**
     * 加载对话历史到内存
     * @param appId
     * @param chatMemory
     * @param maxCount 最多加载多少条
     * @return
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
