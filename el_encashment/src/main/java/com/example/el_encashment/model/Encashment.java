package com.example.el_encashment.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "EL_ENCASHMENT")
public class Encashment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EMP_ID")
    private Long empId;

    @ManyToOne
    @JoinColumn(name = "EMP_ID", insertable = false, updatable = false)
    private Personnel personnel;
    
    private String purpose;
    private String doPartNumber;
    private LocalDate doPartDate;
    private LocalDate eventDate;
    private String blockPeriod;

    
    private int elDays;
    private int hplDays;

    private double elAmount;
    private double hplAmount;
    private double totalAmount;

    private double itAmount;
    private double eduCess;
    private double itRecovery;

    private double otherRecovery;
    private String otherRemark;
    private double otherTaxable;
    private String otherTaxableRemark;

    private double grandTotal;

    private LocalDate createdDate = LocalDate.now();

    //add bill no.
    private String billNo;
    private LocalDate billDate;

    //add dv no.
    private String dvNo;
    private LocalDate dvDate;
    private Double dvAmount;
    private Double dvBalance;
    private Double recoveryCda;
    private String cdaRemarks;
    private Double recoveryCdaTax;
    private String cdaTaxRemarks;
    
    //add MRO No.
    private String dvNoDiff;
    private LocalDate dvDateDiff;
    private Double dvAmountDiff;

    private String dvNoDiffPay;
    private LocalDate dvDateDiffPay;
    private Double dvAmountDiffPay;
    private Double sumDvAmount;

    private String mroNo;
    private LocalDate mroDate;
    private Double mroAmount;

    

    public Encashment() {}

    // getters & setters below
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

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getBlockPeriod() {
        return blockPeriod;
    }

    public void setBlockPeriod(String blockPeriod) {
        this.blockPeriod = blockPeriod;
    }

    public Long getId() { return id; }
    public Long getEmpId() { return empId; }
    public void setEmpId(Long empId) { this.empId = empId; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public int getElDays() { return elDays; }
    public void setElDays(int elDays) { this.elDays = elDays; }

    public int getHplDays() { return hplDays; }
    public void setHplDays(int hplDays) { this.hplDays = hplDays; }

    public double getElAmount() { return elAmount; }
    public void setElAmount(double elAmount) { this.elAmount = elAmount; }

    public double getHplAmount() { return hplAmount; }
    public void setHplAmount(double hplAmount) { this.hplAmount = hplAmount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getItAmount() { return itAmount; }
    public void setItAmount(double itAmount) { this.itAmount = itAmount; }

    public double getEduCess() { return eduCess; }
    public void setEduCess(double eduCess) { this.eduCess = eduCess; }

    public double getItRecovery() { return itRecovery; }
    public void setItRecovery(double itRecovery) { this.itRecovery = itRecovery; }

    public double getOtherRecovery() { return otherRecovery; }
    public void setOtherRecovery(double otherRecovery) { this.otherRecovery = otherRecovery; }

    public double getOtherTaxable() { return otherTaxable; }
    public void setOtherTaxable(double otherTaxable) { this.otherTaxable = otherTaxable; }

    public String getOtherRemark() { return otherRemark; }
    public void setOtherRemark(String otherRemark) { this.otherRemark = otherRemark;}

    public String getOtherTaxableRemark() { return otherTaxableRemark; }
    public void setOtherTaxableRemark(String otherTaxableRemark) { this.otherTaxableRemark = otherTaxableRemark; }

    public double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(double grandTotal) { this.grandTotal = grandTotal; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

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

    


    public String getDvNoDiff() {
        return dvNoDiff;
    }

    public void setDvNoDiff(String dvNoDiff) {
        this.dvNoDiff = dvNoDiff;
    }

    public LocalDate getDvDateDiff() {
        return dvDateDiff;
    }

    public void setDvDateDiff(LocalDate dvDateDiff) {
        this.dvDateDiff = dvDateDiff;
    }

    public Double getDvAmountDiff() {
        return dvAmountDiff;
    }

    public void setDvAmountDiff(Double dvAmountDiff) {
        this.dvAmountDiff = dvAmountDiff;
    }

    public String getDvNoDiffPay() {
        return dvNoDiffPay;
    }

    public void setDvNoDiffPay(String dvNoDiffPay) {
        this.dvNoDiffPay = dvNoDiffPay;
    }

    public LocalDate getDvDateDiffPay() {
        return dvDateDiffPay;
    }

    public void setDvDateDiffPay(LocalDate dvDateDiffPay) {
        this.dvDateDiffPay = dvDateDiffPay;
    }

    public Double getDvAmountDiffPay() {
        return dvAmountDiffPay;
    }

    public void setDvAmountDiffPay(Double dvAmountDiffPay) {
        this.dvAmountDiffPay = dvAmountDiffPay;
    }

    public Double getSumDvAmount() {
        return sumDvAmount;
    }

    public void setSumDvAmount(Double sumDvAmount) {
        this.sumDvAmount = sumDvAmount;
    }

    public String getMroNo() {
        return mroNo;
    }

    public void setMroNo(String mroNo) {
        this.mroNo = mroNo;
    }

    public LocalDate getMroDate() {
        return mroDate;
    }

    public void setMroDate(LocalDate mroDate) {
        this.mroDate = mroDate;
    }

    public Double getMroAmount() {
        return mroAmount;
    }

    public void setMroAmount(Double mroAmount) {
        this.mroAmount = mroAmount;
    }
    
}
