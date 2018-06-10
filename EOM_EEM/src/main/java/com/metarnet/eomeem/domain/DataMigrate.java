package com.metarnet.eomeem.domain;


import com.metarnet.eomeem.domain.dao.OrgTreeInfo;
import com.metarnet.eomeem.model.ExcelPage;
import com.metarnet.eomeem.service.IEemReportService;
import com.ucloud.paas.proxy.aaaa.AAAAService;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by Administrator on 2017/3/8.
 */
@Controller
public class DataMigrate {

    /**
     * 中国联通	110          安徽省分公司	210           中国联通总部	120
       重庆市分公司	212      北京市分公司	211           湖北省分公司	222
       福建省分公司	213      广东省分公司	215          甘肃省分公司	214
     广西壮族自治区分公司	216       贵州省分公司	217      河北省分公司	218
     河南省分公司	219       海南省分公司	220      黑龙江省分公司	221
     湖南省分公司	223        吉林省分公司	224         江苏省分公司	225
     江西省分公司	226       辽宁省分公司	227         内蒙古自治区分公司	228
     宁夏回族自治区分公司	229       青海省分公司	230     山东省分公司	231
     山西省分公司	232       陕西省分公司	233         上海市分公司	234
     四川省分公司	235       天津市分公司	236         西藏自治区分公司	237
     新疆维吾尔自治区分公司	238       云南省分公司	239      浙江省分公司	240
     香港POP	810       澳门	811     台湾POP	812
     美国POP	813          日本POP	814     新加坡POP	815
     欧洲POP	816       澳大利亚POP	817        联通国际公司	518
     联通系统集成公司	511     联通宽带公司	512        联通信息导航有限公司	513
     联通音乐有限公司	514     联通时科公司	515       联通云数据有限公司	516
     联通智网科技有限公司	517
     中国联通研究院	818
     */
    @Resource
    private IEemReportService eemReportServiceImpl;

    @Resource
    private IEemReportService reportService;

    @Resource
    private OrgTreeInfo treeInfo;



    public static Map<String,String> map = new HashMap<String, String>();

