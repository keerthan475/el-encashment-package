package com.example.el_encashment.service;

import com.example.el_encashment.model.DvUpdateRequest;
import com.example.el_encashment.model.Encashment;
import com.example.el_encashment.model.FinanceData;
import com.example.el_encashment.model.Personnel;
import com.example.el_encashment.repository.EncashmentRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EncashmentService {

    private static final DateTimeFormatter REPORT_DATE = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

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

    public List<Encashment> getReportRecords(String category) {
        return getBilledRecords(category);
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

    public String buildBillReportHtml(Long id) {
        Encashment encashment = getPreparedRecord(id);
        if (encashment.getBillNo() == null || encashment.getBillNo().isBlank()) {
            throw badRequest("Bill number is not available for this record");
        }

        Personnel personnel = encashment.getPersonnel();
        FinanceData finance = personnel == null ? null : personnel.getFinanceData();
        double basicPay = finance != null && finance.getBasicPay() != null ? finance.getBasicPay().doubleValue() : 0;
        double specialPay = finance != null && finance.getSpecialPay() != null ? finance.getSpecialPay().doubleValue() : 0;
        double totalPay = basicPay + specialPay;

        String claimLabel = claimLabel(encashment.getPurpose());
        String amountWords = amountInWords(Math.round(encashment.getGrandTotal()));
        String otherRecoveryLine = formatOtherRecoveryLine(encashment);

        return """
            <!doctype html>
            <html>
            <head>
              <meta charset="UTF-8" />
              <title>EL Encashment Bill Report</title>
              <style>
                body { font-family: "Times New Roman", serif; margin: 24px; color: #111; }
                .page { max-width: 980px; margin: 0 auto; }
                .center { text-align: center; }
                .meta { display:flex; justify-content:space-between; align-items:flex-start; margin-top: 8px; font-size:14px; }
                .lead { margin: 18px 0; font-size: 15px; line-height: 1.5; }
                .details { display:grid; grid-template-columns: 1fr 1fr; gap: 6px 24px; font-size:14px; margin-bottom: 20px; }
                .amount-box { width: 420px; margin-top: 18px; }
                .amount-row { display:flex; justify-content:space-between; padding: 4px 0; font-size:14px; }
                .formula { margin: 12px 0 10px; font-size:14px; }
                .summary { margin-top: 16px; width: 520px; }
                .summary-row { display:flex; justify-content:space-between; padding: 3px 0; font-size:14px; }
                .netpay { margin-top: 10px; border-top:1px solid #111; padding-top:8px; font-weight:bold; }
                .rupees { margin-top: 18px; font-size: 14px; }
                .encl { margin-top: 18px; font-size:14px; }
                .footer { display:flex; justify-content:space-between; margin-top: 44px; font-size:14px; }
                .num { min-width: 120px; text-align: right; }
                @media print { body { margin: 8mm; } }
              </style>
            </head>
            <body>
              <div class="page">
                <div class="center">
                  <div><strong>DEFENCE RESEARCH & DEVELOPMENT LABORATORY</strong></div>
                  <div>KANCHANBAGH, HYDERABAD - 58</div>
                </div>

                <div class="meta">
                  <div>
                    <div><strong>Bill No.</strong> %s</div>
                    <div><strong>Bill Date:</strong> %s</div>
                  </div>
                  <div><strong>Unit Code:</strong> DRDL/HYD</div>
                </div>

                <div class="lead">
                  Claim on account of Encashment of leave on <strong>%s</strong> in respect of
                  <strong> %s</strong>, ID No: <strong>%s</strong>, Pers No: <strong>%s</strong>
                  as notified vide DO Part-I No. <strong>%s</strong> dated <strong>%s</strong>.
                </div>

                <div class="details">
                  <div><strong>Event Date:</strong> %s</div>
                  <div><strong>Purpose:</strong> %s</div>
                  <div><strong>PF A/C No:</strong> %s</div>
                  <div><strong>Division:</strong> %s</div>
                  <div><strong>Category:</strong> %s</div>
                  <div><strong>Block Period:</strong> %s</div>
                  <div><strong>Basic Pay:</strong> %.2f</div>
                  <div><strong>Special Pay:</strong> %.2f</div>
                  <div><strong>Total Pay:</strong> %.2f</div>
                  <div><strong>EL Days:</strong> %d</div>
                  <div><strong>HPL Days:</strong> %d</div>
                  <div><strong>IT Recovery:</strong> %.2f</div>
                </div>

                <div class="amount-box">
                  <div class="formula"><strong>EL Amount</strong> <span class="num">%.2f</span></div>
                  <div class="formula"><strong>HPL Amount</strong> <span class="num">%.2f</span></div>
                </div>

                <div class="summary">
                  <div class="summary-row"><span>EL Amount</span><span class="num">Rs. %.2f</span></div>
                  <div class="summary-row"><span>HPL Amount</span><span class="num">Rs. %.2f</span></div>
                  <div class="summary-row"><span>IT Recovery</span><span class="num">Rs. %.2f</span></div>
                  <div class="summary-row"><span>Income Tax</span><span class="num">Rs. %.2f</span></div>
                  <div class="summary-row"><span>Educational Cess : 4%% on Income Tax</span><span class="num">Rs. %.2f</span></div>
                  <div class="summary-row"><span>%s</span><span class="num">Rs. %.2f</span></div>
                  <div class="summary-row netpay"><span>NET PAY</span><span class="num">Rs. %.2f</span></div>
                </div>

                <div class="rupees"><strong>Rupees:</strong> %s only.</div>

                <div class="encl">
                  <div>Encl:-</div>
                  <div>1) DO Part-I No. %s dated %s</div>
                  <div>2) Certificate of Sanction</div>
                  <div>3) Certificate</div>
                </div>

                <div style="margin-top:12px; font-size:14px;">
                  The income tax on the bill will be consolidated along with income tax of other bills and the same will be recovered subsequently in remaining regular pay bills of the financial year.
                </div>

                <div class="footer">
                  <div></div>
                  <div class="center">
                    <div><strong>Sr. ACCOUNTS OFFICER</strong></div>
                    <div>FOR DIRECTOR</div>
                  </div>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
            safe(encashment.getBillNo()),
            formatDate(encashment.getBillDate()),
            claimLabel,
            safe(personnel == null ? null : personnel.getName()),
            safe(personnel == null ? null : personnel.getEmpCode()),
            safe(personnel == null ? null : personnel.getEmpCode()),
            safe(encashment.getDoPartNumber()),
            formatDate(encashment.getDoPartDate()),
            formatDate(encashment.getEventDate()),
            safe(encashment.getPurpose()),
            safe(finance == null ? null : finance.getGpfAccountNo()),
            safe(personnel == null ? null : personnel.getDivision()),
            categoryLabel(personnel == null ? null : personnel.getDisgType()),
            safe(encashment.getBlockPeriod()),
            basicPay,
            specialPay,
            totalPay,
            encashment.getElDays(),
            encashment.getHplDays(),
            encashment.getItRecovery(),
            encashment.getElAmount(),
            encashment.getHplAmount(),
            encashment.getElAmount(),
            encashment.getHplAmount(),
            encashment.getItRecovery(),
            encashment.getItAmount(),
            encashment.getEduCess(),
            otherRecoveryLine,
            encashment.getOtherRecovery() + encashment.getOtherTaxable(),
            encashment.getGrandTotal(),
            amountWords,
            safe(encashment.getDoPartNumber()),
            formatDate(encashment.getDoPartDate())
        );
    }

    public String buildItScheduleHtml(String billNo) {
        String sanitizedBillNo = trimToNull(billNo);
        if (sanitizedBillNo == null) {
            throw badRequest("Bill No is required");
        }

        List<Encashment> records = repository.findDetailedByBillNo(sanitizedBillNo);
        if (records.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No EL encashment records found for Bill No");
        }

        StringBuilder rows = new StringBuilder();
        double totalIncomeTax = 0;
        double totalEduCess = 0;
        double totalOverall = 0;
        int slNo = 1;

        for (Encashment encashment : records) {
            Personnel personnel = encashment.getPersonnel();
            FinanceData finance = personnel == null ? null : personnel.getFinanceData();
            rows.append("""
                <tr>
                  <td>%d</td>
                  <td>%s</td>
                  <td>%s</td>
                  <td>%s<br/>%s</td>
                  <td class="num">%.2f</td>
                  <td class="num">%.2f</td>
                  <td class="num">%.2f</td>
                </tr>
                """.formatted(
                slNo++,
                safe(finance == null ? null : finance.getGpfAccountNo()),
                "-",
                safe(personnel == null ? null : personnel.getName()),
                categoryLabel(personnel == null ? null : personnel.getDisgType()),
                encashment.getItAmount(),
                encashment.getEduCess(),
                encashment.getItAmount() + encashment.getEduCess()
            ));
            totalIncomeTax += encashment.getItAmount();
            totalEduCess += encashment.getEduCess();
            totalOverall += encashment.getItAmount() + encashment.getEduCess();
        }

        return """
            <!doctype html>
            <html>
            <head>
              <meta charset="UTF-8" />
              <title>EL Encashment IT Recovery Schedule</title>
              <style>
                body { font-family: "Times New Roman", serif; margin: 24px; color: #111; }
                .page { max-width: 980px; margin: 0 auto; }
                .center { text-align:center; }
                table { width:100%%; border-collapse:collapse; margin-top:20px; }
                th, td { border:1px solid #111; padding:8px; font-size:14px; }
                th { background:#f3f3f3; }
                .num { text-align:right; }
                .footer { display:flex; justify-content:space-between; margin-top:30px; }
              </style>
            </head>
            <body>
              <div class="page">
                <div class="center">
                  <div><strong>UNIT DRDL, HYDERABAD</strong></div>
                  <div><strong>INCOME TAX RECOVERY SCHEDULE</strong></div>
                  <div>(EL - Encashment)</div>
                </div>
                <div class="center" style="margin-top:16px;">
                  RECOVERY SCHEDULE ON ACCOUNT OF INCOME TAX IN RESPECT OF UNDERMENTIONED OFFICERS/STAFF:
                </div>
                <table>
                  <thead>
                    <tr>
                      <th>Sl. No.</th>
                      <th>GPF A/C No</th>
                      <th>PAN Number</th>
                      <th>Name & Designation</th>
                      <th>Income Tax (Rs.)</th>
                      <th>Educational Cess @ 4%% on Income Tax (Rs.)</th>
                      <th>Total Income Tax (Rs.)</th>
                    </tr>
                  </thead>
                  <tbody>
                    %s
                    <tr>
                      <td colspan="4"><strong>Total</strong></td>
                      <td class="num"><strong>%.2f</strong></td>
                      <td class="num"><strong>%.2f</strong></td>
                      <td class="num"><strong>%.2f</strong></td>
                    </tr>
                  </tbody>
                </table>
                <div><strong>Rupees:</strong> %s only.</div>
                <div class="footer">
                  <div></div>
                  <div class="center">
                    <div><strong>ACCOUNTS</strong></div>
                    <div>FOR DIRECTOR</div>
                    <div>DRDL HYD</div>
                  </div>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
            rows,
            round(totalIncomeTax),
            round(totalEduCess),
            round(totalOverall),
            amountInWords(Math.round(totalOverall))
        );
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

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : date.format(REPORT_DATE);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String categoryLabel(Integer type) {
        if (type == null) return "-";
        return type == 1 ? "Officer" : "Staff";
    }

    private String claimLabel(String purpose) {
        if ("Retirement".equalsIgnoreCase(purpose) || "Superannuation".equalsIgnoreCase(purpose)) {
            return "SUPERANNUATION";
        }
        if ("Home_Town".equalsIgnoreCase(purpose) || "All_India".equalsIgnoreCase(purpose)) {
            return "LTC";
        }
        return safe(purpose).toUpperCase(Locale.ENGLISH);
    }

    private String formatOtherRecoveryLine(Encashment encashment) {
        String recoveryRemark = trimToNull(encashment.getOtherRemark());
        String taxableRemark = trimToNull(encashment.getOtherTaxableRemark());
        StringBuilder label = new StringBuilder("Other Recovery");
        if (recoveryRemark != null) {
            label.append(" (").append(recoveryRemark).append(")");
        }
        if (encashment.getOtherTaxable() > 0) {
            label.append(" + Other Recovery Taxable");
            if (taxableRemark != null) {
                label.append(" (").append(taxableRemark).append(")");
            }
        }
        return label.toString();
    }

    private String amountInWords(long number) {
        if (number == 0) {
            return "Zero";
        }

        String[] units = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};

        return convertIndian(number, units, tens).trim().replaceAll("\\s+", " ");
    }

    private String convertIndian(long number, String[] units, String[] tens) {
        if (number < 20) {
            return units[(int) number];
        }
        if (number < 100) {
            return tens[(int) number / 10] + (number % 10 != 0 ? " " + convertIndian(number % 10, units, tens) : "");
        }
        if (number < 1000) {
            return units[(int) number / 100] + " Hundred" + (number % 100 != 0 ? " " + convertIndian(number % 100, units, tens) : "");
        }
        if (number < 100000) {
            return convertIndian(number / 1000, units, tens) + " Thousand" + (number % 1000 != 0 ? " " + convertIndian(number % 1000, units, tens) : "");
        }
        if (number < 10000000) {
            return convertIndian(number / 100000, units, tens) + " Lakh" + (number % 100000 != 0 ? " " + convertIndian(number % 100000, units, tens) : "");
        }
        return convertIndian(number / 10000000, units, tens) + " Crore" + (number % 10000000 != 0 ? " " + convertIndian(number % 10000000, units, tens) : "");
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
