package com.metarnet.core.common.adapter;

import com.metarnet.core.common.exception.AdapterException;

/**
 * Created with IntelliJ IDEA.
 * User: metarnet
 * Date: 13-4-4
 * Time: 上午11:08
 * 配置管理服务适配器
 */
public class DataRouteAdapter {
    private static final DataRouteAdapter _DataRouteAdapter = new DataRouteAdapter();

    private DataRouteAdapter() {

    }

    public static DataRouteAdapter getInstence() {
        return _DataRouteAdapter;
    }

    public Integer findShardingIdByOrgId(int cloudOrgId) throws AdapterException {
        return 1;
    }

    public Integer findShardInfoByOrgId(int cloudOrgId) throws AdapterException {
        /*try {
            ShardInfoVO shardInfoVO = _DataRouteService.findShardInfoByOrgId(AAAAAdapter.getCompany(cloudOrgId)
                    .getCloudOrgId());
            if (null == shardInfoVO) {
                return findShardInfoByOrgId(AAAAAdapter.getInstence().findOrgByOrgID(AAAAAdapter.getCompany
                        (cloudOrgId).getCloudOrgId()).getParentCloudOrgId());
            }
            return shardInfoVO;
        } catch (PaasAAAAException e) {
            throw new AdapterException(e);
        }*/
//        ShardInfoVO shardInfoVO = _DataRouteService.findShardInfoByOrgId(cloudOrgId);
//        if (shardInfoVO == null) {
//            shardInfoVO = new ShardInfoVO();
//        }
//        if (shardInfoVO.getShardingId() == null) {
//            shardInfoVO.setShardingId(0);
//        }
//        return shardInfoVO;
        return 1;
    }

    public Integer findShardInfoByAreaId(String areaId) throws AdapterException {
        return 1;
//        return _DataRouteService.findShardInfoByAreaId(areaId);
//        ShardInfoVO shardInfoVO = _DataRouteService.findShardInfoByAreaId(areaId);
//
//        if (shardInfoVO == null) {
//            shardInfoVO = new ShardInfoVO();
//        }
//        if (shardInfoVO.getShardingId() == null) {
//            shardInfoVO.setShardingId(0);
//        }
//        return shardInfoVO;
    }
}
