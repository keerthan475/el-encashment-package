package com.example.el_encashment.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "SALARY",
    uniqueConstraints = @UniqueConstraint(columnNames = {"EMP_ID", "MONTH_YEAR"})
)
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "salary_seq")
    @SequenceGenerator(name = "salary_seq", sequenceName = "SALARY_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "EMP_ID", nullable = false)
    private Long empId;

    @ManyToOne
    @JoinColumn(name = "EMP_ID", insertable = false, updatable = false)
    private Personnel personnel;

    @Column(name = "MONTH_YEAR", nullable = false)
    private LocalDate monthYear;

    @Column(name = "CGEIS", nullable = false)
    private Double cgeis;

    @Column(name = "VALUE_AMOUNT")
    private Double value;

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

    public LocalDate getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(LocalDate monthYear) {
        this.monthYear = monthYear;
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
}
