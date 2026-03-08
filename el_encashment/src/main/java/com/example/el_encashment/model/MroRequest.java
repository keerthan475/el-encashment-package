package com.example.el_encashment.model;
import java.time.LocalDate;
import java.util.List;;

public class MroRequest {

    private List<Long> ids;
    private String mroNo;
    private LocalDate mroDate;
    private Double mroAmount;
    
    public List<Long> getIds() {
        return ids;
    }
    public void setIds(List<Long> ids) {
        this.ids = ids;
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
    

    // getters setters
    
}
