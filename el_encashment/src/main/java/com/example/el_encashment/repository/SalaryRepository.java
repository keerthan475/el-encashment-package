package com.example.el_encashment.repository;

import com.example.el_encashment.model.Salary;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SalaryRepository extends JpaRepository<Salary, Long> {

    @Query("""
    SELECT s
    FROM Salary s
    LEFT JOIN FETCH s.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE s.empId = :empId
    ORDER BY s.monthYear ASC
    """)
    List<Salary> findByEmpIdOrderByMonthYearAsc(Long empId);

    boolean existsByEmpIdAndMonthYear(Long empId, LocalDate monthYear);

    void deleteByEmpIdAndMonthYearBetween(Long empId, LocalDate fromMonth, LocalDate toMonth);
}
