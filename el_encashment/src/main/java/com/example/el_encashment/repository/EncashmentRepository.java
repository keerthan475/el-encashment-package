package com.example.el_encashment.repository;

import com.example.el_encashment.model.Encashment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncashmentRepository extends JpaRepository<Encashment, Long> {
}
