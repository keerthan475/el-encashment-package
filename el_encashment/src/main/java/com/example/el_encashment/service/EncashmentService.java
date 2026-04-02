package com.example.el_encashment.service;

import com.example.el_encashment.model.DvUpdateRequest;
import com.example.el_encashment.model.Encashment;
import com.example.el_encashment.repository.EncashmentRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EncashmentService {

    private final EncashmentRepository repository;

    public EncashmentService(EncashmentRepository repository) {
        this.repository = repository;
    }

    public Encashment save(Encashment encashment) {
        validatePreparedRecord(encashment);
        encashment.setBillNo(null);
        encashment.setBillDate(null);
        encashment.setDvNo(null);
        encashment.setDvDate(null);
        encashment.setDvAmount(null);
        encashment.setDvBalance(null);
        encashment.setRecoveryCda(null);
        encashment.setCdaRemarks(null);
        encashment.setRecoveryCdaTax(null);
        encashment.setCdaTaxRemarks(null);
        encashment.setDvNoDiff(null);
        encashment.setDvDateDiff(null);
        encashment.setDvAmountDiff(null);
        encashment.setDvNoDiffPay(null);
        encashment.setDvDateDiffPay(null);
        encashment.setDvAmountDiffPay(null);
        encashment.setSumDvAmount(null);
        encashment.setMroNo(null);
        encashment.setMroDate(null);
        encashment.setMroAmount(null);
        return repository.save(encashment);
    }

    public Encashment getPreparedRecord(Long id) {
        return repository.findDetailedById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
    }

    public Encashment updatePreparedRecord(Long id, Encashment request) {
        Encashment existing = getPreparedRecord(id);

        if (existing.getBillNo() != null && !existing.getBillNo().isBlank()) {
            throw badRequest("Only records without Bill No can be updated from Prepare Bill");
        }

        request.setId(id);
        validatePreparedRecord(request);
        copyPreparedFields(existing, request);
        return repository.save(existing);
    }

    public void deletePreparedRecord(Long id) {
        Encashment existing = getPreparedRecord(id);

        if (existing.getBillNo() != null && !existing.getBillNo().isBlank()) {
            throw badRequest("Only records without Bill No can be deleted from Prepare Bill");
        }

        repository.delete(existing);
    }

    public List<Encashment> getBillsWithoutBillNo() {
        return repository.findPendingPreparedRecords();
    }

    public List<Encashment> getPendingBills(String category) {
        if (category == null || "all".equals(category)) {
            return repository.findPendingPreparedRecords();
        }

        return repository.getPendingBills(resolveTypes(category));
    }

    public List<Encashment> getBilledRecords(String category) {
        if (category == null || "all".equals(category)) {
            return repository.findBilledRecords();
        }

        return repository.getBilledRecords(resolveTypes(category));
    }

    public void updateBillDetails(List<Long> ids, String billNo, LocalDate billDate) {
        List<Encashment> records = getValidatedRecords(ids);
        String sanitizedBillNo = normalizeBillNo(billNo);
        requireDate(billDate, "Bill date");

        for (Encashment encashment : records) {
            if (encashment.getBillNo() != null && !encashment.getBillNo().isBlank()) {
                throw badRequest("Selected record already has a bill number");
            }

            validateDateSequence(
                encashment.getCreatedDate(),
                billDate,
                "Bill date cannot be before created date"
            );

            encashment.setBillNo(sanitizedBillNo);
            encashment.setBillDate(billDate);
        }

        repository.saveAll(records);
    }

    public List<Encashment> getBillsEligibleForDv() {
        return repository.findPendingDvRecords();
    }

    public List<Encashment> getPendingDv(String category) {
        if (category == null || "all".equals(category)) {
            return getBillsEligibleForDv();
        }

        return repository.getPendingDv(resolveTypes(category));
    }

    public List<Encashment> getDvRecords(String category) {
        if (category == null || "all".equals(category)) {
            return repository.findDvRecords();
        }

        return repository.getDvRecords(resolveTypes(category));
    }

    public void updateDvDetails(List<Long> ids, DvUpdateRequest req) {
        List<Encashment> records = getValidatedRecords(ids);
        String sanitizedDvNo = normalizeDvNo(req.getDvNo());
        requireDate(req.getDvDate(), "DV date");
        validatePositive(req.getDvAmount(), "DV amount");
        validateNonNegative(req.getDvBalance(), "DV balance");
        validateNonNegative(req.getRecoveryCda(), "Recovery CDA");
        validateNonNegative(req.getRecoveryCdaTax(), "Recovery CDA Tax");
        validateRemark(req.getRecoveryCda(), req.getCdaRemarks(), "CDA remarks");
        validateRemark(req.getRecoveryCdaTax(), req.getCdaTaxRemarks(), "CDA tax remarks");

        for (Encashment encashment : records) {
            if (encashment.getBillNo() == null || encashment.getBillNo().isBlank()) {
                throw badRequest("Bill number must be added before DV details");
            }

            if (encashment.getDvNo() != null && !encashment.getDvNo().isBlank()) {
                throw badRequest("Selected record already has DV details");
            }

            validateDateSequence(
                encashment.getBillDate(),
                req.getDvDate(),
                "DV date cannot be before bill date"
            );

            encashment.setDvNo(sanitizedDvNo);
            encashment.setDvDate(req.getDvDate());
            encashment.setDvAmount(req.getDvAmount());
            encashment.setDvBalance(req.getDvBalance());
            encashment.setRecoveryCda(req.getRecoveryCda());
            encashment.setCdaRemarks(trimToNull(req.getCdaRemarks()));
            encashment.setRecoveryCdaTax(req.getRecoveryCdaTax());
            encashment.setCdaTaxRemarks(trimToNull(req.getCdaTaxRemarks()));
            encashment.setDvNoDiff("DV-DIFF-01");
            encashment.setDvDateDiff(LocalDate.now());
            encashment.setDvAmountDiff(100.0);
            encashment.setDvNoDiffPay("DV-DIFFPAY-01");
            encashment.setDvDateDiffPay(LocalDate.now());
            encashment.setDvAmountDiffPay(50.0);

            double dv = encashment.getDvAmount() == null ? 0 : encashment.getDvAmount();
            double diff = encashment.getDvAmountDiff() == null ? 0 : encashment.getDvAmountDiff();
            double diffPay = encashment.getDvAmountDiffPay() == null ? 0 : encashment.getDvAmountDiffPay();
            encashment.setSumDvAmount(dv + diff + diffPay);
        }

        repository.saveAll(records);
    }

    public List<Encashment> getPendingMro() {
        return repository.findPendingMroRecords(
            List.of("Home_Town", "All_India")
        );
    }

    public List<Encashment> getPendingMro(String category) {
        if (category == null || "all".equals(category)) {
            return getPendingMro();
        }

        return repository.getPendingMro(resolveTypes(category));
    }

    public void saveMro(List<Long> ids, String mroNo, LocalDate mroDate, Double mroAmount) {
        List<Encashment> records = getValidatedRecords(ids);
        String sanitizedMroNo = normalizeReferenceCode(mroNo, "MRO No");
        requireDate(mroDate, "MRO date");
        validatePositive(mroAmount, "MRO amount");

        double totalSum = 0;

        for (Encashment encashment : records) {
            if (encashment.getDvNo() == null || encashment.getDvNo().isBlank()) {
                throw badRequest("DV number must be added before MRO details");
            }

            if (encashment.getMroNo() != null && !encashment.getMroNo().isBlank()) {
                throw badRequest("Selected record already has MRO details");
            }

            if (encashment.getDvDate() == null) {
                throw badRequest("DV date missing for record id: " + encashment.getId());
            }

            if (!List.of("Home_Town", "All_India").contains(encashment.getPurpose())) {
                throw badRequest("MRO is allowed only for Home_Town or All_India records");
            }

            validateDateSequence(
                encashment.getDvDate(),
                mroDate,
                "MRO date cannot be before DV date"
            );

            double sum = encashment.getSumDvAmount() == null ? 0 : encashment.getSumDvAmount();
            if (sum <= 0) {
                throw badRequest("Sum DV amount must be available before adding MRO");
            }

            totalSum += sum;

            encashment.setMroNo(sanitizedMroNo);
            encashment.setMroDate(mroDate);
            encashment.setMroAmount(mroAmount);
        }

        if (!sumEquals(totalSum, mroAmount)) {
            throw badRequest("MRO amount must equal the total selected Sum DV Amount");
        }

        repository.saveAll(records);
    }

    public List<Encashment> getMroDetails() {
        return repository.findByMroNoIsNotNullOrderByMroDateDesc();
    }

    public List<Encashment> getMroDetails(String category) {
        if (category == null || "all".equalsIgnoreCase(category)) {
            return getMroDetails();
        }

        return repository.getMroDetails(resolveTypes(category));
    }

    private void validatePreparedRecord(Encashment encashment) {
        if (encashment.getEmpId() == null) {
            throw badRequest("Employee is required");
        }

        String purpose = trimToNull(encashment.getPurpose());
        if (purpose == null) {
            throw badRequest("Purpose is required");
        }

        encashment.setPurpose(purpose);
        requireDate(encashment.getDoPartDate(), "DO Part date");

        LocalDate eventDate = encashment.getEventDate();
        if (eventDate == null) {
            throw badRequest("LTC/Event date is required");
        }

        validateDateSequence(
            encashment.getDoPartDate(),
            eventDate.minusDays(1),
            "DO Part date must be before LTC/Event date"
        );

        String doPartNumber = trimToNull(encashment.getDoPartNumber());
        if (doPartNumber == null) {
            throw badRequest("DO Part number is required");
        }
        encashment.setDoPartNumber(doPartNumber);

        int elDays = encashment.getElDays();
        int hplDays = encashment.getHplDays();

        if (isLtcPurpose(purpose)) {
            if (trimToNull(encashment.getBlockPeriod()) == null) {
                throw badRequest("Block period is required for LTC purpose");
            }

            if (elDays <= 0 || elDays > 10) {
                throw badRequest("EL days must be between 1 and 10 for LTC purpose");
            }

            if (hplDays != 0) {
                throw badRequest("HPL days must be 0 for LTC purpose");
            }
        } else {
            if (elDays < 0 || elDays > 300) {
                throw badRequest("EL days must be between 0 and 300");
            }

            if (hplDays < 0 || hplDays > 300) {
                throw badRequest("HPL days must be between 0 and 300");
            }

            if (elDays + hplDays <= 0 || elDays + hplDays > 300) {
                throw badRequest("EL days + HPL days must be between 1 and 300");
            }

            encashment.setBlockPeriod(null);
        }

        validateNonNegative(encashment.getElAmount(), "EL amount");
        validateNonNegative(encashment.getHplAmount(), "HPL amount");
        validateNonNegative(encashment.getTotalAmount(), "Total amount");
        validateNonNegative(encashment.getItAmount(), "IT amount");
        validateNonNegative(encashment.getEduCess(), "Edu cess");
        validateNonNegative(encashment.getItRecovery(), "IT recovery");
        validateNonNegative(encashment.getOtherRecovery(), "Other recovery");
        validateNonNegative(encashment.getOtherTaxable(), "Other taxable");
        validateNonNegative(encashment.getGrandTotal(), "Grand total");
        validateRemark(encashment.getOtherRecovery(), encashment.getOtherRemark(), "Other recovery remark");
        validateRemark(encashment.getOtherTaxable(), encashment.getOtherTaxableRemark(), "Other taxable remark");

        double expectedTotal = encashment.getElAmount() + encashment.getHplAmount();
        if (!sumEquals(expectedTotal, encashment.getTotalAmount())) {
            throw badRequest("Total amount must equal EL amount + HPL amount");
        }

        double expectedGrandTotal =
            encashment.getTotalAmount()
            - (encashment.getItRecovery() + encashment.getOtherRecovery() + encashment.getOtherTaxable());
        if (!sumEquals(expectedGrandTotal, encashment.getGrandTotal())) {
            throw badRequest("Grand total does not match the calculated deductions");
        }
    }

    private boolean isLtcPurpose(String purpose) {
        return "Home_Town".equals(purpose) || "All_India".equals(purpose);
    }

    private void copyPreparedFields(Encashment target, Encashment source) {
        target.setEmpId(source.getEmpId());
        target.setPurpose(source.getPurpose());
        target.setDoPartNumber(source.getDoPartNumber());
        target.setDoPartDate(source.getDoPartDate());
        target.setEventDate(source.getEventDate());
        target.setBlockPeriod(source.getBlockPeriod());
        target.setElDays(source.getElDays());
        target.setHplDays(source.getHplDays());
        target.setElAmount(source.getElAmount());
        target.setHplAmount(source.getHplAmount());
        target.setTotalAmount(source.getTotalAmount());
        target.setItAmount(source.getItAmount());
        target.setEduCess(source.getEduCess());
        target.setItRecovery(source.getItRecovery());
        target.setOtherRecovery(source.getOtherRecovery());
        target.setOtherRemark(trimToNull(source.getOtherRemark()));
        target.setOtherTaxable(source.getOtherTaxable());
        target.setOtherTaxableRemark(trimToNull(source.getOtherTaxableRemark()));
        target.setGrandTotal(source.getGrandTotal());
    }

    private List<Integer> resolveTypes(String category) {
        if ("officer".equalsIgnoreCase(category)) {
            return List.of(1);
        }

        if ("staff".equalsIgnoreCase(category)) {
            return List.of(2, 3);
        }

        throw badRequest("Invalid category");
    }

    private List<Encashment> getValidatedRecords(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw badRequest("Select at least one record");
        }

        List<Long> cleanIds = new ArrayList<>(ids.stream().distinct().toList());
        List<Encashment> records = repository.findAllById(cleanIds);

        if (records.size() != cleanIds.size()) {
            throw badRequest("One or more selected records were not found");
        }

        return records;
    }

    private String normalizeBillNo(String value) {
        String sanitized = trimToNull(value);
        if (sanitized == null || !sanitized.matches("\\d{3}")) {
            throw badRequest("Bill No must be exactly 3 digits");
        }
        return sanitized;
    }

    private String normalizeDvNo(String value) {
        String sanitized = trimToNull(value);
        if (sanitized == null || !sanitized.matches("\\d{4}")) {
            throw badRequest("DV No must be exactly 4 digits");
        }
        return sanitized;
    }

    private String normalizeReferenceCode(String value, String fieldName) {
        String sanitized = trimToNull(value);

        if (sanitized == null) {
            throw badRequest(fieldName + " is required");
        }

        if (!sanitized.matches("[A-Za-z0-9/-]+")) {
            throw badRequest(fieldName + " can contain only letters, numbers, / and -");
        }

        return sanitized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void requireDate(LocalDate date, String fieldName) {
        if (date == null) {
            throw badRequest(fieldName + " is required");
        }
    }

    private void validateDateSequence(LocalDate start, LocalDate end, String message) {
        if (start != null && end != null && end.isBefore(start)) {
            throw badRequest(message);
        }
    }

    private void validatePositive(Double value, String fieldName) {
        if (value == null || value <= 0) {
            throw badRequest(fieldName + " must be greater than 0");
        }
    }

    private void validateNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw badRequest(fieldName + " cannot be negative");
        }
    }

    private void validateNonNegative(Double value, String fieldName) {
        if (value != null && value < 0) {
            throw badRequest(fieldName + " cannot be negative");
        }
    }

    private void validateRemark(double amount, String remark, String fieldName) {
        if (amount > 0 && trimToNull(remark) == null) {
            throw badRequest(fieldName + " is required");
        }
    }

    private void validateRemark(Double amount, String remark, String fieldName) {
        if (amount != null && amount > 0 && trimToNull(remark) == null) {
            throw badRequest(fieldName + " is required");
        }
    }

    private boolean sumEquals(double first, double second) {
        return Math.abs(first - second) < 0.01;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
