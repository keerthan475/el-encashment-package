package com.example.el_encashment.repository;

import com.example.el_encashment.model.Personnel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PersonnelRepository extends JpaRepository<Personnel, Long> {

    @Query("""
    SELECT p
    FROM Personnel p
    LEFT JOIN FETCH p.financeData
    WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
    ORDER BY p.name ASC
    """)
    List<Personnel> searchByName(String name);

    @Query("""
    SELECT p
    FROM Personnel p
    LEFT JOIN FETCH p.financeData
    WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
    AND p.disgType IN :types
    ORDER BY p.name ASC
    """)
    List<Personnel> searchByNameAndDisgTypeIn(String name, List<Integer> types);
}
