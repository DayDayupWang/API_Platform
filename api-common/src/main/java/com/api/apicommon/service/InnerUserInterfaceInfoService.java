package com.api.apicommon.service;

/**
 *
 */
public interface InnerUserInterfaceInfoService {


    /**
     * 查询是否还有接口次数
     * @param userId
     * @param interfaceInfoId
     * @return
     */
     boolean hasInvokeNum(long userId, long interfaceInfoId);

    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);
}
