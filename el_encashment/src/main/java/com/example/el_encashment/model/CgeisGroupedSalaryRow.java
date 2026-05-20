package com.example.el_encashment.model;

import java.time.LocalDate;

public class CgeisGroupedSalaryRow {
    private LocalDate fromMonth;
    private LocalDate toMonth;
    private Double cgeis;
    private Double value;
    private Integer times;

    public CgeisGroupedSalaryRow(LocalDate fromMonth, LocalDate toMonth, Double cgeis, Double value, Integer times) {
        this.fromMonth = fromMonth;
        this.toMonth = toMonth;
        this.cgeis = cgeis;
        this.value = value;
        this.times = times;
    }

    public LocalDate getFromMonth() {
        return fromMonth;
    }

    public LocalDate getToMonth() {
        return toMonth;
    }

    public Double getCgeis() {
        return cgeis;
    }

    public Double getValue() {
        return value;
    }

    public Integer getTimes() {
        return times;
    }
}
