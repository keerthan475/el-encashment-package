package com.example.el_encashment.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "FINANCE_DATA")
public class FinanceData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "finance_seq")
    @SequenceGenerator(
            name = "finance_seq",
            sequenceName = "FINANCE_SEQ",
            allocationSize = 1
    )
    private Long id;

    @OneToOne
    @JoinColumn(name = "PERSON_ID")
    @JsonBackReference
    private Personnel personnel;

    @Column(name = "BASIC_PAY")
    private BigDecimal basicPay;

    @Column(name = "SPECIAL_PAY")
    private BigDecimal specialPay;

    @Column(name = "GPF_ACCOUNT_NO")
    private String gpfAccountNo;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public Personnel getPersonnel() {
        return personnel;
    }

    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;
    }

    public BigDecimal getBasicPay() {
        return basicPay;
    }

    public void setBasicPay(BigDecimal basicPay) {
        this.basicPay = basicPay;
    }

    public BigDecimal getSpecialPay() {
        return specialPay;
    }

    public void setSpecialPay(BigDecimal specialPay) {
        this.specialPay = specialPay;
    }

    public String getGpfAccountNo() {
        return gpfAccountNo;
    }

    public void setGpfAccountNo(String gpfAccountNo) {
        this.gpfAccountNo = gpfAccountNo;
    }
}
