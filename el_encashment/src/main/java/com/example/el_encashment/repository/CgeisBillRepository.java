package com.example.el_encashment.repository;

import com.example.el_encashment.model.CgeisBill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CgeisBillRepository extends JpaRepository<CgeisBill, Long> {

    @Query("""
    SELECT DISTINCT b
    FROM CgeisBill b
    LEFT JOIN FETCH b.personnel p
    LEFT JOIN FETCH p.financeData
    ORDER BY b.billDate DESC, b.id DESC
    """)
    List<CgeisBill> findAllDetailed();

    @Query("""
    SELECT DISTINCT b
    FROM CgeisBill b
    LEFT JOIN FETCH b.personnel p
    LEFT JOIN FETCH p.financeData
    LEFT JOIN FETCH b.items
    WHERE b.empId = :empId
    ORDER BY b.createdDate DESC, b.id DESC
    """)
    List<CgeisBill> findDetailedByEmpId(Long empId);

    @Query("""
    SELECT DISTINCT b
    FROM CgeisBill b
    LEFT JOIN FETCH b.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE b.dvNo IS NULL
    ORDER BY b.billDate DESC, b.id DESC
    """)
    List<CgeisBill> findPendingDv();

    @Query("""
    SELECT DISTINCT b
    FROM CgeisBill b
    LEFT JOIN FETCH b.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE b.dvNo IS NOT NULL
    ORDER BY b.dvDate DESC, b.id DESC
    """)
    List<CgeisBill> findProcessedDv();

    @Query("""
    SELECT DISTINCT b
    FROM CgeisBill b
    LEFT JOIN FETCH b.personnel p
    LEFT JOIN FETCH p.financeData
    LEFT JOIN FETCH b.items
    WHERE b.id = :id
    """)
    Optional<CgeisBill> findDetailedById(Long id);

    @Query("""
    SELECT DISTINCT b
    FROM CgeisBill b
    LEFT JOIN FETCH b.personnel p
    LEFT JOIN FETCH p.financeData
    LEFT JOIN FETCH b.items
    WHERE b.billNo = :billNo
    ORDER BY b.id ASC
    """)
    List<CgeisBill> findDetailedByBillNo(String billNo);

    @Query("""
    SELECT DISTINCT b
    FROM CgeisBill b
    LEFT JOIN FETCH b.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE p.disgType IN :types
    ORDER BY b.billDate DESC, b.id DESC
    """)
    List<CgeisBill> findAllByDisgType(List<Integer> types);

    @Query("""
    SELECT DISTINCT b
    FROM CgeisBill b
    LEFT JOIN FETCH b.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE b.dvNo IS NULL
    AND p.disgType IN :types
    ORDER BY b.billDate DESC, b.id DESC
    """)
    List<CgeisBill> findPendingDvByDisgType(List<Integer> types);

    @Query("""
    SELECT DISTINCT b
    FROM CgeisBill b
    LEFT JOIN FETCH b.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE b.dvNo IS NOT NULL
    AND p.disgType IN :types
    ORDER BY b.dvDate DESC, b.id DESC
    """)
    List<CgeisBill> findProcessedDvByDisgType(List<Integer> types);
}
