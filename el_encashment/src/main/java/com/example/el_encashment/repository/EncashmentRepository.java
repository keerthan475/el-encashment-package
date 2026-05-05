package com.example.el_encashment.repository;

import com.example.el_encashment.model.Encashment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EncashmentRepository extends JpaRepository<Encashment, Long> {

    @Query("""
    SELECT DISTINCT e
    FROM Encashment e
    LEFT JOIN FETCH e.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE e.billNo IS NULL
    ORDER BY e.createdDate DESC, e.id DESC
    """)
    List<Encashment> findPendingPreparedRecords();

    @Query("""
    SELECT DISTINCT e
    FROM Encashment e
    LEFT JOIN FETCH e.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE e.billNo IS NOT NULL
    ORDER BY e.billDate DESC, e.createdDate DESC, e.id DESC
    """)
    List<Encashment> findBilledRecords();

    @Query("""
    SELECT DISTINCT e
    FROM Encashment e
    LEFT JOIN FETCH e.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE e.billNo = :billNo
    ORDER BY e.billDate DESC, e.createdDate DESC, e.id DESC
    """)
    List<Encashment> findDetailedByBillNo(String billNo);

    @Query("""
    SELECT DISTINCT e
    FROM Encashment e
    LEFT JOIN FETCH e.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE e.billNo IS NOT NULL
    AND e.dvNo IS NULL
    ORDER BY e.billDate DESC, e.createdDate DESC, e.id DESC
    """)
    List<Encashment> findPendingDvRecords();

    @Query("""
    SELECT DISTINCT e
    FROM Encashment e
    LEFT JOIN FETCH e.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE e.dvNo IS NOT NULL
    ORDER BY e.dvDate DESC, e.createdDate DESC, e.id DESC
    """)
    List<Encashment> findDvRecords();

    @Query("""
    SELECT DISTINCT e
    FROM Encashment e
    LEFT JOIN FETCH e.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE e.id = :id
    """)
    Optional<Encashment> findDetailedById(Long id);

    @Query("""
    SELECT DISTINCT e
    FROM Encashment e
    LEFT JOIN FETCH e.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE e.dvNo IS NOT NULL
    AND e.mroNo IS NULL
    AND e.purpose IN :purposes
    ORDER BY e.createdDate DESC, e.id DESC
    """)
    List<Encashment> findPendingMroRecords(List<String> purposes);

    @Query("""
    SELECT DISTINCT e
    FROM Encashment e
    LEFT JOIN FETCH e.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE e.mroNo IS NOT NULL
    ORDER BY e.mroDate DESC, e.id DESC
    """)
    List<Encashment> findByMroNoIsNotNullOrderByMroDateDesc();

    @Query("""
    SELECT DISTINCT e
    FROM Encashment e
    JOIN FETCH e.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE e.billNo IS NULL
    AND p.disgType IN :types
    ORDER BY e.createdDate DESC, e.id DESC
    """)
    List<Encashment> getPendingBills(List<Integer> types);

    @Query("""
    SELECT DISTINCT e
    FROM Encashment e
    JOIN FETCH e.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE e.billNo IS NOT NULL
    AND p.disgType IN :types
    ORDER BY e.billDate DESC, e.createdDate DESC, e.id DESC
    """)
    List<Encashment> getBilledRecords(List<Integer> types);

    @Query("""
    SELECT DISTINCT e
    FROM Encashment e
    JOIN FETCH e.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE e.billNo IS NOT NULL
    AND e.dvNo IS NULL
    AND p.disgType IN :types
    ORDER BY e.billDate DESC, e.createdDate DESC, e.id DESC
    """)
    List<Encashment> getPendingDv(List<Integer> types);

    @Query("""
    SELECT DISTINCT e
    FROM Encashment e
    JOIN FETCH e.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE e.dvNo IS NOT NULL
    AND p.disgType IN :types
    ORDER BY e.dvDate DESC, e.createdDate DESC, e.id DESC
    """)
    List<Encashment> getDvRecords(List<Integer> types);

    @Query("""
    SELECT DISTINCT e
    FROM Encashment e
    JOIN FETCH e.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE e.mroNo IS NULL
    AND e.dvNo IS NOT NULL
    AND e.purpose IN ('Home_Town','All_India')
    AND p.disgType IN :types
    ORDER BY e.createdDate DESC, e.id DESC
    """)
    List<Encashment> getPendingMro(List<Integer> types);

    @Query("""
    SELECT DISTINCT e
    FROM Encashment e
    JOIN FETCH e.personnel p
    LEFT JOIN FETCH p.financeData
    WHERE e.mroNo IS NOT NULL
    AND p.disgType IN :types
    ORDER BY e.mroDate DESC, e.id DESC
    """)
    List<Encashment> getMroDetails(List<Integer> types);
}
