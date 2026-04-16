package com.example.el_encashment.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CGEIS_BILL")
public class CgeisBill {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cgeis_bill_seq")
    @SequenceGenerator(name = "cgeis_bill_seq", sequenceName = "CGEIS_BILL_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "EMP_ID", nullable = false)
    private Long empId;

    @ManyToOne
    @JoinColumn(name = "EMP_ID", insertable = false, updatable = false)
    private Personnel personnel;

    @Column(name = "DO_PART_NUMBER", nullable = false)
    private String doPartNumber;

    @Column(name = "DO_PART_DATE", nullable = false)
    private LocalDate doPartDate;

    @Column(name = "BILL_NO", nullable = false)
    private String billNo;

    @Column(name = "BILL_DATE", nullable = false)
    private LocalDate billDate;

    @Column(name = "TOTAL_AMOUNT", nullable = false)
    private Double totalAmount;

    @Column(name = "IT_AMOUNT", nullable = false)
    private Double itAmount;

    @Column(name = "EDU_CESS", nullable = false)
    private Double eduCess;

    @Column(name = "TOTAL_IT", nullable = false)
    private Double totalIt;

    @Column(name = "NET_PAY", nullable = false)
    private Double netPay;

    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDate createdDate = LocalDate.now();

    @Column(name = "DV_NO")
    private String dvNo;

    @Column(name = "DV_DATE")
    private LocalDate dvDate;

    @Column(name = "DV_AMOUNT")
    private Double dvAmount;

    @Column(name = "DV_BALANCE")
    private Double dvBalance;

    @Column(name = "RECOVERY_CDA")
    private Double recoveryCda;

    @Column(name = "CDA_REMARKS")
    private String cdaRemarks;

    @Column(name = "RECOVERY_CDA_TAX")
    private Double recoveryCdaTax;

    @Column(name = "CDA_TAX_REMARKS")
    private String cdaTaxRemarks;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fromMonth ASC")
    private List<CgeisBillItem> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Long getEmpId() {
        return empId;
    }

    public void setEmpId(Long empId) {
        this.empId = empId;
    }

    public Personnel getPersonnel() {
        return personnel;
    }

    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;
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

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getItAmount() {
        return itAmount;
    }

    public void setItAmount(Double itAmount) {
        this.itAmount = itAmount;
    }

    public Double getEduCess() {
        return eduCess;
    }

    public void setEduCess(Double eduCess) {
        this.eduCess = eduCess;
    }

    public Double getTotalIt() {
        return totalIt;
    }

    public void setTotalIt(Double totalIt) {
        this.totalIt = totalIt;
    }

    public Double getNetPay() {
        return netPay;
    }

    public void setNetPay(Double netPay) {
        this.netPay = netPay;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
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

    public List<CgeisBillItem> getItems() {
        return items;
    }

    public void setItems(List<CgeisBillItem> items) {
        this.items = items;
    }
}
