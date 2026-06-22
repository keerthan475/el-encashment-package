package com.example.el_encashment.controller;

import com.example.el_encashment.model.CgeisBillSummary;
import com.example.el_encashment.model.CgeisDvUpdateRequest;
import com.example.el_encashment.model.CgeisGroupedSalaryRow;
import com.example.el_encashment.model.CgeisPrepareRequest;
import com.example.el_encashment.model.Salary;
import com.example.el_encashment.model.SalaryRangeRequest;
import com.example.el_encashment.service.CgeisService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cgeis")
@CrossOrigin
public class CgeisController {

    private final CgeisService service;

    public CgeisController(CgeisService service) {
        this.service = service;
    }

    @GetMapping("/salary/{empId}")
    public List<Salary> getSalaryByEmpId(@PathVariable Long empId) {
        return service.getSalaryByEmpId(empId);
    }

    @GetMapping("/salary-grouped/{empId}")
    public List<CgeisGroupedSalaryRow> getGroupedSalaryByEmpId(@PathVariable Long empId) {
        return service.getGroupedSalaryByEmpId(empId);
    }

    @PostMapping("/salary")
    public void addSalaryRange(@RequestBody SalaryRangeRequest request) {
        service.addSalaryRange(request);
    }

    @PutMapping("/salary")
    public void updateSalaryRange(@RequestBody SalaryRangeRequest request) {
        service.updateSalaryRange(request);
    }

    @DeleteMapping("/salary")
    public void deleteSalaryRange(@RequestBody SalaryRangeRequest request) {
        service.deleteSalaryRange(request);
    }

    @PostMapping("/bill")
    public void createBill(@RequestBody CgeisPrepareRequest request) {
        service.createBill(request);
    }

    @GetMapping("/bill-history/{empId}")
    public List<CgeisBillSummary> getBillHistory(@PathVariable Long empId) {
        return service.getBillsByEmpId(empId);
    }

    @GetMapping("/pending-dv")
    public List<CgeisBillSummary> getPendingDv(@RequestParam(defaultValue = "all") String category) {
        return service.getPendingDv(category);
    }

    @GetMapping("/processed-dv")
    public List<CgeisBillSummary> getProcessedDv(@RequestParam(defaultValue = "all") String category) {
        return service.getProcessedDv(category);
    }

    @PostMapping("/dv")
    public void updateDv(@RequestBody CgeisDvUpdateRequest request) {
        service.updateDvDetails(request.getIds(), request);
    }

    @GetMapping("/reports")
    public List<CgeisBillSummary> getReportBills(@RequestParam(defaultValue = "all") String category) {
        return service.getReportBills(category);
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
