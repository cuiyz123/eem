package com.metarnet.eomeem.domain.dao;

import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.ucloud.paas.proxy.aaaa.AAAAService;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.*;
import java.util.List;

/**
 * Created by Administrator on 2017/3/10.
 */
@Component
public class OrgTreeInfo {

    @Resource
    private IBaseDAO baseDAO;



    public UserEntity findUserInfo(OrgEntity orgEntity){
        UserEntity user = new UserEntity();
        String sql = "select * from metar_usertable where orgID = "+ orgEntity.getOrgId() +" and user_name like 'root%';";
        Connection conn =   this.getCon();
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while(rs.next()){
                user.setUserId(rs.getLong("user_id"));
                user.setAttribute1(rs.getString("attribute1"));
                user.setAttribute2(rs.getString("attribute2"));
                user.setAttribute3(rs.getString("attribute3"));
                user.setAttribute4(rs.getString("attribute4"));
                user.setAttribute5(rs.getString("attribute5"));
                user.setAddress(rs.getString("address"));
                user.setEmail(rs.getString("email"));
                user.setFax(rs.getString("fax"));
                user.setMobilePhone(rs.getString("mobilePhone"));
                user.setOrgCode(rs.getString("orgCode"));
                user.setOrgID(rs.getLong("orgID"));
                user.setPersonType(rs.getInt("personType"));
                user.setProfessional(rs.getString("professional"));
                user.setRemark(rs.getString("remark"));
                user.setSex(rs.getInt("sex"));
                user.setTelephone(rs.getString("telephone"));
                user.setTrueName(rs.getString("user_truename"));
                user.setUserName(rs.getString("user_name"));
                user.setOrgID(rs.getLong("org_id"));
                user.setCategory(rs.getString("category"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public OrgEntity findOrgInfo(String orgCode){
        OrgEntity orgEntity = new OrgEntity();
        String sql = "select * from metar_orgtable where deleteFlag = 0 and orgCode="+orgCode ;

        Connection conn =   this.getCon();
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while(rs.next()){
                orgEntity.setOrgId(rs.getLong("orgId"));
                orgEntity.setOrgName(rs.getString("orgCode"));
                orgEntity.setOrgName(rs.getString("orgName"));
                orgEntity.setOrgCode(orgCode);
                orgEntity.setAddress(rs.getString("address"));
                orgEntity.setDescription(rs.getString("description"));
                orgEntity.setOrgLevel(rs.getInt("orgLevel"));
                orgEntity.setOrgType(rs.getString("orgType"));
                orgEntity.setParentOrgCode(rs.getString("parentOrgCode"));
                orgEntity.setParentOrgId(rs.getLong("parentOrgId"));
                orgEntity.setProCode(rs.getString("proCode"));
                orgEntity.setProfessional(rs.getString("professional"));
                orgEntity.setRemark(rs.getString("remark"));
                orgEntity.setShortName(rs.getString("shortName"));
                orgEntity.setFullOrgName(rs.getString("fullOrgName"));
                orgEntity.setSortNum(rs.getInt("sortNum"));
                return orgEntity;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }finally{
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    public static void main(String[] args){
        OrgEntity orgEntity = new OrgTreeInfo().findOrgInfo("210");
        UserEntity user = new OrgTreeInfo().findUserInfo(orgEntity);
        System.out.println(orgEntity);
    }


    private Connection getCon() {
        try {
            // 加载MySql的驱动类
            Class.forName("com.mysql.jdbc.Driver");
            //测试
           // String url = "jdbc:mysql://10.249.6.34:3306/paoos_adb?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false";
            //生产
            String url = "jdbc:mysql://10.162.66.3:3306/eom_paoos_adb?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false";
            String username = "metarnet";
            String password = "Metarnet123";
            Connection con = DriverManager.getConnection(url, username, password);
            return con;
        } catch (Exception e) {
            System.out.println("找不到驱动程序类 ，加载驱动失败！");
            e.printStackTrace();
            return null;
        }
    }





}
