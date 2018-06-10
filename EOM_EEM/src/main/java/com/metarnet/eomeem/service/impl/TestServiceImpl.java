package com.metarnet.eomeem.service.impl;

import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.eomeem.model.EemTempEntity;
import com.metarnet.eomeem.model.TemplateExcelByteData;
import com.metarnet.eomeem.service.ITestService;
import com.metarnet.eomeem.utils.EemConstants;
import com.metarnet.eomeem.utils.ExcelConverter2;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/16.
 */
@Service
public class TestServiceImpl implements ITestService {
    @Resource
    private IBaseDAO baseDAO;
    private static Map<String, String> reportMap = new HashMap<String, String>();

    static {
        reportMap.put("ADSL上行家庭网关设备质量及售后服务评价表v1.0.xls", "ADSL上行家庭网关");
        reportMap.put("BSC设备后评价季度表v1.1.xls", "BSC");
        reportMap.put("BTS设备后评价季度表v1.1.xls", "BTS");
        reportMap.put("EPON-FTTB-ONU(非盒式)设备运行质量后评价季度表v3.1.xls", "EPON-FTTB-ONU(非盒式)");
        reportMap.put("EPON-FTTB-ONU(盒式)设备运行质量后评价季度表v3.1.xls", "EPON-FTTB-ONU(盒式)质量");
        reportMap.put("EPON-FTTH-ONU设备运行质量后评价季度表v3.2.xls", "EPON-FTTH-ONU质量");
        reportMap.put("EPON-OLT设备运行质量后评价季度表v3.1.xls", "EPON-OLT质量");
        reportMap.put("EPON设备售后服务后评价季度表v3.2.xls", "EPON服务");
        reportMap.put("FTTH-ONU设备互通商用情况后评价及问题说明报表v1.4.xls", "FTTH-ONU设备互通商用后评价表");
        reportMap.put("GGSN设备后评价季度表v1.1.xls", "GGSN");
        reportMap.put("GPON-FTTB-ONU(非盒式)设备运行质量后评价季度表v3.1.xls", "GPON-FTTB-ONU(非盒式)质量");
        reportMap.put("GPON-FTTB-ONU(盒式)设备运行质量后评价季度表v3.1.xls", "GPON-FTTB-ONU(盒式)质量");
        reportMap.put("GPON-FTTH-ONU设备运行质量后评价季度表v3.2.xls", "GPON-FTTH-ONU质量");
        reportMap.put("GPON-OLT设备运行质量后评价季度报表v3.1.xls", "GPON-OLT");
        reportMap.put("GPON设备售后服务后评价季度表v3.2.xls", "GPON服务");
        reportMap.put("IP承载网设备v3.1.xls", "IP承载网");
        reportMap.put("LAN上行家庭网关设备质量及售后服务评价表v1.0.xls", "LAN上行家庭网关");
        reportMap.put("LTE-FDD设备后评价表20151009.xls", "LTE-FDD");
        reportMap.put("MSC-SERVER设备后评价季度表v1.1.xls", "MSC-SERVER");
        reportMap.put("Node-B设备后评价季度表v1.1.xls", "Node-B");
        reportMap.put("OLT设备(含EMS)互通商用情况后评价及问题说明报表v1.4.xls", "OLT设备(含EMS)互通商用");
        reportMap.put("OTN服务满意度后评价季度表v3.2.xls", "OTN服务满意度");
        reportMap.put("OTN设备质量后评价季度表v3.2.xls", "OTN设备质量");
        reportMap.put("RNC设备后评价季度表v1.1.xls", "RNC");
        reportMap.put("SACP设备后评价季度表v1.1.xls", "SACP");
        reportMap.put("SCP设备后评价记录表v1.1.xls", "SCP");
        reportMap.put("SDH服务满意度后评价季度表v3.3.xls", "SDH服务满意度");
        reportMap.put("SDH设备质量后评价季度表v3.3.xls", "SDH设备质量");
        reportMap.put("SGSN设备后评价季度表v1.1.xls", "SGSN");
        reportMap.put("TD-LTE设备后评价表20151009.xls", "TD_LTE");
        reportMap.put("UPS后评价季度报表v3.0.xls", "UPS");
        reportMap.put("WDM服务满意度后评价季度表v3.1.xls", "WDM服务满意度");
        reportMap.put("WDM设备质量后评价季度表v3.1.xls", "WDM设备质量");
        reportMap.put("WLAN-AC接入设备后评价季度报表v3.1.xls", "WLAN-AC");
        reportMap.put("WLAN-AP接入设备后评价季度报表v3.2.xls", "WLAN-AP");
        reportMap.put("xDSL设备运行质量及售后服务评价季度报表v3.1.xls", "xDSL");
        reportMap.put("边缘SDH-MSTP服务满意度后评价季度表v3.1.xls", "边缘SDH-MSTP服务满意度");
        reportMap.put("边缘SDH-MSTP设备质量后评价季度表v3.1.xls", "边缘SDH-MSTP设备质量");
        reportMap.put("电力电缆后评价季度报表v1.0.xls", "电力电缆");
        reportMap.put("蝶形光缆测试记录表v1.6.xls", "蝶形光缆");
        reportMap.put("蝶形光缆综合评价表v1.5.xls", "蝶形光缆综合评价表");
        reportMap.put("高端交换机(SW)设备后评价季度表v3.1.xls", "高端交换机(SW)设备");
        reportMap.put("固定油机后评价季度报表v1.0.xls", "固定油机");
        reportMap.put("光分路器后评价季度报表v1.0.xls", "光分路器");
        reportMap.put("光缆测试评价表v1.6.xls", "光缆测试");
        reportMap.put("光缆接头盒后评价季度报表v1.1.xls", "光缆接头盒");
        reportMap.put("光缆综合评价表v1.7.xls", "光缆产品供货和综合");
        reportMap.put("光跳纤后评价季度报表v1.1.xls", "光跳纤");
        reportMap.put("光纤后评价季度报表v1.3.xls", "光纤");
        reportMap.put("光纤配线架后评价季度报表v1.0.xls", "光纤配线架");
        reportMap.put("核心路由器(CR)设备后评价季度表v3.1.xls", "核心路由器(CR)");
        reportMap.put("基站精密空调后评价季度报表v1.0.xls", "基站精密空调");
        reportMap.put("胶体蓄电池后评价季度报表v1.0.xls", "胶体蓄电池");
        reportMap.put("局用空调后评价季度报表v1.0.xls", "局用空调");
        reportMap.put("局用信号电缆v1.1.xls", "局用信号电缆");
        reportMap.put("开关电源后评价季度报表v1.0.xls", "开关电源");
        reportMap.put("宽带接入服务器(BRAS)设备后评价季度表v3.1.xls", "宽带接入服务器(BRAS)");
        reportMap.put("馈线后评价季度报表V1.4.xls", "馈线");
        reportMap.put("铅酸蓄电池后评价季度报表v1.0.xls", "铅酸蓄电池");
        reportMap.put("设备后评价评分细则-分组承载传送设备(核心汇聚)_V1.2.xls", "分组传送设备核心汇聚");
        reportMap.put("设备后评价评分细则-分组承载传送设备(接入)_v2.5.xls", "分组传送设备接入");
        reportMap.put("市内通信电缆后评价季度报表v1.1.xls", "市内通信电缆");
        reportMap.put("室内分布系统后评价季度报表v1.1.xls", "室内分布系统");
        reportMap.put("舒适性空调后评价季度报表v1.0.xls", "舒适性空调");
        reportMap.put("数字配线架后评价季度报表v1.0.xls", "数字配线架");
        reportMap.put("数字通信电缆后评价季度报表v1.1.xls", "数字通信电缆");
        reportMap.put("天线后评价季度报表v1.4.xls", "天线");
        reportMap.put("卫星服务后评价季度报表v1.2.xls", "卫星-服务");
        reportMap.put("卫星设备质量后评价季度报表v1.1.xls", "卫星-质量");
        reportMap.put("业务路由器(SR)设备后评价季度表v3.1.xls", "业务路由器(SR)");
        reportMap.put("移动核心网设备-HLR设备后评价季度表v1.1.xls", "HLR");
        reportMap.put("移动核心网设备-MGW设备后评价季度表v1.1.xls", "MGW");
        reportMap.put("移动油机后评价季度报表v1.1.xls", "移动油机");
        reportMap.put("直放站后评价季度报表v1.1.xls", "直放站");
        reportMap.put("中低端交换机(非盒式设备)后评价表v3.1.xls", "中低端交换机-非盒式设备");
        reportMap.put("中低端交换机(盒式设备)后评价表v3.1.xls", "中低端交换机-盒式设备");
        reportMap.put("中低端路由器(非盒式设备)后评价表v3.1.xls", "中低端路由器-非盒式设备");
        reportMap.put("中低端路由器(盒式设备)后评价表v3.1.xls", "中低端路由器-盒式设备");
        reportMap.put("综合配线架后评价季度报表v1.0.xls", "综合配线架");
    }

