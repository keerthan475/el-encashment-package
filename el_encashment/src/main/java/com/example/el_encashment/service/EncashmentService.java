package com.example.el_encashment.service;

import com.example.el_encashment.model.Encashment;
import com.example.el_encashment.repository.EncashmentRepository;
import org.springframework.stereotype.Service;

@Service
public class EncashmentService {

    private final EncashmentRepository repository;

    public EncashmentService(EncashmentRepository repository) {
        this.repository = repository;
    }

    public Encashment save(Encashment encashment) {
        return repository.save(encashment);
    }
}
