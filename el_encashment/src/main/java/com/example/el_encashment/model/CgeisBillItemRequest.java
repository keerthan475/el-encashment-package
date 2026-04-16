package com.example.el_encashment.model;

import java.time.LocalDate;

public class CgeisBillItemRequest {
    private LocalDate fromMonth;
    private LocalDate toMonth;
    private Double cgeis;
    private Double value;
    private Integer times;

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
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }
}
