package com.metarnet.eomeem.vo;

/**
 * Created by Administrator on 2016/7/14.
 */
public class AnalysisVo {
    private String provinceName;//省分
    private String provinceCode;//省分
    private String reportPerson;//上报人
    private String reportTel;//联系电话
    private Double timelyRate;//及时率
    private Double momTimelyRate;//环比及时率
    private Double accuracyRate;//准确率
    private Double momAccuracyRate;//环比准确率

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getReportPerson() {
        return reportPerson;
    }

    public void setReportPerson(String reportPerson) {
        this.reportPerson = reportPerson;
    }

    public String getReportTel() {
        return reportTel;
    }

    public void setReportTel(String reportTel) {
        this.reportTel = reportTel;
    }

    public Double getTimelyRate() {
        return timelyRate;
    }

    public void setTimelyRate(Double timelyRate) {
        this.timelyRate = timelyRate;
    }

    public Double getMomTimelyRate() {
        return momTimelyRate;
    }

    public void setMomTimelyRate(Double momTimelyRate) {
        this.momTimelyRate = momTimelyRate;
    }

    public Double getAccuracyRate() {
        return accuracyRate;
    }

    public void setAccuracyRate(Double accuracyRate) {
        this.accuracyRate = accuracyRate;
    }

    public Double getMomAccuracyRate() {
        return momAccuracyRate;
    }

    public void setMomAccuracyRate(Double momAccuracyRate) {
        this.momAccuracyRate = momAccuracyRate;
    }
}
