package com.metarnet.eomeem.model;

import javax.persistence.Id;
import javax.persistence.Table;

//评分表
@javax.persistence.Entity
@Table(name = "EVALUATION_REFERENCE")
public class EvaluationReference {
	private double maxFaultrate;
	private double minFaultrate;
	private String formName;
	private String standby1;
	private String standby2;
	private String standby3;
	private String standby4;

	@Id
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	public double getMaxFaultrate() {
		return maxFaultrate;
	}
	public void setMaxFaultrate(double maxFaultrate) {
		this.maxFaultrate = maxFaultrate;
	}
	public double getMinFaultrate() {
		return minFaultrate;
	}
	public void setMinFaultrate(double minFaultrate) {
		this.minFaultrate = minFaultrate;
	}
	public String getStandby1() {
		return standby1;
	}
	public void setStandby1(String standby1) {
		this.standby1 = standby1;
	}
	public String getStandby2() {
		return standby2;
	}
	public void setStandby2(String standby2) {
		this.standby2 = standby2;
	}
	public String getStandby3() {
		return standby3;
	}
	public void setStandby3(String standby3) {
		this.standby3 = standby3;
	}
	public String getStandby4() {
		return standby4;
	}
	public void setStandby4(String standby4) {
		this.standby4 = standby4;
	}


}