    //excel表格模板对应的id
    public static Map<String,String> tempMap = new HashMap<String, String>();
    static{

        //各个省份对应reportOrgCode
        map.put("中国联通","110");      map.put("中国联通总部","120");     map.put("安徽","210");        map.put("美国","813");
        map.put("北京","211");          map.put("重庆","212");              map.put("福建","213");        map.put("日本","814");
        map.put("甘肃","214");          map.put("广东","215");              map.put("广西","216");        map.put("新加坡","815");
        map.put("贵州","217");          map.put("河北","218");              map.put("河南","219");
        map.put("海南","220");          map.put("黑龙江","221");            map.put("湖北","222");
        map.put("湖南","223");          map.put("吉林","224");              map.put("江苏","225");
        map.put("江西","226");          map.put("辽宁","227");              map.put("内蒙","228");
        map.put("宁夏","229");          map.put("青海","230");              map.put("山东","231");
        map.put("山西","232");          map.put("陕西","233");              map.put("上海","234");
        map.put("四川","235");          map.put("天津","236");              map.put("西藏","237");
        map.put("新疆","238");          map.put("云南","239");              map.put("浙江","240");
        map.put("香港","810");          map.put("澳门","811");              map.put("台湾","812");

        tempMap.put("ADSL上行家庭网关设备质量及售后服务评价表v1.0","1");  tempMap.put("BSC设备后评价表v1.1","2");   tempMap.put("BTS设备后评价表v1.1","3");
        tempMap.put("EPON-FTTB-ONU(盒式)设备运行质量后评价表v3.1","4");  tempMap.put("EPON-FTTB-ONU(非盒式)设备运行质量后评价表v3.1","5");   tempMap.put("EPON-FTTH-ONU设备运行质量后评价表v3.2","6");
        tempMap.put("EPON-OLT设备运行质量后评价表v3.1","7");  tempMap.put("FTTH-ONU设备互通商用情况后评价及问题说明报表v1.4","9");   tempMap.put("GGSN设备后评价表v1.1","10");
        tempMap.put("GPON-FTTB-ONU(盒式)设备运行质量后评价表v3.1","11");  tempMap.put("GPON-FTTB-ONU(非盒式)设备运行质量后评价表v3.1","12");   tempMap.put("GPON-FTTH-ONU设备运行质量后评价表v3.2","13");
        tempMap.put("GPON-OLT设备运行质量后评价报表v3.1","14");  tempMap.put("IP承载网设备v3.1","16");   tempMap.put("LAN上行家庭网关设备质量及售后服务评价表v1.0","17");
        tempMap.put("LTE-FDD设备后评价表20151009","18");  tempMap.put("MSC-SERVER设备后评价表v1.1","19");   tempMap.put("Node-B设备后评价表v1.1","20");
        tempMap.put("OLT设备(含EMS)互通商用情况后评价及问题说明报表v1.4","21");  tempMap.put("OTN服务满意度后评价表v3.2","22");   tempMap.put("OTN设备质量后评价表v3.2","23");
        tempMap.put("RNC设备后评价表v1.1","24");  tempMap.put("SACP设备后评价表v1.1","25");   tempMap.put("SCP设备后评价记录表v1.1","26");
        tempMap.put("SDH服务满意度后评价表v3.3","27");  tempMap.put("SDH设备质量后评价表v3.3","28");   tempMap.put("SGSN设备后评价表v1.1","29");
        tempMap.put("TD-LTE设备后评价表20151009","30");  tempMap.put("UPS后评价季度报表v3.0","31");   tempMap.put("WDM服务满意度后评价表v3.1","32");
        tempMap.put("WDM设备质量后评价表v3.1","33");  tempMap.put("WLAN-AC接入设备后评价报表v3.1","34");   tempMap.put("WLAN-AP接入设备后评价报表v3.2","35");
        tempMap.put("xDSL设备运行质量及售后服务评价季度报表v3.1","36");  tempMap.put("业务路由器(SR)设备后评价表v3.1","37");   tempMap.put("中低端交换机(盒式设备)后评价表v3.1","38");
        tempMap.put("中低端交换机(非盒式设备)后评价表v3.1","39");  tempMap.put("中低端路由器(盒式设备)后评价表v3.1","40");   tempMap.put("中低端路由器(非盒式设备)后评价表v3.1","41");
        tempMap.put("光分路器后评价报表v1.0","42");  tempMap.put("光纤后评价报表v1.3","43");   tempMap.put("光纤配线架后评价报表v1.0","44");
        tempMap.put("光缆接头盒后评价报表v1.1","45");  tempMap.put("光缆测试评价表v1.6","46");   tempMap.put("光缆综合评价表v1.7","47");
        tempMap.put("光跳纤后评价报表v1.1","48");  tempMap.put("卫星服务后评价报表v1.2","49");   tempMap.put("卫星设备质量后评价报表v1.1","50");
        tempMap.put("固定油机后评价报表v1.0","51");  tempMap.put("基站精密空调后评价报表v1.0","52");   tempMap.put("天线后评价报表v1.4","53");
        tempMap.put("室内分布系统后评价报表v1.1","54");  tempMap.put("宽带接入服务器(BRAS)设备后评价表v3.1","55");   tempMap.put("局用信号电缆v1.1","56");
        tempMap.put("局用空调后评价报表v1.0","57");  tempMap.put("市内通信电缆后评价报表v1.1","58");   tempMap.put("开关电源后评价报表v1.0","59");
        tempMap.put("数字通信电缆后评价报表v1.1","60");tempMap.put("数字配线架后评价报表v1.0","61");   tempMap.put("核心路由器(CR)设备后评价表v3.1","62");
        tempMap.put("电力电缆后评价报表v1.0","63");tempMap.put("直放站后评价报表v1.1","64");   tempMap.put("移动核心网设备-HLR设备后评价表v1.1","65");
        tempMap.put("移动核心网设备-MGW设备后评价表v1.1","66");tempMap.put("移动油机后评价报表v1.1","67");   tempMap.put("综合配线架后评价报表v1.0","68");
        tempMap.put("胶体蓄电池后评价报表v1.0","69");tempMap.put("舒适性空调后评价报表v1.0","70");   tempMap.put("蝶形光缆测试记录表v1.6","71");
        tempMap.put("蝶形光缆综合评价表v1.5","72");tempMap.put("设备后评价评分细则-分组承载传送设备(接入)_v2.5","73");   tempMap.put("设备后评价评分细则-分组承载传送设备(核心汇聚)_V1.2","74");
        tempMap.put("边缘SDH-MSTP服务满意度后评价表v3.1","75");tempMap.put("边缘SDH-MSTP设备质量后评价表v3.1","76");   tempMap.put("铅酸蓄电池后评价报表v1.0","77");
        tempMap.put("馈线后评价报表V1.4","78");tempMap.put("高端交换机(SW)设备后评价表v3.1","79");   tempMap.put("IPTV机顶盒质量和售后服务评价报表201608","175");
        tempMap.put("EPON-FTTH-ONU设备售后服务后评价表201608","177");tempMap.put("EPON-OLT和FTTB-ONU设备售后服务后评价表201608","178");   tempMap.put("GPON-FTTH-ONU设备售后服务后评价表201608","179");
        tempMap.put("GPON-OLT和FTTB-ONU设备售后服务后评价表201608","180");}





