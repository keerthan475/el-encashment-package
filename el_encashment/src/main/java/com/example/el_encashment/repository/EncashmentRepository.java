package com.example.el_encashment.repository;

import com.example.el_encashment.model.Encashment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; 

public interface EncashmentRepository extends JpaRepository<Encashment, Long> {
    List<Encashment> findByBillNoIsNull();
    List<Encashment> findByBillNoIsNotNullAndDvNoIsNull();
}
