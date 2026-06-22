package com.example.el_encashment.controller;

import com.example.el_encashment.model.Personnel;
import com.example.el_encashment.repository.PersonnelRepository;
import com.example.el_encashment.service.RetirementService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/personnel")
@CrossOrigin
public class PersonnelController {

    private final PersonnelRepository personnelRepository;
    private final RetirementService retirementService;

    public PersonnelController(
        PersonnelRepository personnelRepository,
        RetirementService retirementService
    ) {
        this.personnelRepository = personnelRepository;
        this.retirementService = retirementService;
    }

    @GetMapping("/all")
    public List<Personnel> getAllPersonnel() {
        return personnelRepository.findAll();
    }

    @GetMapping("/search")
    public List<Personnel> searchPersonnel(
        @RequestParam(defaultValue = "") String name,
        @RequestParam(defaultValue = "all") String category
    ) {
        if ("all".equalsIgnoreCase(category)) {
            return personnelRepository.searchByName(name);
        }

        if ("officer".equalsIgnoreCase(category)) {
            return personnelRepository.searchByNameAndDisgTypeIn(name, List.of(1));
        }

        return personnelRepository.searchByNameAndDisgTypeIn(name, List.of(2, 3));
    }

    @GetMapping("/{id}/retirement-date")
    public LocalDate getRetirementDate(@PathVariable Long id) {
        Optional<Personnel> personnelOpt = personnelRepository.findById(id);

        if (personnelOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Personnel not found");
        }

        LocalDate dob = personnelOpt.get().getDob();
        if (dob == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date of birth is missing for this employee");
        }
        return retirementService.calculateRetirementDate(dob);
    }
}
