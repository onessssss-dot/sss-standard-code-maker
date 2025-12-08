package com.example.demo.service;

import com.example.demouser.model.dto.app.AppAddRequest;
import com.example.demouser.model.dto.app.AppQueryRequest;
import com.example.demouser.model.entity.App;
import com.example.demouser.model.entity.User;
import com.example.demouser.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 *  服务层。
 *
 * @author <a>SSS</a>
 */
public interface AppService extends IService<App> {

    /**
     * 获取应用封装类
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    String deployApp(Long appId, User loginUser);


    /**
     * 获取应用封装类列表
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 构造应用 查询条件
     * @param appQueryRequest
     * @return
     */
     QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);


    /**
     * 生成应用代码
     * @param appId 应用ID
     * @param message 生成提示词
     * @param loginUser 登录用户
     * @return
     */
     Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 创建应用
     * @param appAddRequest
     * @param loginUser
     * @return
     */
     Long createApp(AppAddRequest appAddRequest, User loginUser);
}
