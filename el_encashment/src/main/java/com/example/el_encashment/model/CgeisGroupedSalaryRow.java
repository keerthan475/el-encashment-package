package com.example.el_encashment.model;

import java.time.LocalDate;

public class CgeisGroupedSalaryRow {
    private LocalDate fromMonth;
    private LocalDate toMonth;
    private Double cgeis;
    private Double value;

    public CgeisGroupedSalaryRow(LocalDate fromMonth, LocalDate toMonth, Double cgeis, Double value) {
        this.fromMonth = fromMonth;
        this.toMonth = toMonth;
        this.cgeis = cgeis;
        this.value = value;
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
}
