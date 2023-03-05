package com.api.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.api.project.common.ErrorCode;
import com.api.project.exception.BusinessException;
import com.api.project.mapper.InterfaceInfoMapper;
import com.api.project.model.entity.InterfaceInfo;
import com.api.project.service.InterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author shinelon
 * @description 针对表【interface_info(接口信息表)】的数据库操作Service实现
 * @createDate 2023-02-28 20:25:37
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
        implements InterfaceInfoService {
    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = interfaceInfo.getId();
        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        String requestHeader = interfaceInfo.getRequestHeader();
        String responseHeader = interfaceInfo.getResponseHeader();
        Date createTime = interfaceInfo.getCreateTime();
        Date updateTime = interfaceInfo.getUpdateTime();
        Integer status = interfaceInfo.getStatus();
        Long userId = interfaceInfo.getUserId();
        String method = interfaceInfo.getMethod();
        Integer isDelete = interfaceInfo.getIsDelete();


        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() < 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }
}




