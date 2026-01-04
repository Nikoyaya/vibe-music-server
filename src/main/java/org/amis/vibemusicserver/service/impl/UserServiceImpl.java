package org.amis.vibemusicserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.amis.vibemusicserver.mapper.UserMapper;
import org.amis.vibemusicserver.model.entity.User;
import org.amis.vibemusicserver.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * @author : KwokChichung
 * @description : 用户服务实现类
 * @createDate : 2026/1/5 1:08
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
}

