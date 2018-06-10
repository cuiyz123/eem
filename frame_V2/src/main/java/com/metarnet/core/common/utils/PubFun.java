package com.metarnet.core.common.utils;


import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.OrgStructure;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公用函数，静态调用
 */
public class PubFun {

    public static final String  ORGLEVEL="orglevel";
    public static final String  BELONGEDPROVINCE="belongedProvince";
    public static final String  BELONGEDPROVINCENAME="belongedProvinceName";
    public static final String  BELONGEDCITY="belongedCity";
    public static final String  BELONGEDCITYNAME="belongedCityName";
    public static final String  ORGFULLPATH="orgfullPath";
    /**
     * 功能:把对象中属性值为空子串的转为null
     *
     * @param obj 要转换的对象
     * @return
     */
    public static Object convertEmptyValueToNull(Object obj) {
        Class c = obj.getClass();
        try {
            Field[] fs = c.getDeclaredFields();
            for (int i = 0; i < fs.length; i++) {
                if (fs[i].getType().toString().equals(String.class.toString())) {
                    String name = fs[i].getName();// 得到当前类中的字段名称,因为只有公有的才能访问
                    String getname = "get"
                            + String.valueOf(name.charAt(0)).toUpperCase()
                            + name.substring(1);// 组合成get方法
                    String setname = "set"
                            + String.valueOf(name.charAt(0)).toUpperCase()
                            + name.substring(1);// 组合成set方法
                    Method getMethod = c.getDeclaredMethod(getname);// 得到该类中所有定义的方法
                    String str = (String) getMethod.invoke(obj);// 取出方法中的返回值，也就是STRUTS把前台的入参放入BEAN中的值
                    if (str != null && str.length() == 0) {
                        Method setMethod = c.getDeclaredMethod(setname,
                                String.class);// 得到set方法的实例
                        setMethod.invoke(obj, new Object[]{null});// 设置set方法的值为null
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return obj;
    }




    /**
     * 获区HIBERNATE的查询属性,方便多个查询,无需视图 如:select a.userName,b.sid,c.dd from InfoUser
     * a,InfoDept b,sh c 输出为:[userName,sid,dd]
     *
     * @param sql
     * @return
     */
    public static String[] getMetaData(String sql) {
        String[] source = sql.split("(?i)from")[0].split("select")[1]
                .split(",");
        String[] dest = new String[source.length];
        for (int i = 0; i < source.length; i++) {
            String[] tmp = source[i].trim().split("\\.")[source[i].trim()
                    .split("\\.").length - 1].split(" ");
            dest[i] = tmp[tmp.length - 1];
        }
        return dest;
    }


    /**
     * 获取 目标对象
     * @param proxy 代理对象
     * @return
     * @throws Exception
     */
    public static Object getTarget(Object proxy) throws Exception {

        if(!AopUtils.isAopProxy(proxy)) {
            return proxy;//不是代理对象
        }

        if(AopUtils.isJdkDynamicProxy(proxy)) {
            return getJdkDynamicProxyTargetObject(proxy);
        } else { //cglib
            return getCglibProxyTargetObject(proxy);
        }



    }


    private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        h.setAccessible(true);
        Object dynamicAdvisedInterceptor = h.get(proxy);

        Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        advised.setAccessible(true);

        Object target = ((AdvisedSupport)advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();

        return target;
    }


    private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) h.get(proxy);

        Field advised = aopProxy.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        Object target = ((AdvisedSupport)advised.get(aopProxy)).getTargetSource().getTarget();

        return target;
    }

    public static Map getOrgInfoByOrgID(int orgID){
        Map infoMap = new HashMap();
        try {
            OrgEntity orgEntity= AAAAAdapter.getInstence().findOrgByOrgID(Long.valueOf(orgID));
            // 组织层级0表示集团，1表示省分，2表示地市
            Integer companyTypeCode = -1;
            OrgStructure orgStructure = AAAAAdapter.getInstence().findCompanysByOrgId(orgEntity.getOrgId().intValue());
            AAAAAdapter.CompanyType companyType = AAAAAdapter.CompanyType.County.getCompanyType(orgStructure);
/*            switch (companyType) {
                case County:
                case City:
                    companyTypeCode = 2;
                    break;
                case Province:
                    companyTypeCode = 1;
                    break;
                case Group:
                    companyTypeCode = 0;
                    break;
            }*/
            companyTypeCode=companyType.getCompanyTypeCode();
            List<OrgEntity> orgList = AAAAAdapter.getInstence().getAsboluteOrgHierarchy(orgEntity.getOrgId().intValue());
            StringBuilder stringBuilder=new StringBuilder();
            for(OrgEntity orgEntity1:orgList){
                stringBuilder.append("/");
                stringBuilder.append(orgEntity1.getOrgId());
            }
            infoMap.put(ORGLEVEL,"0".equals(companyTypeCode.toString())?"1":companyTypeCode.toString());
            // 所属省分
            infoMap.put(BELONGEDPROVINCE,orgStructure.getProvinceCompany()!=null?orgStructure.getProvinceCompany().getOrgId():(orgStructure.getGroupCompany()!=null?orgStructure.getGroupCompany().getOrgId():null));
            //所属省份中文名称
            infoMap.put(BELONGEDPROVINCENAME,orgStructure.getProvinceCompany()!=null?orgStructure.getProvinceCompany().getOrgName():(orgStructure.getGroupCompany()!=null?orgStructure.getGroupCompany().getOrgName():null));
            // 所属地市
            infoMap.put(BELONGEDCITY,orgStructure.getCityCompany()!=null?orgStructure.getCityCompany().getOrgId():null);
            //所属地市中文名称
            infoMap.put(BELONGEDCITYNAME,orgStructure.getCityCompany()!=null?orgStructure.getCityCompany().getOrgName():null);
            //设置处理人的组织代码全路径
            infoMap.put(ORGFULLPATH,stringBuilder.toString());
        }catch (Exception e){
            infoMap=null;
            throw new ServiceException(e);
        }  finally {
            return infoMap;
        }
    }
    public static Map getOrgInfoByUserEntity(UserEntity userEntity) throws ServiceException{
        return getOrgInfoByOrgID(userEntity.getOrgID().intValue());
    }

    public static List<Integer> getAbsoluteOrgHierarchy(OrgEntity orgEntity){
       List list=new ArrayList();
        try {
            List<OrgEntity> orgList = AAAAAdapter.getInstence().getAsboluteOrgHierarchy(orgEntity.getOrgId().intValue());
            for(OrgEntity orgEntity1:orgList){
                list.add(orgEntity1.getOrgId());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    //判断字符串是不是数字
    public static boolean isNumeric(String str){
        for (int i = str.length();--i>=0;){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }
}