    private static Map<String, String> collectMap = new HashMap<String, String>();

    static {
        collectMap.put("ADSL上行家庭网关设备质量及售后服务汇总表v1.0.xls", "ADSL");
        collectMap.put("BSC设备后评价汇总表v1.5.xls", "BSC");
        collectMap.put("BTS设备后评价汇总表v1.4.xls", "BTS");
        collectMap.put("EPON-FTTB-ONU(非盒式)汇总表v3.9.xls", "EPON-FTTB-ONU(非盒式)");
        collectMap.put("EPON-FTTB-ONU(盒式)汇总表v3.10.xls", "EPON-FTTB-ONU(盒式)质量");
        collectMap.put("EPON-FTTH-ONU汇总表v3.9.xls", "EPON-FTTH-ONU");
        collectMap.put("FTTH-ONU设备互通商用情况后评价汇总表v1.2.xls", "FTTH-ONU");
        collectMap.put("GGSN设备后评价汇总表v1.4.xls", "GGSN");
        collectMap.put("GPON-FTTB-ONU(非盒式)汇总表v3.10.xls", "GPON-FTTB-ONU(非盒式)");
        collectMap.put("GPON-FTTB-ONU(盒式)汇总表v3.9.xls", "GPON-FTTB-ONU(盒式)");
        collectMap.put("GPON-FTTH-ONU汇总表v3.9.xls", "GPON-FTTH-ONU");
        collectMap.put("GPON-OLT汇总表v3.9.xls", "GPON-OLT");
        collectMap.put("IP承载网后评价汇总表v3.1.xls", "IP承载网");
        collectMap.put("LAN上行家庭网关设备质量及售后服务汇总表v1.0.xls", "LAN");
        collectMap.put("LTE-FDD设备后评价表20151009汇总表v1.2.xls", "LTE-FDD");
        collectMap.put("MSC-SERVER设备后评价汇总表v1.4.xls", "MSC-SERVER");
        collectMap.put("Node-B设备后评价汇总表v1.4.xls", "Node-B");
        collectMap.put("OLT设备(含EMS)互通商用情况后评价汇总表v1.2.xls", "OLT设备");
        collectMap.put("OTN后评价汇总表v3.9.xls", "OTN");
        collectMap.put("RNC设备后评价汇总表v1.5.xls", "RNC");
        collectMap.put("SACP设备后评价汇总表v1.4.xls", "SACP");
        collectMap.put("SCP设备后评价汇总表v1.4.xls", "SCP");
        collectMap.put("SDH后评价汇总表v3.8.xls", "SDH");
        collectMap.put("SGSN设备后评价汇总表v1.4.xls", "SGSN");
        collectMap.put("TD-LTE设备后评价表20151009汇总表v1.2.xls", "TD-LTE");
        collectMap.put("UPS后评价汇总表v3.4.xls", "UPS");
        collectMap.put("WDM后评价汇总表v3.8.xls", "WDM");
        collectMap.put("WLAN接入设备汇总表v4.2.xls", "WLAN");
        collectMap.put("xDSL设备设备运行质量及售后服务后评价汇总表v3.5.xls", "xDSL");
        collectMap.put("边缘SDH-MSTP后评价汇总表v3.8.xls", "SDH-MSTP");
        collectMap.put("电力电缆后评价汇总表v3.1.xls", "电力电缆");
        collectMap.put("蝶形光缆测试汇总v3.2.xls", "蝶形光缆测试");
        collectMap.put("蝶形光缆综合汇总v3.1.xls", "蝶形光缆综合");
        collectMap.put("高端交换机(SW)后评价汇总表v3.5.xls", "高端交换机(SW)");
        collectMap.put("固定油机后评价汇总表v3.1.xls", "固定油机");
        collectMap.put("光分路器后评价汇总表v3.2.xls", "光分路器");
        collectMap.put("光缆测试汇总v3.2.xls", "光缆测试");
        collectMap.put("光缆接头盒后评价汇总表v3.2.xls", "光缆接头盒");
        collectMap.put("光缆综合汇总v4.1.xls", "光缆综合");
        collectMap.put("光跳纤后评价汇总表v3.6.xls", "光跳纤");
        collectMap.put("光纤后评价汇总表v3.2.xls", "光纤");
        collectMap.put("光纤配线架后评价汇总表v3.2.xls", "光纤配线架");
        collectMap.put("核心路由器(CR)后评价汇总表v3.5.xls", "核心路由器(CR)");
        collectMap.put("基站精密空调后评价汇总表v3.1.xls", "基站精密空调");
        collectMap.put("胶体蓄电池后评价汇总表v3.1.xls", "胶体蓄电池");
        collectMap.put("局用空调后评价汇总表v3.1.xls", "局用空调");
        collectMap.put("局用信号电缆设备后评价汇总表v3.2.xls", "局用信号电缆");
        collectMap.put("开关电源后评价汇总表v3.2.xls", "开关电源");
        collectMap.put("宽带接入服务器(BRAS)后评价汇总表v3.5.xls", "宽带接入服务器(BRAS)");
        collectMap.put("馈线后评价汇总表v3.1.xls", "馈线");
        collectMap.put("铅酸蓄电池后评价汇总表v3.1.xls", "铅酸蓄电池");
        collectMap.put("设备后评价评分细则-分组承载传送设备(核心汇聚)_汇总表v1.0.xls", "设备后评价评分细则-核心汇聚");
        collectMap.put("设备后评价评分细则-分组承载传送设备(接入)_汇总表v2.8.xls", "设备后评价评分细则-接入");
        collectMap.put("市内通信电缆设备后评价汇总表v3.4.xls", "市内通信电缆");
        collectMap.put("室内分布系统后评价汇总表v3.1.xls", "室内分布系统");
        collectMap.put("舒适性空调后评价汇总表v3.1.xls", "舒适性空调");
        collectMap.put("数字配线架后评价汇总表v3.2.xls", "数字配线架");
        collectMap.put("数字通信电缆后评价汇总表v3.2.xls", "数字通信电缆");
        collectMap.put("天线后评价汇总表v3.8.xls", "天线");
        collectMap.put("卫星设备服务后评价汇总表v1.4.xls", "卫星设备");
        collectMap.put("卫星设备后评价(卫星数据调制解调器)汇总表v2.1.xls", "卫星设备后评价(卫星数据调制解调器)");
        collectMap.put("业务路由器(SR)后评价汇总表v3.5.xls", "业务路由器(SR)");
        collectMap.put("移动核心网设备-HLR设备后评价汇总表v1.4.xls", "HLR");
        collectMap.put("移动核心网设备-MGW设备后评价汇总表v1.4.xls", "MGW");
        collectMap.put("移动油机后评价汇总表v3.1.xls", "移动油机");
        collectMap.put("直放站后评价汇总表v3.1.xls", "直放站");
        collectMap.put("中低端交换机(非盒式设备)后评价汇总表v3.6.xls", "中低端交换机-非盒式设备");
        collectMap.put("中低端交换机(盒式设备)后评价汇总表v3.6.xls", "中低端交换机-盒式设备");
        collectMap.put("中低端路由器(非盒式设备)后评价汇总表v3.6.xls", "中低端路由器-非盒式设备");
        collectMap.put("中低端路由器(盒式设备)后评价汇总表表v3.6.xls", "中低端路由器-盒式设备");
        collectMap.put("综合配线架设备集采后评价汇总表v3.2.xls", "综合配线架");
        collectMap.put("EPON-OLT汇总表v3.9.xls", "EPON-OLT");
    }

