package com.example.el_encashment.model;

import java.time.LocalDate;
import java.util.List;

public class BillUpdateRequest {

    private List<Long> ids;
    private String billNo;
    private LocalDate billDate;

    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }

    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }

    public LocalDate getBillDate() { return billDate; }
    public void setBillDate(LocalDate billDate) { this.billDate = billDate; }
}

