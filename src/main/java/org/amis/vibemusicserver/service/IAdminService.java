package org.amis.vibemusicserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.amis.vibemusicserver.model.dto.AdminDTO;
import org.amis.vibemusicserver.model.entity.Admin;
import org.amis.vibemusicserver.result.Result;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author KwokChichung
 */
public interface IAdminService extends IService<Admin> {

    // 管理员注册
    Result register(AdminDTO adminDTO);

    // 管理员登录
    Result login(AdminDTO adminDTO);

    // 退出登录
    Result logout(String token);
}
