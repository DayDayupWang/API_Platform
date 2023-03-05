package com.api.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.api.project.model.entity.InterfaceInfo;

/**
* @author shinelon
* @description 针对表【interface_info(接口信息表)】的数据库操作Service
* @createDate 2023-02-28 20:25:37
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
}
