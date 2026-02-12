package com.example.el_encashment.controller;

import com.example.el_encashment.model.Personnel;
import com.example.el_encashment.repository.PersonnelRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.el_encashment.service.RetirementService;
import java.time.LocalDate;
import java.util.Optional;


@RestController
@RequestMapping("/api/personnel")
@CrossOrigin
public class PersonnelController {

    private final PersonnelRepository personnelRepository;
    private final RetirementService retirementService;

    public PersonnelController(PersonnelRepository personnelRepository,
                            RetirementService retirementService) {
        this.personnelRepository = personnelRepository;
        this.retirementService = retirementService;
    }


    @GetMapping("/all")
    public List<Personnel> getAllPersonnel() {
        return personnelRepository.findAll();
    }

    @GetMapping("/search")
    public List<Personnel> searchPersonnel(@RequestParam String name) {
        return personnelRepository.findByNameContainingIgnoreCase(name);
    }

    @GetMapping("/{id}/retirement-date")
    public LocalDate getRetirementDate(@PathVariable Long id) {

        Optional<Personnel> personnelOpt = personnelRepository.findById(id);

        if (personnelOpt.isEmpty()) {
            throw new RuntimeException("Personnel not found");
        }

        LocalDate dob = personnelOpt.get().getDob();

        return retirementService.calculateRetirementDate(dob);
    }

}
