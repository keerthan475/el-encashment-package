package com.example.el_encashment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "CGEIS_BILL_ITEM")
public class CgeisBillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cgeis_bill_item_seq")
    @SequenceGenerator(name = "cgeis_bill_item_seq", sequenceName = "CGEIS_BILL_ITEM_SEQ", allocationSize = 1)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BILL_ID", nullable = false)
    private CgeisBill bill;

    @Column(name = "FROM_MONTH", nullable = false)
    private LocalDate fromMonth;

    @Column(name = "TO_MONTH", nullable = false)
    private LocalDate toMonth;

    @Column(name = "CGEIS", nullable = false)
    private Double cgeis;

    @Column(name = "VALUE_AMOUNT", nullable = false)
    private Double value;

    @Column(name = "CGEIS_VALUE", nullable = false)
    private Double legacyValue;

    @Column(name = "TIMES", nullable = false)
    private Integer times;

    @Column(name = "LINE_AMOUNT", nullable = false)
    private Double lineAmount;

    public Long getId() {
        return id;
    }

    public CgeisBill getBill() {
        return bill;
    }

    public void setBill(CgeisBill bill) {
        this.bill = bill;
    }

    public LocalDate getFromMonth() {
        return fromMonth;
    }

    public void setFromMonth(LocalDate fromMonth) {
        this.fromMonth = fromMonth;
    }

    public LocalDate getToMonth() {
        return toMonth;
    }

    public void setToMonth(LocalDate toMonth) {
        this.toMonth = toMonth;
    }

    public Double getCgeis() {
        return cgeis;
    }

    public void setCgeis(Double cgeis) {
        this.cgeis = cgeis;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
        this.legacyValue = value;
    }

    public Double getLegacyValue() {
        return legacyValue;
    }

    public void setLegacyValue(Double legacyValue) {
        this.legacyValue = legacyValue;
        if (this.value == null) {
            this.value = legacyValue;
        }
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    public Double getLineAmount() {
        return lineAmount;
    }

    public void setLineAmount(Double lineAmount) {
        this.lineAmount = lineAmount;
    }
}
