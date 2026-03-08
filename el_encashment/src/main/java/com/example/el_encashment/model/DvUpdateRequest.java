package com.example.el_encashment.model;

import java.time.LocalDate;
import java.util.List;

public class DvUpdateRequest {
    private List<Long> ids;
    private String dvNo;
    private LocalDate dvDate;
    private Double dvAmount;
    private Double dvBalance;
    private Double recoveryCda;
    private String cdaRemarks;
    private Double recoveryCdaTax;
    private String cdaTaxRemarks;
    public List<Long> getIds() {
        return ids;
    }
    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
    public String getDvNo() {
        return dvNo;
    }
    public void setDvNo(String dvNo) {
        this.dvNo = dvNo;
    }
    public LocalDate getDvDate() {
        return dvDate;
    }
    public void setDvDate(LocalDate dvDate) {
        this.dvDate = dvDate;
    }
    public Double getDvAmount() {
        return dvAmount;
    }
    public void setDvAmount(Double dvAmount) {
        this.dvAmount = dvAmount;
    }
    public Double getDvBalance() {
        return dvBalance;
    }
    public void setDvBalance(Double dvBalance) {
        this.dvBalance = dvBalance;
    }
    public Double getRecoveryCda() {
        return recoveryCda;
    }
    public void setRecoveryCda(Double recoveryCda) {
        this.recoveryCda = recoveryCda;
    }
    public String getCdaRemarks() {
        return cdaRemarks;
    }
    public void setCdaRemarks(String cdaRemarks) {
        this.cdaRemarks = cdaRemarks;
    }
    public Double getRecoveryCdaTax() {
        return recoveryCdaTax;
    }
    public void setRecoveryCdaTax(Double recoveryCdaTax) {
        this.recoveryCdaTax = recoveryCdaTax;
    }
    public String getCdaTaxRemarks() {
        return cdaTaxRemarks;
    }
    public void setCdaTaxRemarks(String cdaTaxRemarks) {
        this.cdaTaxRemarks = cdaTaxRemarks;
    }

    
}
