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
            e.setDvNoDiff("DV-DIFF-01");
            e.setDvDateDiff(LocalDate.now());
            e.setDvAmountDiff(100.0);

            e.setDvNoDiffPay("DV-DIFFPAY-01");
            e.setDvDateDiffPay(LocalDate.now());
            e.setDvAmountDiffPay(50.0);

            double dv = e.getDvAmount() == null ? 0 : e.getDvAmount();
            double diff = e.getDvAmountDiff() == null ? 0 : e.getDvAmountDiff();
            double diffPay = e.getDvAmountDiffPay() == null ? 0 : e.getDvAmountDiffPay();

            e.setSumDvAmount(dv + diff + diffPay);
        }

        repository.saveAll(records);
    }

    //for table in add mro
    public List<Encashment> getPendingMro() {
        return repository
            .findByDvNoIsNotNullAndMroNoIsNullAndPurposeInOrderByCreatedDateDesc(
                List.of("Home_Town","All_India")
            );
    }

    public List<Encashment> getPendingMro(String category){
        if(category == null || category.equals("all")){
            return getPendingMro();
        }

        List<Integer> types;

        if(category.equals("officer")){
            types = List.of(1);
        }else{
            types = List.of(2,3);
        }

        return repository.getPendingMro(types);
    }    

    public void saveMro(List<Long> ids, String mroNo, LocalDate mroDate, Double mroAmount) {

        List<Encashment> list = repository.findAllById(ids);

        for(Encashment e : list) {
            if(e.getDvDate() == null){
                throw new RuntimeException("DV date missing for record id: " + e.getId());
            }            

            if(mroDate.isBefore(e.getDvDate())){
                throw new RuntimeException("MRO date cannot be before DV date");
            }

            double sum = e.getSumDvAmount();

            if(!sumEquals(sum,mroAmount)){
                throw new RuntimeException("MRO amount must equal sum DV amount");
            }

            e.setMroNo(mroNo);
            e.setMroDate(mroDate);
            e.setMroAmount(mroAmount);
        }

        repository.saveAll(list);
    }

    private boolean sumEquals(double a,double b){
        return Math.abs(a-b) < 0.01;
    }

    public List<Encashment> getMroDetails(){
        return repository.findByMroNoIsNotNullOrderByMroDateDesc();
    }


}
