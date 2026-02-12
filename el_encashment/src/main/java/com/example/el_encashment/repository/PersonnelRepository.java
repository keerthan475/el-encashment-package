package com.example.el_encashment.repository;

import com.example.el_encashment.model.Personnel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PersonnelRepository extends JpaRepository<Personnel, Long> {

    List<Personnel> findByNameContainingIgnoreCase(String name);

}