    @RequestMapping(value = "/dataMigrate.do", params = "method=test")
    @ResponseBody
    public  void test(){
        //File file = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "data");
        File file = new File("E:\\work_project");
        System.out.println(file.getAbsolutePath());
        List<Map<File,List<File>>> fm = rootFiles(file);
        System.out.println(fm);
        for(int i = 0;i < fm.size();i++){
            Map<File,List<File>> m = fm.get(i);
            Set<Map.Entry<File,List<File>>> set = m.entrySet();
            for (Map.Entry<File,List<File>> entry : set) {
                File dir = entry.getKey();
                List<File> filelist = entry.getValue();
                ExcelPage excelPage = new ExcelPage();
                if(dir.getName().contains("第一季度")){
                    excelPage.setReportDate("下半年");
                    excelPage.setReportYear("2015");
                    excelPage.setApplyId(Long.parseLong("1"));
                    //excelPage.setApplyId(Long.parseLong("2"));
                    for(int j = 0;j < filelist.size();j++){
                        //处理每个文件夹下的每个excel文件
                        //result返回值: ok     hasReportedShouldOverride(覆盖)      withdraw(退回后重新上报)        reportDateError(不在上报时间范围内)
                       Object[] obj = this.handleExcel(excelPage,filelist.get(j));
                        this.handleReturnValue(obj,filelist.get(j),excelPage);
                    }
                }else if(dir.getName().contains("第二季度")){
                    excelPage.setReportDate("上半年");
                    excelPage.setReportYear("2016");
                    excelPage.setApplyId(Long.parseLong("1"));
                    //excelPage.setApplyId(Long.parseLong("2"));
                    for(int j = 0;j < filelist.size();j++){
                        //处理每个文件夹下的每个excel文件
                        //result返回值: ok     hasReportedShouldOverride(覆盖)      withdraw(退回后重新上报)        reportDateError(不在上报时间范围内)
                        Object[] obj = this.handleExcel(excelPage,filelist.get(j));
                        this.handleReturnValue(obj,filelist.get(j),excelPage);
                    }
                }
            }
        }
    }

