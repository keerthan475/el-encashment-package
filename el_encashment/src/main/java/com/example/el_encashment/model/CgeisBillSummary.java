package com.example.el_encashment.model;

import java.time.LocalDate;

public class CgeisBillSummary {
    private final Long id;
    private final String billNo;
    private final LocalDate billDate;
    private final String dvNo;
    private final LocalDate dvDate;
    private final Double dvAmount;
    private final Double dvBalance;
    private final Double totalAmount;
    private final Double totalIt;
    private final Double netPay;
    private final Personnel personnel;

    public CgeisBillSummary(CgeisBill bill) {
        this.id = bill.getId();
        this.billNo = bill.getBillNo();
        this.billDate = bill.getBillDate();
        this.dvNo = bill.getDvNo();
        this.dvDate = bill.getDvDate();
        this.dvAmount = bill.getDvAmount();
        this.dvBalance = bill.getDvBalance();
        this.totalAmount = bill.getTotalAmount();
        this.totalIt = bill.getTotalIt();
        this.netPay = bill.getNetPay();
        this.personnel = bill.getPersonnel();
    }

    public Long getId() { return id; }
    public String getBillNo() { return billNo; }
    public LocalDate getBillDate() { return billDate; }
    public String getDvNo() { return dvNo; }
    public LocalDate getDvDate() { return dvDate; }
    public Double getDvAmount() { return dvAmount; }
    public Double getDvBalance() { return dvBalance; }
    public Double getTotalAmount() { return totalAmount; }
    public Double getTotalIt() { return totalIt; }
    public Double getNetPay() { return netPay; }
    public Personnel getPersonnel() { return personnel; }
}
