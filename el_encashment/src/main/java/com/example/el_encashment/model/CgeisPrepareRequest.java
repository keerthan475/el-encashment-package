package com.example.el_encashment.model;

import java.time.LocalDate;
import java.util.List;

public class CgeisPrepareRequest {
    private Long empId;
    private String doPartNumber;
    private LocalDate doPartDate;
    private String billNo;
    private LocalDate billDate;
    private String reason;
    private Double totalAmount;
    private Double insuranceCoverage;
    private Double otherRecovery;
    private String remarks;
    private List<CgeisBillItemRequest> items;

    public Long getEmpId() {
        return empId;
    }

    public void setEmpId(Long empId) {
        this.empId = empId;
    }

    public String getDoPartNumber() {
        return doPartNumber;
    }

    public void setDoPartNumber(String doPartNumber) {
        this.doPartNumber = doPartNumber;
    }

    public LocalDate getDoPartDate() {
        return doPartDate;
    }

    public void setDoPartDate(LocalDate doPartDate) {
        this.doPartDate = doPartDate;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getInsuranceCoverage() {
        return insuranceCoverage;
    }

    public void setInsuranceCoverage(Double insuranceCoverage) {
        this.insuranceCoverage = insuranceCoverage;
    }

    public Double getOtherRecovery() {
        return otherRecovery;
    }

    public void setOtherRecovery(Double otherRecovery) {
        this.otherRecovery = otherRecovery;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<CgeisBillItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CgeisBillItemRequest> items) {
        this.items = items;
    }
}
