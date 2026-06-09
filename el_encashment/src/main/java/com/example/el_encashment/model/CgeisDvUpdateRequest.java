package com.example.el_encashment.model;

import java.time.LocalDate;
import java.util.List;

public class CgeisDvUpdateRequest {
    private List<Long> ids;
    private String dvNo;
    private LocalDate dvDate;
    private Double dvAmount;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
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
}
