package com.example.el_encashment.service;

import com.example.el_encashment.model.Encashment;
import com.example.el_encashment.repository.EncashmentRepository;
import com.example.el_encashment.model.DvUpdateRequest;

import java.time.LocalDate;
import java.util.List;
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

    public List<Encashment> getBillsWithoutBillNo() {
        return repository.findByBillNoIsNull();
    }

    public void updateBillDetails(List<Long> ids, String billNo, LocalDate billDate) {
        List<Encashment> records = repository.findAllById(ids);

        for (Encashment e : records) {
            e.setBillNo(billNo);
            e.setBillDate(billDate);
        }

        repository.saveAll(records);
    }

    public List<Encashment> getBillsEligibleForDv() {
        return repository.findByBillNoIsNotNullAndDvNoIsNull();
    }

    public void updateDvDetails(List<Long> ids, DvUpdateRequest req) {

        List<Encashment> records = repository.findAllById(ids);

        for (Encashment e : records) {
            e.setDvNo(req.getDvNo());
            e.setDvDate(req.getDvDate());
            e.setDvAmount(req.getDvAmount());
            e.setDvBalance(req.getDvBalance());
            e.setRecoveryCda(req.getRecoveryCda());
            e.setCdaRemarks(req.getCdaRemarks());
            e.setRecoveryCdaTax(req.getRecoveryCdaTax());
            e.setCdaTaxRemarks(req.getCdaTaxRemarks());
        }

        repository.saveAll(records);
    }
}