    private static Map<String, Long> idsMap = new HashMap<String, Long>();

    static {
        idsMap.put("ADSL上行家庭网关设备质量及售后服务评价表v1.0.xls", 1L);
        idsMap.put("BSC设备后评价季度表v1.1.xls", 2L);
        idsMap.put("BTS设备后评价季度表v1.1.xls", 3L);
        idsMap.put("EPON-FTTB-ONU(盒式)设备运行质量后评价季度表v3.1.xls", 4L);
        idsMap.put("EPON-FTTB-ONU(非盒式)设备运行质量后评价季度表v3.1.xls", 5L);
        idsMap.put("EPON-FTTH-ONU设备运行质量后评价季度表v3.2.xls", 6L);
        idsMap.put("EPON-OLT设备运行质量后评价季度表v3.1.xls", 7L);
        idsMap.put("EPON设备售后服务后评价季度表v3.2.xls", 8L);
        idsMap.put("FTTH-ONU设备互通商用情况后评价及问题说明报表v1.4.xls", 9L);
        idsMap.put("GGSN设备后评价季度表v1.1.xls", 10L);
        idsMap.put("GPON-FTTB-ONU(盒式)设备运行质量后评价季度表v3.1.xls", 11L);
        idsMap.put("GPON-FTTB-ONU(非盒式)设备运行质量后评价季度表v3.1.xls", 12L);
        idsMap.put("GPON-FTTH-ONU设备运行质量后评价季度表v3.2.xls", 13L);
        idsMap.put("GPON-OLT设备运行质量后评价季度报表v3.1.xls", 14L);
        idsMap.put("GPON设备售后服务后评价季度表v3.2.xls", 15L);
        idsMap.put("IP承载网设备v3.1.xls", 16L);
        idsMap.put("LAN上行家庭网关设备质量及售后服务评价表v1.0.xls", 17L);
        idsMap.put("LTE-FDD设备后评价表20151009.xls", 18L);
        idsMap.put("MSC-SERVER设备后评价季度表v1.1.xls", 19L);
        idsMap.put("Node-B设备后评价季度表v1.1.xls", 20L);
        idsMap.put("OLT设备(含EMS)互通商用情况后评价及问题说明报表v1.4.xls", 21L);
        idsMap.put("OTN服务满意度后评价季度表v3.2.xls", 22L);
        idsMap.put("OTN设备质量后评价季度表v3.2.xls", 23L);
        idsMap.put("RNC设备后评价季度表v1.1.xls", 24L);
        idsMap.put("SACP设备后评价季度表v1.1.xls", 25L);
        idsMap.put("SCP设备后评价记录表v1.1.xls", 26L);
        idsMap.put("SDH服务满意度后评价季度表v3.3.xls", 27L);
        idsMap.put("SDH设备质量后评价季度表v3.3.xls", 28L);
        idsMap.put("SGSN设备后评价季度表v1.1.xls", 29L);
        idsMap.put("TD-LTE设备后评价表20151009.xls", 30L);
        idsMap.put("UPS后评价季度报表v3.0.xls", 31L);
        idsMap.put("WDM服务满意度后评价季度表v3.1.xls", 32L);
        idsMap.put("WDM设备质量后评价季度表v3.1.xls", 33L);
        idsMap.put("WLAN-AC接入设备后评价季度报表v3.1.xls", 34L);
        idsMap.put("WLAN-AP接入设备后评价季度报表v3.2.xls", 35L);
        idsMap.put("xDSL设备运行质量及售后服务评价季度报表v3.1.xls", 36L);
        idsMap.put("业务路由器(SR)设备后评价季度表v3.1.xls", 37L);
        idsMap.put("中低端交换机(盒式设备)后评价表v3.1.xls", 38L);
        idsMap.put("中低端交换机(非盒式设备)后评价表v3.1.xls", 39L);
        idsMap.put("中低端路由器(盒式设备)后评价表v3.1.xls", 40L);
        idsMap.put("中低端路由器(非盒式设备)后评价表v3.1.xls", 41L);
        idsMap.put("光分路器后评价季度报表v1.0.xls", 42L);
        idsMap.put("光纤后评价季度报表v1.3.xls", 43L);
        idsMap.put("光纤配线架后评价季度报表v1.0.xls", 44L);
        idsMap.put("光缆接头盒后评价季度报表v1.1.xls", 45L);
        idsMap.put("光缆测试评价表v1.6.xls", 46L);
        idsMap.put("光缆综合评价表v1.7.xls", 47L);
        idsMap.put("光跳纤后评价季度报表v1.1.xls", 48L);
        idsMap.put("卫星服务后评价季度报表v1.2.xls", 49L);
        idsMap.put("卫星设备质量后评价季度报表v1.1.xls", 50L);
        idsMap.put("固定油机后评价季度报表v1.0.xls", 51L);
        idsMap.put("基站精密空调后评价季度报表v1.0.xls", 52L);
        idsMap.put("天线后评价季度报表v1.4.xls", 53L);
        idsMap.put("室内分布系统后评价季度报表v1.1.xls", 54L);
        idsMap.put("宽带接入服务器(BRAS)设备后评价季度表v3.1.xls", 55L);
        idsMap.put("局用信号电缆v1.1.xls", 56L);
        idsMap.put("局用空调后评价季度报表v1.0.xls", 57L);
        idsMap.put("市内通信电缆后评价季度报表v1.1.xls", 58L);
        idsMap.put("开关电源后评价季度报表v1.0.xls", 59L);
        idsMap.put("数字通信电缆后评价季度报表v1.1.xls", 60L);
        idsMap.put("数字配线架后评价季度报表v1.0.xls", 61L);
        idsMap.put("核心路由器(CR)设备后评价季度表v3.1.xls", 62L);
        idsMap.put("电力电缆后评价季度报表v1.0.xls", 63L);
        idsMap.put("直放站后评价季度报表v1.1.xls", 64L);
        idsMap.put("移动核心网设备-HLR设备后评价季度表v1.1.xls", 65L);
        idsMap.put("移动核心网设备-MGW设备后评价季度表v1.1.xls", 66L);
        idsMap.put("移动油机后评价季度报表v1.1.xls", 67L);
        idsMap.put("综合配线架后评价季度报表v1.0.xls", 68L);
        idsMap.put("胶体蓄电池后评价季度报表v1.0.xls", 69L);
        idsMap.put("舒适性空调后评价季度报表v1.0.xls", 70L);
        idsMap.put("蝶形光缆测试记录表v1.6.xls", 71L);
        idsMap.put("蝶形光缆综合评价表v1.5.xls", 72L);
        idsMap.put("设备后评价评分细则-分组承载传送设备(接入)_v2.5.xls", 73L);
        idsMap.put("设备后评价评分细则-分组承载传送设备(核心汇聚)_V1.2.xls", 74L);
        idsMap.put("边缘SDH-MSTP服务满意度后评价季度表v3.1.xls", 75L);
        idsMap.put("边缘SDH-MSTP设备质量后评价季度表v3.1.xls", 76L);
        idsMap.put("铅酸蓄电池后评价季度报表v1.0.xls", 77L);
        idsMap.put("馈线后评价季度报表V1.4.xls", 78L);
        idsMap.put("高端交换机(SW)设备后评价季度表v3.1.xls", 79L);
        idsMap.put("ADSL上行家庭网关设备质量及售后服务汇总表v1.0.xls", 80L);
        idsMap.put("BSC设备后评价汇总表v1.5.xls", 81L);
        idsMap.put("BTS设备后评价汇总表v1.4.xls", 82L);
        idsMap.put("EPON-FTTB-ONU(盒式)汇总表v3.10.xls", 83L);
        idsMap.put("EPON-FTTB-ONU(非盒式)汇总表v3.9.xls", 84L);
        idsMap.put("EPON-FTTH-ONU汇总表v3.9.xls", 85L);
        idsMap.put("EPON-OLT汇总表v3.9.xls", 86L);
        idsMap.put("FTTH-ONU设备互通商用情况后评价汇总表v1.2.xls", 87L);
        idsMap.put("GGSN设备后评价汇总表v1.4.xls", 88L);
        idsMap.put("GPON-FTTB-ONU(盒式)汇总表v3.9.xls", 89L);
        idsMap.put("GPON-FTTB-ONU(非盒式)汇总表v3.10.xls", 90L);
        idsMap.put("GPON-FTTH-ONU汇总表v3.9.xls", 91L);
        idsMap.put("GPON-OLT汇总表v3.9.xls", 92L);
        idsMap.put("IP承载网后评价汇总表v3.1.xls", 93L);
        idsMap.put("LAN上行家庭网关设备质量及售后服务汇总表v1.0.xls", 94L);
        idsMap.put("LTE-FDD设备后评价表20151009汇总表v1.2.xls", 95L);
        idsMap.put("MSC-SERVER设备后评价汇总表v1.4.xls", 96L);
        idsMap.put("Node-B设备后评价汇总表v1.4.xls", 97L);
        idsMap.put("OLT设备(含EMS)互通商用情况后评价汇总表v1.2.xls", 98L);
        idsMap.put("OTN后评价汇总表v3.9.xls", 99L);
        idsMap.put("RNC设备后评价汇总表v1.5.xls", 100L);
        idsMap.put("SACP设备后评价汇总表v1.4.xls", 101L);
        idsMap.put("SCP设备后评价汇总表v1.4.xls", 102L);
        idsMap.put("SDH后评价汇总表v3.8.xls", 103L);
        idsMap.put("SGSN设备后评价汇总表v1.4.xls", 104L);
        idsMap.put("TD-LTE设备后评价表20151009汇总表v1.2.xls", 105L);
        idsMap.put("UPS后评价汇总表v3.4.xls", 106L);
        idsMap.put("WDM后评价汇总表v3.8.xls", 107L);
        idsMap.put("WLAN接入设备汇总表v4.2.xls", 108L);
        idsMap.put("xDSL设备设备运行质量及售后服务后评价汇总表v3.5.xls", 109L);
        idsMap.put("业务路由器(SR)后评价汇总表v3.5.xls", 110L);
        idsMap.put("中低端交换机(盒式设备)后评价汇总表v3.6.xls", 111L);
        idsMap.put("中低端交换机(非盒式设备)后评价汇总表v3.6.xls", 112L);
        idsMap.put("中低端路由器(盒式设备)后评价汇总表表v3.6.xls", 113L);
        idsMap.put("中低端路由器(非盒式设备)后评价汇总表v3.6.xls", 114L);
        idsMap.put("光分路器后评价汇总表v3.2.xls", 115L);
        idsMap.put("光纤后评价汇总表v3.2.xls", 116L);
        idsMap.put("光纤配线架后评价汇总表v3.2.xls", 117L);
        idsMap.put("光缆接头盒后评价汇总表v3.2.xls", 118L);
        idsMap.put("光缆测试汇总v3.2.xls", 119L);
        idsMap.put("光缆综合汇总v4.1.xls", 120L);
        idsMap.put("光跳纤后评价汇总表v3.6.xls", 121L);
        idsMap.put("卫星设备后评价(卫星数据调制解调器)汇总表v2.1.xls", 122L);
        idsMap.put("卫星设备服务后评价汇总表v1.4.xls", 123L);
        idsMap.put("固定油机后评价汇总表v3.1.xls", 124L);
        idsMap.put("基站精密空调后评价汇总表v3.1.xls", 125L);
        idsMap.put("天线后评价汇总表v3.8.xls", 126L);
        idsMap.put("室内分布系统后评价汇总表v3.1.xls", 127L);
        idsMap.put("宽带接入服务器(BRAS)后评价汇总表v3.5.xls", 128L);
        idsMap.put("局用信号电缆设备后评价汇总表v3.2.xls", 129L);
        idsMap.put("局用空调后评价汇总表v3.1.xls", 130L);
        idsMap.put("市内通信电缆设备后评价汇总表v3.4.xls", 131L);
        idsMap.put("开关电源后评价汇总表v3.2.xls", 132L);
        idsMap.put("数字通信电缆后评价汇总表v3.2.xls", 133L);
        idsMap.put("数字配线架后评价汇总表v3.2.xls", 134L);
        idsMap.put("核心路由器(CR)后评价汇总表v3.5.xls", 135L);
        idsMap.put("电力电缆后评价汇总表v3.1.xls", 136L);
        idsMap.put("直放站后评价汇总表v3.1.xls", 137L);
        idsMap.put("移动核心网设备-HLR设备后评价汇总表v1.4.xls", 138L);
        idsMap.put("移动核心网设备-MGW设备后评价汇总表v1.4.xls", 139L);
        idsMap.put("移动油机后评价汇总表v3.1.xls", 140L);
        idsMap.put("综合配线架设备集采后评价汇总表v3.2.xls", 141L);
        idsMap.put("胶体蓄电池后评价汇总表v3.1.xls", 142L);
        idsMap.put("舒适性空调后评价汇总表v3.1.xls", 143L);
        idsMap.put("蝶形光缆测试汇总v3.2.xls", 144L);
        idsMap.put("蝶形光缆综合汇总v3.1.xls", 145L);
        idsMap.put("设备后评价评分细则-分组承载传送设备(接入)_汇总表v2.8.xls", 146L);
        idsMap.put("设备后评价评分细则-分组承载传送设备(核心汇聚)_汇总表v1.0.xls", 147L);
        idsMap.put("边缘SDH-MSTP后评价汇总表v3.8.xls", 148L);
        idsMap.put("铅酸蓄电池后评价汇总表v3.1.xls", 149L);
        idsMap.put("馈线后评价汇总表v3.1.xls", 150L);
        idsMap.put("高端交换机(SW)后评价汇总表v3.5.xls", 151L);

    }

