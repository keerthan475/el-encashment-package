package com.example.el_encashment.model;

import java.time.LocalDate;

public class CgeisBillSummary {
    private final Long id;
    private final String billNo;
    private final LocalDate billDate;
    private final String doPartNumber;
    private final LocalDate doPartDate;
    private final String dvNo;
    private final LocalDate dvDate;
    private final Double dvAmount;
    private final String reason;
    private final Double totalAmount;
    private final Double insuranceCoverage;
    private final Double otherRecovery;
    private final Double netPay;
    private final String remarks;
    private final Personnel personnel;

    public CgeisBillSummary(CgeisBill bill) {
        this.id = bill.getId();
        this.billNo = bill.getBillNo();
        this.billDate = bill.getBillDate();
        this.doPartNumber = bill.getDoPartNumber();
        this.doPartDate = bill.getDoPartDate();
        this.dvNo = bill.getDvNo();
        this.dvDate = bill.getDvDate();
        this.dvAmount = bill.getDvAmount();
        this.reason = bill.getReason();
        this.totalAmount = bill.getTotalAmount();
        this.insuranceCoverage = bill.getInsuranceCoverage();
        this.otherRecovery = bill.getOtherRecovery();
        this.netPay = bill.getNetPay();
        this.remarks = bill.getRemarks();
        this.personnel = bill.getPersonnel();
    }

    public Long getId() { return id; }
    public String getBillNo() { return billNo; }
    public LocalDate getBillDate() { return billDate; }
    public String getDoPartNumber() { return doPartNumber; }
    public LocalDate getDoPartDate() { return doPartDate; }
    public String getDvNo() { return dvNo; }
    public LocalDate getDvDate() { return dvDate; }
    public Double getDvAmount() { return dvAmount; }
    public String getReason() { return reason; }
    public Double getTotalAmount() { return totalAmount; }
    public Double getInsuranceCoverage() { return insuranceCoverage; }
    public Double getOtherRecovery() { return otherRecovery; }
    public Double getNetPay() { return netPay; }
    public String getRemarks() { return remarks; }
    public Personnel getPersonnel() { return personnel; }
}
