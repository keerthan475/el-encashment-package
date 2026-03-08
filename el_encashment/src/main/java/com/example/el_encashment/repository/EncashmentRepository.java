package com.example.el_encashment.repository;

import com.example.el_encashment.model.Encashment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List; 

public interface EncashmentRepository extends JpaRepository<Encashment, Long> {
    List<Encashment> findByBillNoIsNull();
    List<Encashment> findByBillNoIsNotNullAndDvNoIsNull();
    List<Encashment> findByDvNoIsNotNullAndMroNoIsNullAndPurposeInOrderByCreatedDateDesc(
        List<String> purposes
    );

    List<Encashment> findByMroNoIsNotNullOrderByMroDateDesc();

    @Query("""
    SELECT e
    FROM Encashment e
    JOIN e.personnel p
    WHERE e.mroNo IS NULL
    AND e.dvNo IS NOT NULL
    AND e.purpose IN ('Home_Town','All_India')
    AND p.disgType IN :types
    ORDER BY e.createdDate DESC
    """)
    List<Encashment> getPendingMro(List<Integer> types);
}