    @Override
    public void saveReportTemps(File file1, File file2, UserEntity userEntity) {
        try {
            EemTempEntity eemTempEntity = new EemTempEntity();
            eemTempEntity.setTempName(file1.getName().substring(0, file1.getName().lastIndexOf(".")));
            eemTempEntity.setTempType(1);
            if (file1.getName().equals("FTTH-ONU设备互通商用情况后评价及问题说明报表v1.4.xls") || file1.getName().equals("OLT设备(含EMS)互通商用情况后评价及问题说明报表v1.4.xls")) {
                eemTempEntity.setLevel(3);
                eemTempEntity.setTempPattern(1);
            } else if (file1.getName().equals("光缆测试评价表v1.6.xls") || file1.getName().equals("蝶形光缆测试记录表v1.6.xls") || file1.getName().equals("蝶形光缆综合评价表v1.5.xls") || file1.getName().equals("光缆综合评价表v1.7.xls")) {
                eemTempEntity.setLevel(2);
                eemTempEntity.setTempPattern(2);
            } else {
                eemTempEntity.setLevel(1);
                eemTempEntity.setTempPattern(1);
            }
            if (file1.getName().equals("光缆测试评价表v1.6.xls") || file1.getName().equals("蝶形光缆测试记录表v1.6.xls")) {
                eemTempEntity.setReportedFrequency(2);
            } else {
                eemTempEntity.setReportedFrequency(2);
            }
            eemTempEntity.setShortName(reportMap.get(file1.getName()));
            InputStream fis = new FileInputStream(file1);
            byte[] uploadByte = null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            uploadByte = bos.toByteArray();
            //页面模板
            ExcelConverter2 ec = new ExcelConverter2();
            byte[] xmlByte = ec.fromExcelFileByteArrayToXml(new FileInputStream(file1)).getBytes();
//            byte[] uploadByte = file1.getBytes();
            TemplateExcelByteData excelByteData = new TemplateExcelByteData();

            excelByteData.setObjectId(baseDAO.getSequenceNextValue(TemplateExcelByteData.class));

            excelByteData.setXmlFileData(xmlByte);
            excelByteData.setUploadFileData(uploadByte);
            eemTempEntity.setTemplateExcelByteData(excelByteData);
            //数据模板
            eemTempEntity.setPrimitiveName(file2.getName());
            String prefix = file2.getName().substring(file2.getName().lastIndexOf(".") + 1);
            eemTempEntity.setSuffix(prefix);
            eemTempEntity.setRelativePath("report/"+eemTempEntity.getTempName() + "." + prefix);
//            file2.transferTo(new File(EemConstants.RELATIVE_PATH + File.separator + eemTempEntity.getTempName() + "." + prefix));
            eemTempEntity.setObjectId(idsMap.get(file1.getName()));
            baseDAO.saveOrUpdate(eemTempEntity, userEntity);
        } catch (DAOException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveCollectTemps(File file1, File file2, UserEntity userEntity) {
        try {
            EemTempEntity eemTempEntity = new EemTempEntity();
            eemTempEntity.setTempName(file1.getName().substring(0, file1.getName().lastIndexOf(".")));
            eemTempEntity.setTempType(2);
            eemTempEntity.setLevel(1);
            if (file1.getName().equals("蝶形光缆测试汇总v3.2.xls") || file1.getName().equals("蝶形光缆综合汇总v3.1.xls") || file1.getName().equals("光缆测试汇总v3.2.xls") || file1.getName().equals("光缆综合汇总v4.1.xls")) {
                eemTempEntity.setTempPattern(2);
            } else {
                eemTempEntity.setTempPattern(1);
            }
            if (file1.getName().equals("蝶形光缆测试汇总v3.2.xls") || file1.getName().equals("光缆测试汇总v3.2.xls")) {
                eemTempEntity.setReportedFrequency(2);
            } else {
                eemTempEntity.setReportedFrequency(2);
            }
            eemTempEntity.setShortName(collectMap.get(file1.getName()));
            InputStream fis = new FileInputStream(file1);
            byte[] uploadByte = null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            uploadByte = bos.toByteArray();
            //页面模板
            ExcelConverter2 ec = new ExcelConverter2();
            byte[] xmlByte = ec.fromExcelFileByteArrayToXml(new FileInputStream(file1)).getBytes();
//            byte[] uploadByte = file1.getBytes();
            TemplateExcelByteData excelByteData = new TemplateExcelByteData();

            excelByteData.setObjectId(baseDAO.getSequenceNextValue(TemplateExcelByteData.class));

            excelByteData.setXmlFileData(xmlByte);
            excelByteData.setUploadFileData(uploadByte);
            eemTempEntity.setTemplateExcelByteData(excelByteData);
            //数据模板
            eemTempEntity.setPrimitiveName(file2.getName());
            String prefix = file2.getName().substring(file2.getName().lastIndexOf(".") + 1);
            eemTempEntity.setSuffix(prefix);
            eemTempEntity.setRelativePath("collect/"+eemTempEntity.getTempName() + "." + prefix);
//            file2.transferTo(new File(EemConstants.RELATIVE_PATH + File.separator + eemTempEntity.getTempName() + "." + prefix));
            eemTempEntity.setObjectId(idsMap.get(file1.getName()));
            baseDAO.saveOrUpdate(eemTempEntity, userEntity);
        } catch (DAOException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateReportRelID() {
        try {
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=80 where OBJECT_ID=1");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=81 where OBJECT_ID=2");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=82 where OBJECT_ID=3");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=83 where OBJECT_ID=4");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=84 where OBJECT_ID=5");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=85 where OBJECT_ID=6");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=86 where OBJECT_ID=7");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=85 where OBJECT_ID=8");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=87 where OBJECT_ID=9");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=88 where OBJECT_ID=10");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=89 where OBJECT_ID=11");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=90 where OBJECT_ID=12");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=91 where OBJECT_ID=13");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=92 where OBJECT_ID=14");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=91 where OBJECT_ID=15");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=93 where OBJECT_ID=16");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=94 where OBJECT_ID=17");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=95 where OBJECT_ID=18");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=96 where OBJECT_ID=19");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=97 where OBJECT_ID=20");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=98 where OBJECT_ID=21");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=99 where OBJECT_ID=22");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=99 where OBJECT_ID=23");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=100 where OBJECT_ID=24");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=101 where OBJECT_ID=25");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=102 where OBJECT_ID=26");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=103 where OBJECT_ID=27");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=103 where OBJECT_ID=28");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=104 where OBJECT_ID=29");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=105 where OBJECT_ID=30");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=106 where OBJECT_ID=31");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=107 where OBJECT_ID=32");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=107 where OBJECT_ID=33");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=108 where OBJECT_ID=34");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=108 where OBJECT_ID=35");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=109 where OBJECT_ID=36");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=110 where OBJECT_ID=37");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=111 where OBJECT_ID=38");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=112 where OBJECT_ID=39");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=113 where OBJECT_ID=40");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=114 where OBJECT_ID=41");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=115 where OBJECT_ID=42");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=116 where OBJECT_ID=43");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=117 where OBJECT_ID=44");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=118 where OBJECT_ID=45");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=119 where OBJECT_ID=46");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=120 where OBJECT_ID=47");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=121 where OBJECT_ID=48");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=122 where OBJECT_ID=49");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=123 where OBJECT_ID=50");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=124 where OBJECT_ID=51");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=125 where OBJECT_ID=52");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=126 where OBJECT_ID=53");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=127 where OBJECT_ID=54");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=128 where OBJECT_ID=55");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=129 where OBJECT_ID=56");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=130 where OBJECT_ID=57");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=131 where OBJECT_ID=58");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=132 where OBJECT_ID=59");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=133 where OBJECT_ID=60");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=134 where OBJECT_ID=61");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=135 where OBJECT_ID=62");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=136 where OBJECT_ID=63");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=137 where OBJECT_ID=64");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=138 where OBJECT_ID=65");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=139 where OBJECT_ID=66");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=140 where OBJECT_ID=67");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=141 where OBJECT_ID=68");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=142 where OBJECT_ID=69");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=143 where OBJECT_ID=70");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=144 where OBJECT_ID=71");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=145 where OBJECT_ID=72");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=146 where OBJECT_ID=73");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=147 where OBJECT_ID=74");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=148 where OBJECT_ID=75");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=148 where OBJECT_ID=76");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=149 where OBJECT_ID=77");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=150 where OBJECT_ID=78");
            baseDAO.executeSql("update t_eom_temp_info set REL_TEMP_ID=151 where OBJECT_ID=79");
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }
}
