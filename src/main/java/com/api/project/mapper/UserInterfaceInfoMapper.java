package com.api.project.mapper;


import com.api.apicommon.model.entity.UserInterfaceInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author wyh
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
* @createDate 2023-03-06 11:45:43
* @Entity generator.domain.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
    //查询前limit个调用次数最多的接口
    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);

}




