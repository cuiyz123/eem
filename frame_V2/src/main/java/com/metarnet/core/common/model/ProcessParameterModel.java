package com.metarnet.core.common.model;

import java.util.HashSet;
import java.util.Set;

/**
 * 流程五个维度对象
*  User: wshan
 * Date: 13-4-22
 * Time: 下午17:10
 */
public class ProcessParameterModel {

	private Set<String> areacode = new HashSet<String>() ;//区域名称
	private Set<String> orgcode = new HashSet<String>();//组织名称
	private Set<String> majorcode = new HashSet<String>();//专业名称
	private Set<String> productcode = new HashSet<String>();//产品名称
//	private Set<String> roleclass = new HashSet<String>();//角色名称
	
	public Set<String> getAreacode() {
		return areacode;
	}
	/**
	 * 区域名称
	 * @param areacode
	 */
	public void setAreacode(Set<String> areacode) {
		this.areacode = areacode;
	}
	public Set<String> getOrgcode() {
		return orgcode;
	}
	/**
	 * 组织名称
	 * @param orgcode
	 */
	public void setOrgcode(Set<String> orgcode) {
		this.orgcode = orgcode;
	}
	public Set<String> getMajorcode() {
		return majorcode;
	}
	/**
	 * 专业名称
	 * @param majorcode
	 */
	public void setMajorcode(Set<String> majorcode) {
		this.majorcode = majorcode;
	}
	public Set<String> getProductcode() {
		return productcode;
	}
	/**
	 * 产品名称
	 * @return
	 */
	public void setProductcode(Set<String> productcode) {
		this.productcode = productcode;
	}
	/*public Set<String> getRoleclass() {
		return roleclass;
	}
	*//**
	 * 角色名称
	 * @param roleclass
	 *//*
	public void setRoleclass(Set<String> roleclass) {
		this.roleclass = roleclass;
	}*/
	
}
