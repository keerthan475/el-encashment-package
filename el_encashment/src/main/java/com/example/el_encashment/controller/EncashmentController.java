package com.example.el_encashment.controller;

import com.example.el_encashment.model.BillUpdateRequest;
import com.example.el_encashment.model.DvUpdateRequest;
import com.example.el_encashment.model.Encashment;
import com.example.el_encashment.model.MroRequest;
import com.example.el_encashment.service.EncashmentService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/{id}")
    public Encashment getById(@PathVariable Long id) {
        return service.getPreparedRecord(id);
    }

    @PutMapping("/{id}")
    public Encashment update(@PathVariable Long id, @RequestBody Encashment encashment) {
        return service.updatePreparedRecord(id, encashment);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deletePreparedRecord(id);
    }

    @GetMapping("/pending-bills")
    public List<Encashment> getPendingBills(@RequestParam(defaultValue = "all") String category) {
        return service.getPendingBills(category);
    }

    @GetMapping("/billed-records")
    public List<Encashment> getBilledRecords(@RequestParam(defaultValue = "all") String category) {
        return service.getBilledRecords(category);
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
    public List<Encashment> getPendingDv(@RequestParam(defaultValue = "all") String category) {
        return service.getPendingDv(category);
    }

    @GetMapping("/dv-records")
    public List<Encashment> getDvRecords(@RequestParam(defaultValue = "all") String category) {
        return service.getDvRecords(category);
    }

    @PostMapping("/add-dv-no")
    public void updateDv(@RequestBody DvUpdateRequest req) {
        service.updateDvDetails(req.getIds(), req);
    }

    @GetMapping("/pending-mro")
    public List<Encashment> getPendingMro(@RequestParam(required = false) String category) {
        return service.getPendingMro(category);
    }

    @PostMapping("/add-mro")
    public void addMro(@RequestBody MroRequest req) {
        service.saveMro(
            req.getIds(),
            req.getMroNo(),
            req.getMroDate(),
            req.getMroAmount()
        );
    }

    @GetMapping("/mro-details")
    public List<Encashment> getMroDetails(
        @RequestParam(defaultValue = "all") String category
    ) {
        return service.getMroDetails(category);
    }

    @GetMapping("/reports")
    public List<Encashment> getReportRecords(@RequestParam(defaultValue = "all") String category) {
        return service.getReportRecords(category);
    }

    @GetMapping(value = "/bill-report/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getBillReport(@PathVariable Long id) {
        return ResponseEntity.ok(service.buildBillReportHtml(id));
    }

    @GetMapping(value = "/it-report", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getItReport(@RequestParam String billNo) {
        return ResponseEntity.ok(service.buildItScheduleHtml(billNo));
    }
}
