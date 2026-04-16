package com.example.el_encashment.service;

import com.example.el_encashment.model.*;
import com.example.el_encashment.repository.CgeisBillRepository;
import com.example.el_encashment.repository.SalaryRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
                grouped.add(new CgeisGroupedSalaryRow(from, to, cgeis, null));
                from = current.getMonthYear();
                to = current.getMonthYear();
                cgeis = current.getCgeis();
            }
        }

        grouped.add(new CgeisGroupedSalaryRow(from, to, cgeis, null));
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
                <tr>
                  <td>%s</td>
                  <td>%s</td>
                  <td class="num">%.2f</td>
                  <td class="num">%.2f</td>
                  <td class="num">%d</td>
                  <td class="num">%.2f</td>
                </tr>
                """.formatted(
                formatMonth(item.getFromMonth()),
                formatMonth(item.getToMonth()),
                defaultZero(item.getCgeis()),
                defaultZero(item.getValue()),
                item.getTimes(),
                item.getLineAmount()
            ))
            .collect(Collectors.joining());

        String amountWords = amountInWords(bill.getNetPay().longValue());
        Personnel personnel = bill.getPersonnel();
        FinanceData finance = personnel == null ? null : personnel.getFinanceData();
        double basicPay = finance != null && finance.getBasicPay() != null ? finance.getBasicPay().doubleValue() : 0;
        double specialPay = finance != null && finance.getSpecialPay() != null ? finance.getSpecialPay().doubleValue() : 0;

        return """
            <!doctype html>
            <html>
            <head>
              <meta charset="UTF-8" />
              <title>CGEIS Bill Report</title>
              <style>
                body { font-family: "Times New Roman", serif; margin: 24px; color: #111; }
                .page { max-width: 980px; margin: 0 auto; }
                .center { text-align: center; }
                .meta { display:flex; justify-content:space-between; margin-top:12px; font-size:14px; }
                .lead { margin: 18px 0; font-size: 15px; }
                .grid { display:grid; grid-template-columns: 1fr 1fr; gap: 8px 30px; margin: 14px 0 22px; font-size:14px; }
                table { width:100%%; border-collapse:collapse; margin: 12px 0 18px; }
                th, td { border:1px solid #111; padding:8px; font-size:14px; }
                th { background:#f3f3f3; }
                .num { text-align:right; }
                .summary { margin-left:auto; width: 360px; }
                .summary td:first-child { font-weight: bold; }
                .footer { display:flex; justify-content:space-between; margin-top: 36px; }
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
                  <div><strong>Bill No:</strong> %s</div>
                  <div><strong>Bill Date:</strong> %s</div>
                </div>
                <div class="lead">
                  Claim on account of CGEIS Funds in respect of <strong>%s</strong>, Emp Code <strong>%s</strong>.
                </div>

                <div class="grid">
                  <div><strong>Division:</strong> %s</div>
                  <div><strong>Category:</strong> %s</div>
                  <div><strong>GPF A/C No:</strong> %s</div>
                  <div><strong>DO Part No:</strong> %s</div>
                  <div><strong>Basic Pay:</strong> %.2f</div>
                  <div><strong>DO Part Date:</strong> %s</div>
                  <div><strong>Special Pay:</strong> %.2f</div>
                  <div><strong>Total Pay:</strong> %.2f</div>
                </div>

                <table>
                  <thead>
                    <tr>
                      <th>From Month</th>
                      <th>To Month</th>
                      <th>CGEIS</th>
                      <th>Value</th>
                      <th>Times</th>
                      <th>Value x Times</th>
                    </tr>
                  </thead>
                  <tbody>
                    %s
                  </tbody>
                </table>

                <table class="summary">
                  <tr><td>Total Amount</td><td class="num">%.2f</td></tr>
                  <tr><td>Income Tax</td><td class="num">%.2f</td></tr>
                  <tr><td>Educational Cess (4%%)</td><td class="num">%.2f</td></tr>
                  <tr><td>Total IT Recovery</td><td class="num">%.2f</td></tr>
                  <tr><td>Net Pay</td><td class="num">%.2f</td></tr>
                </table>

                <div><strong>Rupees:</strong> %s only.</div>
                <div style="margin-top:18px;">
                  <div>Encl: 1) DO Part / Sanction</div>
                  <div>2) CGEIS supporting schedule</div>
                </div>

                <div class="footer">
                  <div>Income tax, if any, will be recovered as per rules.</div>
                  <div class="center">
                    <div><strong>Sr. ACCOUNTS OFFICER</strong></div>
                    <div>FOR DIRECTOR</div>
                  </div>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
            bill.getBillNo(),
            formatDate(bill.getBillDate()),
            safe(personnel == null ? null : personnel.getName()),
            safe(personnel == null ? null : personnel.getEmpCode()),
            safe(personnel == null ? null : personnel.getDivision()),
            categoryLabel(personnel == null ? null : personnel.getDisgType()),
            safe(finance == null ? null : finance.getGpfAccountNo()),
            safe(bill.getDoPartNumber()),
            basicPay,
            formatDate(bill.getDoPartDate()),
            specialPay,
            basicPay + specialPay,
            rows,
            bill.getTotalAmount(),
            bill.getItAmount(),
            bill.getEduCess(),
            bill.getTotalIt(),
            bill.getNetPay(),
            amountWords
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

        String rows = "";
        double totalIt = 0;
        double totalEduCess = 0;
        double grandTotal = 0;
        int slNo = 1;
        for (CgeisBill bill : bills) {
            Personnel p = bill.getPersonnel();
            FinanceData finance = p == null ? null : p.getFinanceData();
            rows += """
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
                safe(p == null ? null : p.getName()),
                categoryLabel(p == null ? null : p.getDisgType()),
                bill.getItAmount(),
                bill.getEduCess(),
                bill.getTotalIt()
            );
            totalIt += bill.getItAmount();
            totalEduCess += bill.getEduCess();
            grandTotal += bill.getTotalIt();
        }

        return """
            <!doctype html>
            <html>
            <head>
              <meta charset="UTF-8" />
              <title>CGEIS IT Recovery Schedule</title>
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
                  <div><strong>UNIT ORG, HYDERABAD</strong></div>
                  <div><strong>INCOME TAX RECOVERY SCHEDULE</strong></div>
                  <div>(CGEIS Funds)</div>
                </div>
                <div class="center" style="margin-top:16px;">
                  Recovery schedule on account of income tax in respect of the undermentioned officers/staff for Bill No <strong>%s</strong>.
                </div>
                <table>
                  <thead>
                    <tr>
                      <th>Sl No</th>
                      <th>GPF A/C No</th>
                      <th>PAN</th>
                      <th>Name & Designation</th>
                      <th>Income Tax</th>
                      <th>Educational Cess (4%%)</th>
                      <th>Total Income Tax</th>
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
            sanitizedBillNo,
            rows,
            round(totalIt),
            round(totalEduCess),
            round(grandTotal),
            amountInWords(Math.round(grandTotal))
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
