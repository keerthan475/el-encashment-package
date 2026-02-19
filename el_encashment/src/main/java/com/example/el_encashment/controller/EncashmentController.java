package com.example.el_encashment.controller;

import com.example.el_encashment.model.Encashment;
import com.example.el_encashment.service.EncashmentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/encashment")
@CrossOrigin
public class EncashmentController {

    private final EncashmentService service;

    public EncashmentController(EncashmentService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public Encashment save(@RequestBody Encashment encashment) {
        return service.save(encashment);
    }
}
