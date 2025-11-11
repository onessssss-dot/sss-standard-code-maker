package com.example.demo.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.example.demo.model.entity.App;
import com.example.demo.mapper.AppMapper;
import com.example.demo.service.AppService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author <a>SSS</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
