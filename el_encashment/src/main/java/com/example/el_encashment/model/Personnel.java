package com.example.el_encashment.model;

import jakarta.persistence.*;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "PERSONNEL")
public class Personnel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "personnel_seq")
    @SequenceGenerator(
            name = "personnel_seq",
            sequenceName = "PERSONNEL_SEQ",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "EMP_CODE", unique = true)
    private String empCode;

    private String name;

    private String division;

    private LocalDate dob;

    @OneToOne(mappedBy = "personnel", cascade = CascadeType.ALL)
    @JsonManagedReference
    private FinanceData financeData;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public FinanceData getFinanceData() {
        return financeData;
    }

    public void setFinanceData(FinanceData financeData) {
        this.financeData = financeData;
    }
}