    public void handleReturnValue(Object[] obj,File file,ExcelPage excelPage){
        String result = (String)obj[0];
        UserEntity userEntity = (UserEntity)obj[1];
        //result返回值: ok     hasReportedShouldOverride(覆盖)      withdraw(退回后重新上报)        reportDateError(不在上报时间范围内)
        if(result.equals("ok")){
            byte[] bytes = new byte[(int)file.length()];
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                fis.read(bytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String msg = "";
            try {
                msg = reportService.saveReportData(bytes, excelPage, "", "",userEntity);
            } catch (Exception e) {
                msg = "系统内部运行异常";
                e.printStackTrace();
            }
        }else if(result.equals("hasReportedShouldOverride")){
            byte[] bytes = new byte[(int)file.length()];
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                fis.read(bytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String msg = "";
            try {
                msg = reportService.saveReportData(bytes, excelPage, "", "",userEntity);
            } catch (Exception e) {
                msg = "系统内部运行异常";
                e.printStackTrace();
            }
        }else if(result.equals("withdraw")){

        }else{
            //hasReportedShouldOverride
        }
    }


    public ExcelPage getTpInputID(ExcelPage excelPage,File file){
        if(file.getName().contains("光纤后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("光纤后评价报表v1.3")));
        }else if(file.getName().contains("SCP")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("SCP设备后评价记录表v1.1")));
        }else if(file.getName().contains("电力电缆")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("电力电缆后评价报表v1.0")));
        }else if(file.getName().contains("光分路器")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("光分路器后评价报表v1.0")));
        }else if(file.getName().contains("基站精密空调")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("基站精密空调后评价报表v1.0")));
        }else if(file.getName().contains("胶体蓄电池")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("胶体蓄电池后评价报表v1.0")));
        }else if(file.getName().contains("局用空调")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("局用空调后评价报表v1.0")));
        }else if(file.getName().contains("开关电源")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("开关电源后评价报表v1.0")));
        }else if(file.getName().contains("铅酸蓄电池")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("铅酸蓄电池后评价报表v1.0")));
        }else if(file.getParentFile().getName().contains("打包下载-SDH服务满意度后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("SDH服务满意度后评价表v3.3")));
        }else if(file.getParentFile().getName().contains("打包下载-WDM服务满意度后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("WDM服务满意度后评价表v3.1")));
        }else if(file.getParentFile().getName().contains("打包下载-边缘SDH MSTP服务满意度")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("边缘SDH-MSTP服务满意度后评价表v3.1")));
        }else if(file.getParentFile().getName().contains("打包下载-边缘SDH MSTP设备质量后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("边缘SDH-MSTP设备质量后评价表v3.1")));
        }else if(file.getParentFile().getName().contains("打包下载-固定油机后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("固定油机后评价报表v1.0")));
        }else if(file.getParentFile().getName().contains("光缆接头盒")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("光缆接头盒后评价报表v1.1")));
        }else if(file.getParentFile().getName().contains("打包下载-光跳纤后评价季度报表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("光跳纤后评价报表v1.1")));
        }else if(file.getParentFile().getName().contains("打包下载-光纤配线架后评价季度报表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("光纤配线架后评价报表v1.0")));
        }else if(file.getParentFile().getName().contains("局用信号电缆")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("局用信号电缆v1.1")));
        }else if(file.getParentFile().getName().contains("市内分布系统后评价季度报表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("室内分布系统后评价报表v1.1")));
        }else if(file.getParentFile().getName().contains("舒适性空调后评价季度报表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("舒适性空调后评价报表v1.0")));
        }else if(file.getParentFile().getName().contains("数字配线架后评价季度报表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("数字配线架后评价报表v1.0")));
        }else if(file.getParentFile().getName().contains("数字配线架后评价季度报表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("数字配线架后评价报表v1.0")));
        }else if(file.getParentFile().getName().contains("直放站后评价季度报表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("直放站后评价报表v1.1")));
        }else if(file.getParentFile().getName().contains("中低端交互机(非盒式设备)")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("中低端交换机(非盒式设备)后评价表v3.1")));
        }else if(file.getParentFile().getName().contains("中低端交换机(盒式设备)")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("中低端交换机(盒式设备)")));
        }else if(file.getParentFile().getName().contains("中低端路由器(非盒式设备)")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("中低端路由器(非盒式设备)后评价表v3.1")));
        }else if(file.getParentFile().getName().contains("中低端路由器(盒式设备)")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("中低端路由器(盒式设备)后评价表v3.1")));
        }else if(file.getParentFile().getName().contains("综合配线架后评价季度报表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("综合配线架后评价报表v1.0")));
        }else if(file.getParentFile().getName().contains("ADSL上行家庭网关设备质量及售后服务")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("ADSL上行家庭网关设备质量及售后服务评价表v1.0")));
        }else if(file.getParentFile().getName().contains("FTTH ONU设备互通商用")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("FTTH-ONU设备互通商用情况后评价及问题说明报表v1.4")));
        }else if(file.getParentFile().getName().contains("LAN上行家庭网关设备质量及售后服务")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("LAN上行家庭网关设备质量及售后服务评价表v1.0")));
        }else if(file.getParentFile().getName().contains("LTE-FDD设备后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("LTE-FDD设备后评价表20151009")));
        }else if(file.getParentFile().getName().contains("OLT设备(含EMS)互通商用")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("OLT设备(含EMS)互通商用情况后评价及问题说明报表v1.4")));
        }else if(file.getParentFile().getName().contains("OTN设备质量后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("OTN设备质量后评价表v3.2")));
        }else if(file.getParentFile().getName().contains("TD-LTE设备后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("TD-LTE设备后评价表20151009")));
        }else if(file.getParentFile().getName().contains("UPS后评价季度报表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("UPS后评价季度报表v3.0")));
        }else if(file.getParentFile().getName().contains("WLAN-AC接入设备后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("WLAN-AC接入设备后评价报表v3.1")));
        }else if(file.getParentFile().getName().contains("WLAN-AP接入设备后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("WLAN-AP接入设备后评价报表v3.2")));
        }else if(file.getParentFile().getName().contains("设备后评价评分细则-分组承载(核心汇聚)")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("设备后评价评分细则-分组承载传送设备(核心汇聚)_V1.2")));
        }else if(file.getParentFile().getName().contains("设备评分细则-分组承载传送设备接入")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("设备后评价评分细则-分组承载传送设备(接入)_v2.5")));
        }else if(file.getParentFile().getName().contains("设备评分细则-分组承载传送设备接入")) {
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("设备后评价评分细则-分组承载传送设备(接入)_v2.5")));
        }else if(file.getParentFile().getName().contains("市内通信电缆后评价季度报表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("市内通信电缆后评价报表v1.1")));
        }else if(file.getParentFile().getName().contains("卫星服务后评价表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("卫星服务后评价报表v1.2")));
        }else if(file.getParentFile().getName().contains("卫星设备质量后评价季度报表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("卫星设备质量后评价报表v1.1")));
        }else if(file.getParentFile().getName().contains("移动油机后评价季度报表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("移动油机后评价报表v1.1")));
        }else if(file.getParentFile().getName().contains("BSC设备后评价季度表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("BSC设备后评价表v1.1")));
        }else if(file.getParentFile().getName().contains("BTS后评价季度表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("BTS设备后评价表v1.1")));
        }else if(file.getParentFile().getName().contains("EPON-FTTH ONU设备运行质量")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("EPON-FTTH-ONU设备运行质量后评价表v3.2")));
        }else if(file.getParentFile().getName().contains("EPON设备售后服务后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("EPON-FTTB-ONU(盒式)设备运行质量后评价表v3.1")));
        }else if(file.getParentFile().getName().contains("GGSN设备后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("GGSN设备后评价表v1.1")));
        }else if(file.getParentFile().getName().contains("GPON-FTTH ONU设备运行质量")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("GPON-FTTH-ONU设备运行质量后评价表v3.2")));
        }else if(file.getParentFile().getName().contains("GPON设备售后服务后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("GPON-OLT设备运行质量后评价报表v3.1")));
        }else if(file.getParentFile().getName().contains("MSC SERVER设备后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("MSC-SERVER设备后评价表v1.1")));
        }else if(file.getParentFile().getName().contains("Node-B设备后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("Node-B设备后评价表v1.1")));
        }else if(file.getParentFile().getName().contains("RNC设备后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("RNC设备后评价表v1.1")));
        }else if(file.getParentFile().getName().contains("sacp设备后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("SACP设备后评价表v1.1")));
        }else if(file.getParentFile().getName().contains("scp设备后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("SCP设备后评价记录表v1.1")));
        }else if(file.getParentFile().getName().contains("SGSN设备后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("SGSN设备后评价表v1.1")));
        }else if(file.getParentFile().getName().contains("蝶形光缆综合评价表")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("蝶形光缆综合评价表v1.5")));
        }else if(file.getParentFile().getName().contains("光缆综合评价表")){
           excelPage.setTpInputID(Integer.parseInt(tempMap.get("光缆综合评价表v1.7")));
        }else if(file.getParentFile().getName().contains("馈线后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("馈线后评价报表V1.4")));
        }else if(file.getParentFile().getName().contains("数字通信电缆后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("数字通信电缆后评价报表v1.1")));
        }else if(file.getParentFile().getName().contains("天线后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("天线后评价报表v1.4")));
        }else if(file.getParentFile().getName().contains("移动核心网设备-HLR设备后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("移动核心网设备-HLR设备后评价表v1.1")));
        }else if(file.getParentFile().getName().contains("移动核心网设备-MGW设备后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("移动核心网设备-MGW设备后评价表v1.1")));
        }/*else if(file.getParentFile().getName().contains("节能设备后评价报表")){
            //excelPage.setTpInputID(Integer.parseInt(tempMap.get("WLAN-AP接入设备后评价报表v3.2")));
        }*/else{
            System.out.println("@@@@@@@");
        }


            //节能设备后评价报表


        return excelPage;
    }




    public Object[] handleExcel(ExcelPage excelPage,File file){
        Object[] obj = new Object[2];
        //result返回值: ok     hasReportedShouldOverride(覆盖)      withdraw(退回后重新上报)        reportDateError(不在上报时间范围内)
        String result = "";
       /* if(file.getName().contains("光纤后评价")){
            excelPage.setTpInputID(Integer.parseInt(tempMap.get("光纤后评价报表v1.3")));
        }*/
        excelPage = this.getTpInputID(excelPage,file);
        UserEntity user = null;
        Set<String> ms = map.keySet();
        Iterator<String> item = ms.iterator();
        while(item.hasNext()){
            String pro = item.next();
            if(file.getName().contains(pro)){
                String orgReportCode = map.get(pro);
                OrgEntity org = treeInfo.findOrgInfo(orgReportCode);
                //UserEntity user = new UserEntity();
                user = treeInfo.findUserInfo(org);
                user.setOrgCode(orgReportCode);
                user.setCategory("PRO");
                //OrgEntity org =  user.getOrgEntity();
                if(org == null){
                    org = new OrgEntity();
                    org.setOrgCode(orgReportCode);
                }else {
                    org.setOrgCode(orgReportCode);
                }
                user.setOrgEntity(org);
                break;
            }
        }
        result = eemReportServiceImpl.hasPowerToSave(excelPage,user);
        obj[0] = result;
        obj[1] = user;
        return obj;
    }

   /*
    public  String handleExcel(Map<File,List<File>> m){
        Set<Map.Entry<File,List<File>>> set = m.entrySet();
        for (Map.Entry<File,List<File>> entry : set) {
            File dir = entry.getKey();
            List<File> filelist = entry.getValue();
            ExcelPage excelPage = new ExcelPage();
            if(dir.getName().contains("第一季度")){
                excelPage.setReportDate("下半年");
                excelPage.setReportYear("2105");
                //excelPage.setApplyId(Long.parseLong("1"));
                excelPage.setApplyId(Long.parseLong("2"));
                for(int i = 0;i < filelist.size();i++){
                   UserEntity user = new UserEntity();
                   if(filelist.get(i).getName().contains("光纤")){
                       excelPage.setTpInputID(Integer.parseInt(tempMap.get("光纤后评价报表v1.3")));
                       Set<String> ms = map.keySet();
                       Iterator<String> item = ms.iterator();
                       while(item.hasNext()){
                           String pro = item.next();
                           if(filelist.get(i).getName().contains(pro)){
                               String orgReportCode = map.get(pro);
                               user.setOrgCode(orgReportCode);
                               user.setCategory("PRO");
                               OrgEntity org =  user.getOrgEntity();
                               if(org == null){
                                   org = new OrgEntity();
                                   org.setOrgCode(orgReportCode);
                               }else {
                                   org.setOrgCode(orgReportCode);
                               }
                               user.setOrgEntity(org);
                               break;
                           }
                       }
                       result = eemReportServiceImpl.hasPowerToSave(excelPage,user);
                   }
                }
            }else if(dir.getName().contains("第二季度")){
                excelPage.setReportDate("下半年");
                excelPage.setReportYear("2105");
                //excelPage.setApplyId(Long.parseLong("1"));
                excelPage.setApplyId(Long.parseLong("2"));
                for(int i = 0;i < filelist.size();i++){
                    UserEntity user = new UserEntity();
                    if(filelist.get(i).getName().contains("光纤")){
                        excelPage.setTpInputID(Integer.parseInt(tempMap.get("光纤后评价报表v1.3")));
                        Set<String> ms = map.keySet();
                        Iterator<String> item = ms.iterator();
                        while(item.hasNext()){
                            String pro = item.next();
                            if(filelist.get(i).getName().contains(pro)){
                                String orgReportCode = map.get(pro);
                                user.setOrgCode(orgReportCode);
                                user.setCategory("PRO");
                                OrgEntity org =  user.getOrgEntity();
                                if(org == null){
                                    org = new OrgEntity();
                                    org.setOrgCode(orgReportCode);
                                }else {
                                    org.setOrgCode(orgReportCode);
                                }
                                user.setOrgEntity(org);
                                break;
                            }
                        }
                       result = eemReportServiceImpl.hasPowerToSave(excelPage,user);
                    }
                }
            }
        }
                        return result;
    }

*/

    /**
     *
     * @param file
     * @return
     */
    public static List<Map<File,List<File>>> rootFiles(File file){
        List<Map<File,List<File>>> lm = new ArrayList<Map<File,List<File>>>();
        File[] files = file.listFiles();
        for(int i = 0;i < files.length;i++){
            if(files[i].exists() && files[i].isDirectory()&&files[i].getName().contains("打包下载")){
                Map<File,List<File>> fileMap = new HashMap<File, List<File>>();
                List<File> filelist = iteratorFiles(files[i]);
                if(filelist != null){
                    fileMap.put(files[i],filelist);
                }
                lm.add(fileMap);
            }
        }
        return lm;
    }

    private static List<File> iteratorFiles(File file){
        List<File> list = null;
        if(file.getName().contains("打包下载")){
            File[] files = file.listFiles();
            if(files.length > 0){
                list = new ArrayList<File>();
                for(int i = 0;i < files.length;i++){
                    list.add(files[i]);
                }
            }
        }
        return list;
    }




}