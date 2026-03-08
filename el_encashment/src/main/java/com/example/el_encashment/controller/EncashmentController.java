package com.example.el_encashment.controller;

import com.example.el_encashment.model.Encashment;
import com.example.el_encashment.service.EncashmentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.el_encashment.model.BillUpdateRequest;
import com.example.el_encashment.model.DvUpdateRequest;
import com.example.el_encashment.model.MroRequest;

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

    @GetMapping("/pending-bills")
    public List<Encashment> getPendingBills() {
        return service.getBillsWithoutBillNo();
    }

    @PostMapping("/add-bill-no")
    public void addBillNo(@RequestBody BillUpdateRequest request) {
        service.updateBillDetails(
            request.getIds(),
            request.getBillNo(),
            request.getBillDate()
        );
    }

    @GetMapping("/pending-dv")
    public List<Encashment> getBillsWithBillNo() {
        return service.getBillsEligibleForDv();
    }

    @PostMapping("/add-dv-no")
    public void updateDv(@RequestBody DvUpdateRequest req) {
        service.updateDvDetails(req.getIds(), req);
    }

    @GetMapping("/pending-mro")
    public List<Encashment> getPendingMro(
            @RequestParam(required=false) String category){

        return service.getPendingMro(category);
    }

    @PostMapping("/add-mro")
    public void addMro(@RequestBody MroRequest req){
        service.saveMro(
            req.getIds(),
            req.getMroNo(),
            req.getMroDate(),
            req.getMroAmount()
        );
    }

    @GetMapping("/mro-details")
    public List<Encashment> getMroDetails(){
        return service.getMroDetails();
    }
}
