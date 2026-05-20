package com.example.el_encashment.service;

import com.example.el_encashment.model.*;
import com.example.el_encashment.repository.CgeisBillRepository;
import com.example.el_encashment.repository.SalaryRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CgeisService {

    private static final DateTimeFormatter REPORT_DATE = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
    private final SalaryRepository salaryRepository;
    private final CgeisBillRepository billRepository;

    public CgeisService(SalaryRepository salaryRepository, CgeisBillRepository billRepository) {
        this.salaryRepository = salaryRepository;
        this.billRepository = billRepository;
    }

    public List<Salary> getSalaryByEmpId(Long empId) {
        return salaryRepository.findByEmpIdOrderByMonthYearAsc(empId);
    }

    public List<CgeisGroupedSalaryRow> getGroupedSalaryByEmpId(Long empId) {
        List<Salary> rows = getSalaryByEmpId(empId);
        List<CgeisGroupedSalaryRow> grouped = new ArrayList<>();
        if (rows.isEmpty()) {
            return grouped;
        }
        Map<String, CgeisBillItem> savedItems = loadSavedItemsByRange(empId);

        LocalDate from = rows.get(0).getMonthYear();
        LocalDate to = from;
        Double cgeis = rows.get(0).getCgeis();

        for (int i = 1; i < rows.size(); i++) {
            Salary current = rows.get(i);
            LocalDate expectedNext = to.plusMonths(1);
            if (
                expectedNext.equals(current.getMonthYear()) &&
                Double.compare(defaultZero(cgeis), defaultZero(current.getCgeis())) == 0
            ) {
                to = current.getMonthYear();
            } else {
                grouped.add(buildGroupedRow(from, to, cgeis, savedItems));
                from = current.getMonthYear();
                to = current.getMonthYear();
                cgeis = current.getCgeis();
            }
        }

        grouped.add(buildGroupedRow(from, to, cgeis, savedItems));
        return grouped;
    }

    public void addSalaryRange(SalaryRangeRequest request) {
        if (request.getEmpId() == null) {
            throw badRequest("Employee is required");
        }
        requireDate(request.getFromMonth(), "From month");
        requireDate(request.getToMonth(), "To month");
        validatePositive(request.getCgeis(), "CGEIS");

        LocalDate fromMonth = request.getFromMonth().withDayOfMonth(1);
        LocalDate toMonth = request.getToMonth().withDayOfMonth(1);
        if (toMonth.isBefore(fromMonth)) {
            throw badRequest("To month cannot be before From month");
        }

        LocalDate cursor = fromMonth;
        while (!cursor.isAfter(toMonth)) {
            if (salaryRepository.existsByEmpIdAndMonthYear(request.getEmpId(), cursor)) {
                throw badRequest("Salary record already exists for " + cursor.format(MONTH_LABEL));
            }
            cursor = cursor.plusMonths(1);
        }

        cursor = fromMonth;
        List<Salary> rows = new ArrayList<>();
        while (!cursor.isAfter(toMonth)) {
            Salary salary = new Salary();
            salary.setEmpId(request.getEmpId());
            salary.setMonthYear(cursor);
            salary.setCgeis(request.getCgeis());
            rows.add(salary);
            cursor = cursor.plusMonths(1);
        }
        salaryRepository.saveAll(rows);
    }

    public void deleteSalaryRange(SalaryRangeRequest request) {
        if (request.getEmpId() == null) {
            throw badRequest("Employee is required");
        }
        requireDate(request.getFromMonth(), "From month");
        requireDate(request.getToMonth(), "To month");
        LocalDate fromMonth = request.getFromMonth().withDayOfMonth(1);
        LocalDate toMonth = request.getToMonth().withDayOfMonth(1);
        if (toMonth.isBefore(fromMonth)) {
            throw badRequest("To month cannot be before From month");
        }
        salaryRepository.deleteByEmpIdAndMonthYearBetween(request.getEmpId(), fromMonth, toMonth);
    }

    public CgeisBill createBill(CgeisPrepareRequest request) {
        if (request.getEmpId() == null) {
            throw badRequest("Employee is required");
        }
        String doPartNumber = trimToNull(request.getDoPartNumber());
        if (doPartNumber == null) {
            throw badRequest("DO Part number is required");
        }
        requireDate(request.getDoPartDate(), "DO Part date");
        requireDate(request.getBillDate(), "Bill date");
        String billNo = normalizeBillNo(request.getBillNo());
        if (request.getDoPartDate().isAfter(request.getBillDate())) {
            throw badRequest("DO Part date cannot be after bill date");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw badRequest("Select at least one CGEIS row");
        }

        List<CgeisBillItem> items = new ArrayList<>();
        double total = 0;
        for (CgeisBillItemRequest itemRequest : request.getItems()) {
            requireDate(itemRequest.getFromMonth(), "From month");
            requireDate(itemRequest.getToMonth(), "To month");
            validatePositive(itemRequest.getCgeis(), "CGEIS");
            validatePositive(itemRequest.getValue(), "Value");
            if (itemRequest.getTimes() == null || itemRequest.getTimes() <= 0) {
                throw badRequest("Times must be greater than 0");
            }
            if (itemRequest.getToMonth().isBefore(itemRequest.getFromMonth())) {
                throw badRequest("To month cannot be before From month");
            }

            CgeisBillItem item = new CgeisBillItem();
            item.setFromMonth(itemRequest.getFromMonth().withDayOfMonth(1));
            item.setToMonth(itemRequest.getToMonth().withDayOfMonth(1));
            item.setCgeis(itemRequest.getCgeis());
            item.setValue(itemRequest.getValue());
            item.setTimes(itemRequest.getTimes());
            double amount = itemRequest.getValue() * itemRequest.getTimes();
            item.setLineAmount(amount);
            total += amount;
            items.add(item);
        }

        double itAmount = request.getItAmount() == null ? 0 : request.getItAmount();
        if (itAmount < 0) {
            throw badRequest("IT amount cannot be negative");
        }
        double eduCess = round(itAmount * 0.04);
        double totalIt = round(itAmount + eduCess);
        double netPay = round(total - totalIt);

        CgeisBill bill = new CgeisBill();
        bill.setEmpId(request.getEmpId());
        bill.setDoPartNumber(doPartNumber);
        bill.setDoPartDate(request.getDoPartDate());
        bill.setBillNo(billNo);
        bill.setBillDate(request.getBillDate());
        bill.setTotalAmount(round(total));
        bill.setItAmount(round(itAmount));
        bill.setEduCess(eduCess);
        bill.setTotalIt(totalIt);
        bill.setNetPay(netPay);
        bill.setItems(items);
        for (CgeisBillItem item : items) {
            item.setBill(bill);
        }
        return billRepository.save(bill);
    }

    public List<CgeisBillSummary> getBillsByEmpId(Long empId) {
        return billRepository.findDetailedByEmpId(empId).stream().map(CgeisBillSummary::new).toList();
    }

    public List<CgeisBillSummary> getPendingDv(String category) {
        List<CgeisBill> bills = isAll(category)
            ? billRepository.findPendingDv()
            : billRepository.findPendingDvByDisgType(resolveTypes(category));
        return bills.stream().map(CgeisBillSummary::new).toList();
    }

    public List<CgeisBillSummary> getProcessedDv(String category) {
        List<CgeisBill> bills = isAll(category)
            ? billRepository.findProcessedDv()
            : billRepository.findProcessedDvByDisgType(resolveTypes(category));
        return bills.stream().map(CgeisBillSummary::new).toList();
    }

    public List<CgeisBillSummary> getReportBills(String category) {
        List<CgeisBill> bills = isAll(category)
            ? billRepository.findAllDetailed()
            : billRepository.findAllByDisgType(resolveTypes(category));
        return bills.stream().map(CgeisBillSummary::new).toList();
    }

    public void updateDvDetails(List<Long> ids, DvUpdateRequest req) {
        if (ids == null || ids.isEmpty()) {
            throw badRequest("Select at least one bill");
        }
        String dvNo = normalizeDvNo(req.getDvNo());
        requireDate(req.getDvDate(), "DV date");
        validatePositive(req.getDvAmount(), "DV amount");
        validateNonNegative(req.getDvBalance(), "DV balance");
        validateNonNegative(req.getRecoveryCda(), "Recovery CDA");
        validateNonNegative(req.getRecoveryCdaTax(), "Recovery CDA Tax");
        validateRemark(req.getRecoveryCda(), req.getCdaRemarks(), "CDA remarks");
        validateRemark(req.getRecoveryCdaTax(), req.getCdaTaxRemarks(), "CDA tax remarks");

        List<CgeisBill> bills = billRepository.findAllById(ids);
        if (bills.size() != ids.stream().distinct().count()) {
            throw badRequest("One or more selected CGEIS bills were not found");
        }

        for (CgeisBill bill : bills) {
            if (bill.getBillNo() == null || bill.getBillNo().isBlank()) {
                throw badRequest("Bill number is required before DV entry");
            }
            if (bill.getDvNo() != null && !bill.getDvNo().isBlank()) {
                throw badRequest("Selected bill already has DV details");
            }
            if (bill.getBillDate() != null && req.getDvDate().isBefore(bill.getBillDate())) {
                throw badRequest("DV date cannot be before bill date");
            }
            bill.setDvNo(dvNo);
            bill.setDvDate(req.getDvDate());
            bill.setDvAmount(req.getDvAmount());
            bill.setDvBalance(req.getDvBalance());
            bill.setRecoveryCda(req.getRecoveryCda());
            bill.setCdaRemarks(trimToNull(req.getCdaRemarks()));
            bill.setRecoveryCdaTax(req.getRecoveryCdaTax());
            bill.setCdaTaxRemarks(trimToNull(req.getCdaTaxRemarks()));
        }
        billRepository.saveAll(bills);
    }

    public String buildBillReportHtml(Long id) {
        CgeisBill bill = billRepository.findDetailedById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CGEIS bill not found"));

        String rows = bill.getItems().stream()
            .map(item -> """
                <div class="entry-row">
                  <div>%s&nbsp;&nbsp;&nbsp; to &nbsp;&nbsp;&nbsp;%s</div>
                  <div>@%.0f</div>
                  <div>= Rs.%.2f X %d</div>
                  <div>=</div>
                  <div class="num">%.2f</div>
                </div>
                """.formatted(
                formatLongDate(item.getFromMonth()),
                formatLongDate(endOfMonth(item.getToMonth())),
                defaultZero(item.getCgeis()),
                defaultZero(item.getValue()),
                item.getTimes(),
                defaultZero(item.getLineAmount())
            ))
            .collect(Collectors.joining());

        long roundedTotal = Math.round(defaultZero(bill.getTotalAmount()));
        String amountWords = amountInWords(roundedTotal);
        Personnel personnel = bill.getPersonnel();
        FinanceData finance = personnel == null ? null : personnel.getFinanceData();
        LocalDate retiredOn = bill.getItems().isEmpty() ? null : endOfMonth(bill.getItems().get(bill.getItems().size() - 1).getToMonth());

        return """
            <!doctype html>
            <html>
            <head>
              <meta charset="UTF-8" />
              <title>CGEIS Bill Report</title>
              <style>
                body { font-family: "Times New Roman", serif; margin: 24px; color: #111; }
                .page { max-width: 1000px; margin: 0 auto; }
                .center { text-align: center; }
                .top-note { display:flex; justify-content:space-between; font-size:14px; margin-bottom:24px; }
                .title-1 { text-align:center; font-size:24px; font-weight:bold; text-decoration:underline; }
                .title-2 { text-align:center; font-size:22px; font-weight:bold; text-decoration:underline; margin-top:12px; }
                .title-3 { text-align:center; font-size:21px; font-weight:bold; text-decoration:underline; margin-top:28px; }
                .person-block { margin-top:42px; font-size:16px; line-height:1.9; }
                .claim-block { margin-top:54px; font-size:18px; font-weight:bold; line-height:1.45; }
                .section-title { margin-top:54px; font-size:18px; font-weight:bold; }
                .entry-list { margin-top:28px; }
                .entry-row { display:grid; grid-template-columns: 2.6fr 0.7fr 1.4fr 0.3fr 0.9fr; gap:18px; align-items:baseline; margin: 24px 0; font-size:18px; }
                .num { text-align:right; }
                .totals { width: 340px; margin-left:auto; margin-top: 28px; font-size:18px; }
                .totals-row { display:flex; justify-content:space-between; margin: 10px 0; }
                .words { margin-top: 34px; font-size:18px; font-weight:bold; }
                .authority { margin-top: 34px; font-size:17px; line-height:1.7; }
                @media print { body { margin: 8mm; } }
              </style>
            </head>
            <body>
              <div class="page">
                <div class="top-note">
                  <div>%s</div>
                  <div>'DEDUCTIONS'</div>
                </div>
                <div class="title-1">UNIT - DEFENCE RESEARCH &amp; DEVELOPMENT LABORATORY</div>
                <div class="title-2">KANCHANBAGH, HYDERABAD - 500058</div>
                <div class="title-3">Central Government Employees Group Insurance Scheme - 1980</div>

                <div class="person-block">
                  <div>SHRI %s, %s</div>
                  <div>GPFACCNO : %s</div>
                  <div>Pers No : %s</div>
                  <div>Retired On : %s</div>
                </div>

                <div class="claim-block">
                  Claim on account of CGEIS Saving Fund in respect of SHRI %s,
                  %s, Id number: %s who has retired from Govt. Service w.e.f. %s
                  notified vide DO Part-II, %s, Date: %s.
                </div>

                <div class="section-title">(1) CGEIS SAVING FUND:</div>

                <div class="entry-list">
                  %s
                </div>

                <div class="totals">
                  <div class="totals-row"><span>Total :</span><span>%.2f</span></div>
                  <div class="totals-row"><span>Rounded of Total :</span><span>%.2f</span></div>
                </div>

                <div class="words">TOTAL IN WORDS: Rupees %s Only</div>

                <div class="authority">
                  <div><strong>Authority:-</strong></div>
                  <div style="margin-top:12px;">1. DO Part-II, No. %s, date %s</div>
                  <div>2. Service Book</div>
                  <div>3. CERTIFICATE OF SANCTION</div>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
            formatGeneratedStamp(bill.getBillDate()),
            safe(personnel == null ? null : personnel.getName()),
            categoryLabel(personnel == null ? null : personnel.getDisgType()),
            safe(finance == null ? null : finance.getGpfAccountNo()),
            safe(personnel == null ? null : personnel.getEmpCode()),
            formatLongDate(retiredOn),
            safe(personnel == null ? null : personnel.getName()),
            categoryLabel(personnel == null ? null : personnel.getDisgType()),
            safe(personnel == null ? null : personnel.getEmpCode()),
            formatLongDate(retiredOn),
            safe(bill.getDoPartNumber()),
            formatLongDate(bill.getDoPartDate()),
            rows,
            defaultZero(bill.getTotalAmount()),
            (double) roundedTotal,
            amountWords
                .toUpperCase(Locale.ENGLISH),
            safe(bill.getDoPartNumber()),
            formatLongDate(bill.getDoPartDate())
        );
    }

    public String buildItScheduleHtml(String billNo) {
        String sanitizedBillNo = trimToNull(billNo);
        if (sanitizedBillNo == null) {
            throw badRequest("Bill No is required");
        }
        List<CgeisBill> bills = billRepository.findDetailedByBillNo(sanitizedBillNo);
        if (bills.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No CGEIS bills found for Bill No");
        }

        CgeisBill bill = bills.get(0);
        Personnel p = bill.getPersonnel();
        FinanceData finance = p == null ? null : p.getFinanceData();
        String rows = bill.getItems().stream()
            .map(item -> """
                <div class="entry-row">
                  <div>%s</div>
                  <div>to %s</div>
                  <div>@%.0f Rs.%.2f X %d</div>
                  <div>=</div>
                  <div class="num">%.2f</div>
                </div>
                """.formatted(
                formatLongDate(item.getFromMonth()),
                formatLongDate(endOfMonth(item.getToMonth())),
                defaultZero(item.getCgeis()),
                defaultZero(item.getValue()),
                item.getTimes(),
                defaultZero(item.getLineAmount())
            ))
            .collect(Collectors.joining());

        double total = defaultZero(bill.getTotalAmount());
        double roundedTotal = Math.round(total);
        double insuranceFunds = 0;
        double otherRecovery = 0;
        double grandTotal = roundedTotal + insuranceFunds;
        double netAmount = grandTotal - otherRecovery;
        LocalDate retiredOn = bill.getItems().isEmpty() ? null : endOfMonth(bill.getItems().get(bill.getItems().size() - 1).getToMonth());

        return """
            <!doctype html>
            <html>
            <head>
              <meta charset="UTF-8" />
              <title>CGEIS Sanction Report</title>
              <style>
                body { font-family: "Times New Roman", serif; margin: 24px; color: #111; }
                .page { max-width: 1000px; margin: 0 auto; }
                .top-note { display:flex; justify-content:space-between; font-size:14px; margin-bottom:24px; }
                .center { text-align:center; }
                .title-1 { text-align:center; font-size:24px; font-weight:bold; text-decoration:underline; }
                .title-2 { text-align:center; font-size:22px; font-weight:bold; text-decoration:underline; }
                .title-3 { text-align:center; font-size:22px; font-weight:bold; text-decoration:underline; margin-top:26px; }
                .para { margin-top: 48px; font-size:18px; line-height:1.5; text-align:justify; }
                .section-title { margin-top:44px; font-size:18px; font-weight:bold; }
                .entry-list { margin-top:24px; width: 88%%; }
                .entry-row { display:grid; grid-template-columns: 1.4fr 0.9fr 1.6fr 0.3fr 0.8fr; gap:16px; margin: 12px 0; font-size:17px; }
                .num { text-align:right; }
                .totals { width: 330px; margin-left:auto; margin-top: 16px; font-size:17px; }
                .totals-row { display:flex; justify-content:space-between; margin: 8px 0; }
                .govt { margin-top: 52px; font-size:18px; line-height:1.7; }
                .sign { margin-top: 48px; width: 320px; margin-left:auto; text-align:center; font-size:18px; line-height:1.7; }
              </style>
            </head>
            <body>
              <div class="page">
                <div class="top-note">
                  <div>%s</div>
                  <div>'SANCTION'</div>
                </div>
                <div class="title-1">DEFENCE RESEARCH AND DEVELOPMENT LABORATORY</div>
                <div class="title-2">KANCHANBAGH, HYDERABAD - 500058</div>
                <div class="title-3">SANCTION</div>

                <div class="para">
                  Under the provisions of Para 118 of Government of India, Ministry of Finance
                  (Department of Expenditure) OM No.F/15(3)/78 WIP dated 31<sup>st</sup> October 1980
                  as reproduced in CPRO 27/81 sanction is hereby accorded for the payment of
                  Rs. %.0f/- Rupees %s Only Saving Fund of Central Government Employees Group
                  Insurance Scheme to SHRI %s, GPFACNO %s, TO, Id number: %s, who has retired
                  from Govt. Service w.e.f. %s notified vide DO Part-II, No.%s, dated: %s for your
                  audit and payment.
                </div>

                <div class="section-title">GROUP INSURANCE (SAVING FUND):</div>

                <div class="entry-list">
                  %s
                </div>

                <div class="totals">
                  <div class="totals-row"><span>TOTAL</span><span>%.2f</span></div>
                  <div class="totals-row"><span>Rounded of TOTAL</span><span>%.2f</span></div>
                  <div class="totals-row"><span>Cgeis Insurance Funds</span><span>%.2f</span></div>
                  <div class="totals-row"><span>GRAND TOTAL</span><span>%.2f</span></div>
                  <div class="totals-row"><span>Other Recovery</span><span>%.2f</span></div>
                  <div class="totals-row"><span>NET AMOUNT</span><span>%.2f</span></div>
                </div>

                <div class="govt">
                  <div>No.DRDL/FIN/CGOS/CGEIS/%s/%s</div>
                  <div style="margin-top:28px;">Government of India</div>
                  <div>Ministry of Defence</div>
                  <div>Defence Research &amp; Development Laboratory</div>
                  <div>HYDERABAD-500058</div>
                  <div style="margin-top:28px;">Date : %s</div>
                </div>

                <div class="sign">
                  <div>Yours Faithfully</div>
                  <div style="margin-top:70px;"><strong>CFA</strong></div>
                  <div><strong>Senior Accounts Officer</strong></div>
                  <div><strong>For Director, DRDL</strong></div>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
            formatGeneratedStamp(bill.getBillDate()),
            roundedTotal,
            amountInWords(Math.round(roundedTotal)).toUpperCase(Locale.ENGLISH),
            safe(p == null ? null : p.getName()),
            safe(finance == null ? null : finance.getGpfAccountNo()),
            safe(p == null ? null : p.getEmpCode()),
            formatLongDate(retiredOn),
            safe(bill.getDoPartNumber()),
            formatLongDate(bill.getDoPartDate()),
            rows,
            round(grandTotal),
            roundedTotal,
            insuranceFunds,
            grandTotal,
            otherRecovery,
            netAmount,
            safe(bill.getBillNo()),
            safe(bill.getDvNo()),
            formatLongDate(bill.getBillDate())
        );
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

    private boolean isAll(String category) {
        return category == null || "all".equalsIgnoreCase(category);
    }

    private CgeisGroupedSalaryRow buildGroupedRow(
        LocalDate from,
        LocalDate to,
        Double cgeis,
        Map<String, CgeisBillItem> savedItems
    ) {
        CgeisBillItem savedItem = savedItems.get(buildRangeKey(from, to, cgeis));
        return new CgeisGroupedSalaryRow(
            from,
            to,
            cgeis,
            savedItem == null ? null : savedItem.getValue(),
            savedItem == null ? null : savedItem.getTimes()
        );
    }

    private Map<String, CgeisBillItem> loadSavedItemsByRange(Long empId) {
        Map<String, CgeisBillItem> savedItems = new HashMap<>();
        for (CgeisBill bill : billRepository.findDetailedByEmpId(empId)) {
            for (CgeisBillItem item : bill.getItems()) {
                savedItems.putIfAbsent(
                    buildRangeKey(item.getFromMonth(), item.getToMonth(), item.getCgeis()),
                    item
                );
            }
        }
        return savedItems;
    }

    private String buildRangeKey(LocalDate from, LocalDate to, Double cgeis) {
        return "%s|%s|%.4f".formatted(
            from,
            to,
            defaultZero(cgeis)
        );
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

    private void requireDate(LocalDate date, String label) {
        if (date == null) {
            throw badRequest(label + " is required");
        }
    }

    private void validatePositive(Double value, String label) {
        if (value == null || value <= 0) {
            throw badRequest(label + " must be greater than 0");
        }
    }

    private void validateNonNegative(Double value, String label) {
        if (value != null && value < 0) {
            throw badRequest(label + " cannot be negative");
        }
    }

    private void validateRemark(Double amount, String remark, String label) {
        if (amount != null && amount > 0 && trimToNull(remark) == null) {
            throw badRequest(label + " is required");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double defaultZero(Double value) {
        return value == null ? 0 : value;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : date.format(REPORT_DATE);
    }

    private String formatMonth(LocalDate date) {
        return date == null ? "-" : YearMonth.from(date).format(MONTH_LABEL);
    }

    private String formatLongDate(LocalDate date) {
        return date == null ? "-" : date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH));
    }

    private LocalDate endOfMonth(LocalDate date) {
        return date == null ? null : YearMonth.from(date).atEndOfMonth();
    }

    private String formatGeneratedStamp(LocalDate date) {
        LocalDate value = date == null ? LocalDate.now() : date;
        return value.format(DateTimeFormatter.ofPattern("M/d/yy", Locale.ENGLISH)) + ", 3:10 PM";
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String categoryLabel(Integer type) {
        if (type == null) return "-";
        return type == 1 ? "Officer" : "Staff";
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
}
