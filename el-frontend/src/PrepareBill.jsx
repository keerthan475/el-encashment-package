import { useCallback, useEffect, useMemo, useState } from "react";
import { getApiErrorMessage } from "./utils/api";
import { formatAmount, formatDate, formatDisgType } from "./utils/formatters";

const emptyForm = {
  purpose: "",
  eventDate: "",
  blockPeriod: "",
  elDays: 0,
  hplDays: 0,
  elAmount: 0,
  hplAmount: 0,
  totalAmount: 0,
  doPartNumber: "",
  doPartDate: "",
  itApplicable: "",
  itPercent: "",
  itAmount: 0,
  eduCess: 0,
  itRecovery: 0,
  otherRecovery: 0,
  otherRemark: "",
  otherTaxable: 0,
  otherTaxableRemark: "",
  grandTotal: 0
};

const isLtcPurpose = (purpose) => purpose === "Home_Town" || purpose === "All_India";

function PrepareBill() {
  const [search, setSearch] = useState("");
  const [searchCategory, setSearchCategory] = useState("all");
  const [tableCategory, setTableCategory] = useState("all");
  const [employees, setEmployees] = useState([]);
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [pendingRecords, setPendingRecords] = useState([]);
  const [isPendingLoading, setIsPendingLoading] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [availableBlocks, setAvailableBlocks] = useState([]);
  const [showCalculation, setShowCalculation] = useState(false);
  const [finalCalculated, setFinalCalculated] = useState(false);
  const [errors, setErrors] = useState({});
  const [statusMessage, setStatusMessage] = useState("");
  const [statusType, setStatusType] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);

  const showStatus = (type, message) => {
    setStatusType(type);
    setStatusMessage(message);
  };

  const updateForm = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const resetForm = () => {
    setForm(emptyForm);
    setSelectedEmployee(null);
    setEmployees([]);
    setAvailableBlocks([]);
    setShowCalculation(false);
    setFinalCalculated(false);
    setEditingId(null);
    setErrors({});
    setSearch("");
    setIsSearchOpen(false);
  };

  const loadPendingRecords = useCallback(async () => {
    setIsPendingLoading(true);
    try {
      const response = await fetch(`http://localhost:8080/api/encashment/pending-bills?category=${tableCategory}`);
      if (!response.ok) throw new Error("Unable to load pending records");
      setPendingRecords(await response.json());
    } catch (error) {
      setPendingRecords([]);
      showStatus("error", error.message || "Unable to load pending records");
    } finally {
      setIsPendingLoading(false);
    }
  }, [tableCategory]);

  useEffect(() => {
    loadPendingRecords();
  }, [loadPendingRecords]);

  const generateBlocks = (dateValue, purposeValue) => {
    if (!dateValue) {
      setAvailableBlocks([]);
      return;
    }
    const year = new Date(dateValue).getFullYear();
    const baseYear = purposeValue === "Home_Town" ? 2016 : 2018;
    const size = purposeValue === "Home_Town" ? 2 : 4;
    const diff = year - baseYear;
    const blockIndex = Math.floor(diff / size);
    const currentStart = baseYear + blockIndex * size;
    const currentEnd = currentStart + size - 1;
    const blocks = [];
    if (year === currentStart && blockIndex > 0) {
      blocks.push(`${currentStart - size}-${currentStart - 1}`);
    }
    blocks.push(`${currentStart}-${currentEnd}`);
    setAvailableBlocks(blocks);
  };

  const getEventDateLabel = (purpose) => {
    if (isLtcPurpose(purpose)) return "LTC Date";
    if (purpose === "Retirement") return "Retirement Date";
    return "Event Date";
  };

  const searchEmployees = useCallback(async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/personnel/search?name=${encodeURIComponent(search)}&category=${searchCategory}`);
      if (!response.ok) throw new Error("Unable to search employees");
      setEmployees(await response.json());
      setIsSearchOpen(true);
    } catch (error) {
      showStatus("error", error.message || "Unable to search employees");
    }
  }, [search, searchCategory]);

  useEffect(() => {
    if (!isSearchOpen) return;
    const delay = setTimeout(() => {
      searchEmployees();
    }, 200);
    return () => clearTimeout(delay);
  }, [search, searchCategory, isSearchOpen, searchEmployees]);

  const handleSelectEmployee = (employee) => {
    setSelectedEmployee(employee);
    setSearch(`${employee.name} | ${employee.empCode}`);
    setEmployees([]);
    setIsSearchOpen(false);
    setErrors((prev) => ({ ...prev, employee: "" }));
  };

  const handlePurposeChange = async (value) => {
    setShowCalculation(false);
    setFinalCalculated(false);
    setErrors((prev) => ({ ...prev, purpose: "", eventDate: "", blockPeriod: "" }));
    setForm((prev) => ({
      ...prev,
      purpose: value,
      eventDate: "",
      blockPeriod: "",
      elDays: 0,
      hplDays: 0,
      elAmount: 0,
      hplAmount: 0,
      totalAmount: 0,
      itApplicable: "",
      itPercent: "",
      itAmount: 0,
      eduCess: 0,
      itRecovery: 0,
      grandTotal: 0
    }));
    setAvailableBlocks([]);
    if (!value || !selectedEmployee) return;
    if (value === "Retirement") {
      try {
        const response = await fetch(`http://localhost:8080/api/personnel/${selectedEmployee.id}/retirement-date`);
        if (!response.ok) {
          throw new Error(await getApiErrorMessage(response, "Unable to load retirement date"));
        }
        const date = await response.json();
        setForm((prev) => ({ ...prev, eventDate: date }));
      } catch (error) {
        showStatus("error", error.message || "Unable to load retirement date");
      }
    }
  };

  const calculateAmounts = () => {
    const basic = Number(selectedEmployee?.financeData?.basicPay || 0);
    const payPlusDa = basic * 1.5;
    const elAmount = (payPlusDa * Number(form.elDays || 0)) / 30;
    const hplAmount = ((payPlusDa * Number(form.hplDays || 0)) / 30) / 2;
    setForm((prev) => ({
      ...prev,
      elAmount,
      hplAmount,
      totalAmount: elAmount + hplAmount
    }));
    setShowCalculation(true);
  };

  const computeIT = () => {
    if (form.itApplicable === "No") {
      setForm((prev) => ({ ...prev, itAmount: 0, eduCess: 0, itRecovery: 0 }));
      return;
    }
    const itAmount = (Number(form.totalAmount) * Number(form.itPercent)) / 100;
    const eduCess = itAmount * 0.04;
    setForm((prev) => ({ ...prev, itAmount, eduCess, itRecovery: itAmount + eduCess }));
  };

  const handleITAmountChange = (value) => {
    const itAmount = Number(value || 0);
    const eduCess = itAmount * 0.04;
    setForm((prev) => ({ ...prev, itAmount, eduCess, itRecovery: itAmount + eduCess }));
  };

  const validateSection1 = () => {
    const nextErrors = {};
    const elDays = Number(form.elDays);
    const hplDays = Number(form.hplDays);
    if (!selectedEmployee) nextErrors.employee = "Select employee";
    if (!form.purpose) nextErrors.purpose = "Select purpose";
    if (!form.eventDate) nextErrors.eventDate = "Select event date";
    if (!form.doPartNumber.trim()) nextErrors.doPartNumber = "Enter DO Part number";
    if (!form.doPartDate) nextErrors.doPartDate = "Select DO Part date";
    else if (form.eventDate && form.doPartDate >= form.eventDate) nextErrors.doPartDate = "DO Part date must be before LTC/Event date";

    if (isLtcPurpose(form.purpose)) {
      if (!form.blockPeriod) nextErrors.blockPeriod = "Select block period";
      if (!Number.isInteger(elDays) || elDays < 1 || elDays > 10) nextErrors.elDays = "EL days must be between 1 and 10";
      if (hplDays !== 0) nextErrors.hplDays = "HPL days must be 0 for LTC purpose";
    } else {
      if (!Number.isInteger(elDays) || elDays < 0 || elDays > 300) nextErrors.elDays = "EL days must be between 0 and 300";
      if (!Number.isInteger(hplDays) || hplDays < 0 || hplDays > 300) nextErrors.hplDays = "HPL days must be between 0 and 300";
      if (elDays + hplDays < 1 || elDays + hplDays > 300) nextErrors.totalLeave = "EL days + HPL days must be between 1 and 300";
    }

    setErrors((prev) => ({ ...prev, ...nextErrors }));
    return Object.keys(nextErrors).length === 0;
  };

  const validateSection3 = () => {
    const nextErrors = {};
    if (!form.itApplicable) nextErrors.itApplicable = "Select IT applicable option";
    if (form.itApplicable === "Yes" && !form.itPercent) nextErrors.itPercent = "Select IT %";
    if (Number(form.otherRecovery) < 0) nextErrors.otherRecovery = "Other recovery cannot be negative";
    if (Number(form.otherTaxable) < 0) nextErrors.otherTaxable = "Other taxable cannot be negative";
    if (Number(form.otherRecovery) > 0 && !form.otherRemark.trim()) nextErrors.otherRemark = "Enter remark for other recovery";
    if (Number(form.otherTaxable) > 0 && !form.otherTaxableRemark.trim()) nextErrors.otherTaxableRemark = "Enter remark for other taxable";
    setErrors((prev) => ({ ...prev, ...nextErrors }));
    return Object.keys(nextErrors).length === 0;
  };

  const computeGrandTotal = () => {
    if (!validateSection1() || !validateSection3()) return;
    const grandTotal = Number(form.totalAmount) - (Number(form.itRecovery) + Number(form.otherRecovery) + Number(form.otherTaxable));
    setForm((prev) => ({ ...prev, grandTotal }));
    setFinalCalculated(true);
  };

  const buildPayload = () => ({
    empId: selectedEmployee.id,
    purpose: form.purpose,
    eventDate: form.eventDate,
    blockPeriod: isLtcPurpose(form.purpose) ? form.blockPeriod : null,
    elDays: Number(form.elDays),
    hplDays: isLtcPurpose(form.purpose) ? 0 : Number(form.hplDays),
    elAmount: Number(form.elAmount),
    hplAmount: Number(form.hplAmount),
    totalAmount: Number(form.totalAmount),
    doPartNumber: form.doPartNumber.trim(),
    doPartDate: form.doPartDate,
    itAmount: Number(form.itAmount),
    eduCess: Number(form.eduCess),
    itRecovery: Number(form.itRecovery),
    otherRecovery: Number(form.otherRecovery),
    otherRemark: form.otherRemark.trim(),
    otherTaxable: Number(form.otherTaxable),
    otherTaxableRemark: form.otherTaxableRemark.trim(),
    grandTotal: Number(form.grandTotal)
  });

  const submitPreparedBill = async (method, url, successMessage) => {
    if (!validateSection1() || !validateSection3()) return;
    setIsSubmitting(true);
    showStatus("", "");
    try {
      const response = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: method === "DELETE" ? undefined : JSON.stringify(buildPayload())
      });
      if (!response.ok) {
        const message = await getApiErrorMessage(response, "Unable to save record");
        throw new Error(message || "Unable to save record");
      }
      showStatus("success", successMessage);
      resetForm();
      await loadPendingRecords();
    } catch (error) {
      showStatus("error", error.message || "Unable to save record");
    } finally {
      setIsSubmitting(false);
    }
  };

  const saveBill = async () => submitPreparedBill("POST", "http://localhost:8080/api/encashment/save", "Bill saved successfully. Fresh form is ready for the next bill.");
  const updateBill = async () => submitPreparedBill("PUT", `http://localhost:8080/api/encashment/${editingId}`, "Bill updated successfully");
  const deleteBill = async () => submitPreparedBill("DELETE", `http://localhost:8080/api/encashment/${editingId}`, "Bill deleted successfully");

  const editRecord = async (id) => {
    showStatus("", "");
    try {
      const response = await fetch(`http://localhost:8080/api/encashment/${id}`);
      if (!response.ok) throw new Error("Unable to load record");
      const record = await response.json();
      setEditingId(record.id);
      setSelectedEmployee(record.personnel);
      setSearch(record.personnel ? `${record.personnel.name} | ${record.personnel.empCode}` : "");
      setForm({
        purpose: record.purpose || "",
        eventDate: record.eventDate || "",
        blockPeriod: record.blockPeriod || "",
        elDays: record.elDays ?? 0,
        hplDays: record.hplDays ?? 0,
        elAmount: record.elAmount ?? 0,
        hplAmount: record.hplAmount ?? 0,
        totalAmount: record.totalAmount ?? 0,
        doPartNumber: record.doPartNumber || "",
        doPartDate: record.doPartDate || "",
        itApplicable: Number(record.itRecovery) > 0 ? "Yes" : "No",
        itPercent: "",
        itAmount: record.itAmount ?? 0,
        eduCess: record.eduCess ?? 0,
        itRecovery: record.itRecovery ?? 0,
        otherRecovery: record.otherRecovery ?? 0,
        otherRemark: record.otherRemark || "",
        otherTaxable: record.otherTaxable ?? 0,
        otherTaxableRemark: record.otherTaxableRemark || "",
        grandTotal: record.grandTotal ?? 0
      });
      generateBlocks(record.eventDate, record.purpose);
      setShowCalculation(true);
      setFinalCalculated(true);
      setErrors({});
      setIsSearchOpen(false);
      window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (error) {
      showStatus("error", error.message || "Unable to load record");
    }
  };

  const canCalculate = !!selectedEmployee && !!form.purpose && !!form.eventDate && Number(form.elDays) >= 0;
  const filteredEmployees = useMemo(() => {
    if (searchCategory === "officer") {
      return employees.filter((employee) => employee.disgType === 1);
    }
    if (searchCategory === "staff") {
      return employees.filter((employee) => employee.disgType === 2 || employee.disgType === 3);
    }
    return employees;
  }, [employees, searchCategory]);
  const searchCategoryLabel = useMemo(() => searchCategory === "officer" ? "Officer" : searchCategory === "staff" ? "Staff" : "All", [searchCategory]);
  const tableCategoryLabel = useMemo(() => tableCategory === "officer" ? "Officer" : tableCategory === "staff" ? "Staff" : "All", [tableCategory]);

  return (
    <div>
      {statusMessage && <div className={`status-banner ${statusType === "error" ? "is-error" : "is-success"}`}>{statusMessage}</div>}

      <div className="page-card">
        <div className="toolbar-row">
          <div>
            <h2 className="section-title">Employee Search</h2>
            <p className="section-subtitle">Search and select one employee to prepare or edit a bill.</p>
          </div>
          {editingId && <button onClick={resetForm}>Cancel Edit</button>}
        </div>

        <div className="search-shell">
          <div className="search-inline">
            <input type="text" placeholder="Search employee by name or code" value={search} onChange={(e) => { setSearch(e.target.value); setIsSearchOpen(true); }} onFocus={() => { setIsSearchOpen(true); searchEmployees(); }} />
            <button onClick={searchEmployees}>Search</button>
          </div>

          {isSearchOpen && (
            <div className="search-dropdown">
              <div className="search-dropdown__header">
                <label htmlFor="prepare-search-category">Category Filter</label>
                <select id="prepare-search-category" value={searchCategory} onChange={(e) => { setSearchCategory(e.target.value); setEmployees([]); }}>
                  <option value="all">All</option>
                  <option value="officer">Officer</option>
                  <option value="staff">Staff</option>
                </select>
              </div>
              <div className="search-dropdown__list">
                {filteredEmployees.map((employee) => (
                  <button key={employee.id} type="button" className="search-dropdown__item" onClick={() => handleSelectEmployee(employee)}>
                    {employee.name} | {employee.empCode} | {formatDisgType(employee.disgType)}
                  </button>
                ))}
                {filteredEmployees.length === 0 && <div style={{ padding: "12px 14px", color: "#64748b" }}>No employees found for {searchCategoryLabel}</div>}
              </div>
            </div>
          )}
        </div>
        {errors.employee && <p className="error-text">{errors.employee}</p>}
      </div>

      {selectedEmployee && (
        <div className="page-card">
          <div className="form-grid">
            <div>
              <h3 className="section-title">Employee Details</h3>
              <p className="helper-text">Name: {selectedEmployee.name}</p>
              <p className="helper-text">Emp Code: {selectedEmployee.empCode}</p>
              <p className="helper-text">Division: {selectedEmployee.division}</p>
              <p className="helper-text">DOB: {formatDate(selectedEmployee.dob)}</p>
              <p className="helper-text">Category: {formatDisgType(selectedEmployee.disgType)}</p>
              <p className="helper-text">GPF: {selectedEmployee.financeData?.gpfAccountNo || "-"}</p>
            </div>
            <div>
              <h3 className="section-title">Financial Details</h3>
              <p className="helper-text">Basic Pay: {formatAmount(selectedEmployee.financeData?.basicPay || 0)}</p>
              <p className="helper-text">DA %: 50</p>
              <p className="helper-text">DA Amount: {formatAmount(Number(selectedEmployee.financeData?.basicPay || 0) * 0.5)}</p>
              <p className="helper-text">Total Pay: {formatAmount(Number(selectedEmployee.financeData?.basicPay || 0) * 1.5)}</p>
            </div>
          </div>
        </div>
      )}

      {selectedEmployee && (
        <div className="page-card">
          <h3 className="section-title">Section 1 - Purpose & Leave Details</h3>
          <div className="form-grid" style={{ marginTop: "16px" }}>
            <div>
              <label>Purpose</label>
              <select value={form.purpose} onChange={(e) => handlePurposeChange(e.target.value)}>
                <option value="">Select</option>
                <option value="Home_Town">Home Town</option>
                <option value="All_India">All India</option>
                <option value="Retirement">Retirement</option>
                <option value="Expired">Expired</option>
                <option value="VRS">VRS</option>
                <option value="Resigned">Resigned</option>
              </select>
              {errors.purpose && <p className="error-text">{errors.purpose}</p>}
            </div>

            {form.purpose && (
              <div>
                <label>{getEventDateLabel(form.purpose)}</label>
                <input type="date" lang="en-GB" value={form.eventDate} onChange={(e) => { updateForm("eventDate", e.target.value); if (isLtcPurpose(form.purpose)) generateBlocks(e.target.value, form.purpose); setErrors((prev) => ({ ...prev, eventDate: "" })); }} readOnly={form.purpose === "Retirement"} />
                {errors.eventDate && <p className="error-text">{errors.eventDate}</p>}
              </div>
            )}

            {isLtcPurpose(form.purpose) && availableBlocks.length > 0 && (
              <div>
                <label>Block Period</label>
                <select value={form.blockPeriod} onChange={(e) => { updateForm("blockPeriod", e.target.value); setErrors((prev) => ({ ...prev, blockPeriod: "" })); }}>
                  <option value="">Select Block</option>
                  {availableBlocks.map((block) => <option key={block} value={block}>{block}</option>)}
                </select>
                {errors.blockPeriod && <p className="error-text">{errors.blockPeriod}</p>}
              </div>
            )}

            {form.purpose && <div><label>EL Days</label><input type="number" value={form.elDays} min="0" max={isLtcPurpose(form.purpose) ? 10 : 300} onChange={(e) => { updateForm("elDays", Number(e.target.value)); setErrors((prev) => ({ ...prev, elDays: "", totalLeave: "" })); }} />{errors.elDays && <p className="error-text">{errors.elDays}</p>}</div>}

            {form.purpose && !isLtcPurpose(form.purpose) && <div><label>HPL Days</label><input type="number" value={form.hplDays} min="0" max="300" onChange={(e) => { updateForm("hplDays", Number(e.target.value)); setErrors((prev) => ({ ...prev, hplDays: "", totalLeave: "" })); }} />{errors.hplDays && <p className="error-text">{errors.hplDays}</p>}{errors.totalLeave && <p className="error-text">{errors.totalLeave}</p>}</div>}

            <div><label>DO Part Number</label><input type="text" value={form.doPartNumber} onChange={(e) => { updateForm("doPartNumber", e.target.value); setErrors((prev) => ({ ...prev, doPartNumber: "" })); }} />{errors.doPartNumber && <p className="error-text">{errors.doPartNumber}</p>}</div>
            <div><label>DO Part Date</label><input type="date" lang="en-GB" value={form.doPartDate} onChange={(e) => { updateForm("doPartDate", e.target.value); setErrors((prev) => ({ ...prev, doPartDate: "" })); }} />{errors.doPartDate && <p className="error-text">{errors.doPartDate}</p>}</div>
            <div style={{ alignSelf: "end" }}><button onClick={() => { if (!validateSection1()) return; calculateAmounts(); }} disabled={!canCalculate}>Go</button></div>
          </div>
        </div>
      )}

      {showCalculation && <div className="page-card"><h3 className="section-title">Section 2 - Calculation</h3><div className="form-grid" style={{ marginTop: "16px" }}><div><label>EL Amount</label><input value={formatAmount(form.elAmount)} readOnly /></div><div><label>HPL Amount</label><input value={formatAmount(form.hplAmount)} readOnly /></div><div><label>Total Amount</label><input value={formatAmount(form.totalAmount)} readOnly /></div></div></div>}

      {showCalculation && (
        <div className="page-card">
          <h3 className="section-title">Section 3 - Tax & Recovery</h3>
          <div className="form-grid" style={{ marginTop: "16px" }}>
            <div><label>IT Recovery Applicable</label><select value={form.itApplicable} onChange={(e) => { updateForm("itApplicable", e.target.value); setErrors((prev) => ({ ...prev, itApplicable: "", itPercent: "" })); }}><option value="">Select</option><option value="Yes">Yes</option><option value="No">No</option></select>{errors.itApplicable && <p className="error-text">{errors.itApplicable}</p>}</div>
            {form.itApplicable === "Yes" && <div><label>IT %</label><select value={form.itPercent} onChange={(e) => { updateForm("itPercent", e.target.value); setErrors((prev) => ({ ...prev, itPercent: "" })); }}><option value="">Select</option>{[0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50].map((value) => <option key={value} value={value}>{value}%</option>)}</select>{errors.itPercent && <p className="error-text">{errors.itPercent}</p>}</div>}
            {form.itApplicable === "Yes" && <div style={{ alignSelf: "end" }}><button onClick={computeIT}>Calculate IT</button></div>}
            {form.itApplicable && <><div><label>IT Amount</label><input type="number" value={form.itAmount} onChange={(e) => handleITAmountChange(e.target.value)} /></div><div><label>Edu Cess (4%)</label><input value={formatAmount(form.eduCess)} readOnly /></div><div><label>IT Recovery</label><input value={formatAmount(form.itRecovery)} readOnly /></div></>}
            <div><label>Other Recovery</label><input type="number" value={form.otherRecovery} onChange={(e) => { updateForm("otherRecovery", e.target.value); setErrors((prev) => ({ ...prev, otherRecovery: "", otherRemark: "" })); }} />{errors.otherRecovery && <p className="error-text">{errors.otherRecovery}</p>}</div>
            <div><label>Other Recovery Remark</label><input type="text" value={form.otherRemark} onChange={(e) => { updateForm("otherRemark", e.target.value); setErrors((prev) => ({ ...prev, otherRemark: "" })); }} />{errors.otherRemark && <p className="error-text">{errors.otherRemark}</p>}</div>
            <div><label>Other Taxable</label><input type="number" value={form.otherTaxable} onChange={(e) => { updateForm("otherTaxable", e.target.value); setErrors((prev) => ({ ...prev, otherTaxable: "", otherTaxableRemark: "" })); }} />{errors.otherTaxable && <p className="error-text">{errors.otherTaxable}</p>}</div>
            <div><label>Other Taxable Remark</label><input type="text" value={form.otherTaxableRemark} onChange={(e) => { updateForm("otherTaxableRemark", e.target.value); setErrors((prev) => ({ ...prev, otherTaxableRemark: "" })); }} />{errors.otherTaxableRemark && <p className="error-text">{errors.otherTaxableRemark}</p>}</div>
            <div style={{ alignSelf: "end" }}><button onClick={computeGrandTotal}>Final Calculate</button></div>
            {finalCalculated && <div><label>Grand Total</label><input value={formatAmount(form.grandTotal)} readOnly /></div>}
          </div>

          {finalCalculated && !editingId && <div className="actions-row" style={{ marginTop: "18px" }}><button onClick={saveBill} disabled={isSubmitting}>{isSubmitting ? "Saving..." : "Save Bill"}</button><span className="helper-text">After saving, the form is cleared and the new record appears in the table below.</span></div>}
          {finalCalculated && editingId && <div className="actions-row" style={{ marginTop: "18px" }}><button onClick={updateBill} disabled={isSubmitting}>{isSubmitting ? "Updating..." : "Update Bill"}</button><button onClick={deleteBill} disabled={isSubmitting} style={{ background: "linear-gradient(135deg, #d32f2f 0%, #b71c1c 100%)" }}>{isSubmitting ? "Deleting..." : "Delete Bill"}</button></div>}
        </div>
      )}

      <div className="page-card">
        <div className="toolbar-row">
          <div>
            <h3 className="section-title">Records Without Bill No</h3>
            <p className="section-subtitle">Current filter: {tableCategoryLabel}</p>
          </div>
          <div className="toolbar-actions"><div><label>Category</label><select value={tableCategory} onChange={(e) => setTableCategory(e.target.value)}><option value="all">All</option><option value="officer">Officer</option><option value="staff">Staff</option></select></div></div>
        </div>
        <div className="table-wrap" style={{ maxHeight: "320px" }}>
          <table>
            <thead>
              <tr><th>Emp Code</th><th>Name</th><th>Division</th><th>Category</th><th>Purpose</th><th>LTC/Event Date</th><th>Block Period</th><th>EL Days</th><th>HPL Days</th><th>DO No</th><th>DO Date</th><th>Total Amount</th><th>Action</th></tr>
            </thead>
            <tbody>
              {pendingRecords.map((record) => (
                <tr key={record.id}>
                  <td>{record.personnel?.empCode || "-"}</td><td>{record.personnel?.name || "-"}</td><td>{record.personnel?.division || "-"}</td><td>{formatDisgType(record.personnel?.disgType)}</td><td>{record.purpose}</td><td>{formatDate(record.eventDate)}</td><td>{record.blockPeriod || "-"}</td><td>{record.elDays}</td><td>{record.hplDays}</td><td>{record.doPartNumber}</td><td>{formatDate(record.doPartDate)}</td><td>{formatAmount(record.totalAmount)}</td><td><button onClick={() => editRecord(record.id)}>Update/Delete</button></td>
                </tr>
              ))}
              {!isPendingLoading && pendingRecords.length === 0 && <tr><td colSpan="13" style={{ textAlign: "center" }}>No records found</td></tr>}
            </tbody>
          </table>
        </div>
        {isPendingLoading && <p className="helper-text">Loading records...</p>}
      </div>
    </div>
  );
}

export default PrepareBill;
