import { useCallback, useEffect, useMemo, useState } from "react";
import { getApiErrorMessage } from "../utils/api";
import { formatAmount, formatDate, formatDisgType } from "../utils/formatters";

const emptyRangeForm = { fromMonth: "", toMonth: "", cgeis: "" };
const emptyBillForm = { doPartNumber: "", doPartDate: "", billNo: "", billDate: "", itAmount: "" };

const buildRowKey = (row) => `${row.fromMonth}_${row.toMonth}_${row.cgeis}`;

function CgeisPreparePage() {
  const [search, setSearch] = useState("");
  const [searchCategory, setSearchCategory] = useState("all");
  const [employees, setEmployees] = useState([]);
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [salaryRows, setSalaryRows] = useState([]);
  const [billHistory, setBillHistory] = useState([]);
  const [timesMap, setTimesMap] = useState({});
  const [valueMap, setValueMap] = useState({});
  const [rangeForm, setRangeForm] = useState(emptyRangeForm);
  const [billForm, setBillForm] = useState(emptyBillForm);
  const [errors, setErrors] = useState({});
  const [statusMessage, setStatusMessage] = useState("");
  const [statusType, setStatusType] = useState("");
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isAddRangeOpen, setIsAddRangeOpen] = useState(false);

  const showStatus = (type, message) => {
    setStatusType(type);
    setStatusMessage(message);
  };

  const clearStatus = () => {
    setStatusType("");
    setStatusMessage("");
  };

  const loadEmployeeData = useCallback(async (empId) => {
    if (!empId) return;
    setIsLoading(true);
    clearStatus();
    try {
      const [salaryResponse, historyResponse] = await Promise.all([
        fetch(`http://localhost:8080/api/cgeis/salary-grouped/${empId}`),
        fetch(`http://localhost:8080/api/cgeis/bill-history/${empId}`)
      ]);
      if (!salaryResponse.ok) {
        throw new Error(await getApiErrorMessage(salaryResponse, "Unable to load CGEIS details"));
      }
      if (!historyResponse.ok) {
        throw new Error(await getApiErrorMessage(historyResponse, "Unable to load CGEIS details"));
      }
      const salaryData = await salaryResponse.json();
      const historyData = await historyResponse.json();
      setSalaryRows(salaryData);
      setBillHistory(historyData);
      setTimesMap(Object.fromEntries(salaryData.map((row) => [buildRowKey(row), row.times ?? ""])));
      setValueMap(Object.fromEntries(salaryData.map((row) => [buildRowKey(row), row.value ?? ""])));
    } catch (error) {
      setSalaryRows([]);
      setBillHistory([]);
      showStatus("error", error.message || "Unable to load CGEIS details");
    } finally {
      setIsLoading(false);
    }
  }, []);

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
    const delay = setTimeout(() => searchEmployees(), 200);
    return () => clearTimeout(delay);
  }, [search, searchCategory, isSearchOpen, searchEmployees]);

  const filteredEmployees = useMemo(() => {
    if (searchCategory === "officer") return employees.filter((employee) => employee.disgType === 1);
    if (searchCategory === "staff") return employees.filter((employee) => employee.disgType === 2 || employee.disgType === 3);
    return employees;
  }, [employees, searchCategory]);

  const handleSelectEmployee = async (employee) => {
    setSelectedEmployee(employee);
    setSearch(`${employee.name} | ${employee.empCode}`);
    setEmployees([]);
    setIsSearchOpen(false);
    setErrors({});
    await loadEmployeeData(employee.id);
  };

  const selectedItems = salaryRows
    .map((row) => {
      const key = buildRowKey(row);
      const times = Number(timesMap[key] || 0);
      const value = Number(valueMap[key] || 0);
      return times > 0 ? { ...row, times, value } : null;
    })
    .filter(Boolean);

  const totalAmount = selectedItems.reduce((sum, row) => sum + Number(row.value) * row.times, 0);
  const itAmount = Number(billForm.itAmount || 0);
  const eduCess = itAmount * 0.04;
  const totalIt = itAmount + eduCess;
  const netPay = totalAmount - totalIt;

  const addRange = async () => {
    const nextErrors = {};
    if (!selectedEmployee) nextErrors.employee = "Select employee";
    if (!rangeForm.fromMonth) nextErrors.fromMonth = "Select from month";
    if (!rangeForm.toMonth) nextErrors.toMonth = "Select to month";
    if (!rangeForm.cgeis || Number(rangeForm.cgeis) <= 0) nextErrors.cgeis = "Enter valid CGEIS";
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    try {
      clearStatus();
      const response = await fetch("http://localhost:8080/api/cgeis/salary", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          empId: selectedEmployee.id,
          fromMonth: rangeForm.fromMonth,
          toMonth: rangeForm.toMonth,
          cgeis: Number(rangeForm.cgeis)
        })
      });
      if (!response.ok) throw new Error(await getApiErrorMessage(response, "Unable to add salary range"));
      setRangeForm(emptyRangeForm);
      setIsAddRangeOpen(false);
      showStatus("success", "CGEIS range added successfully");
      await loadEmployeeData(selectedEmployee.id);
    } catch (error) {
      showStatus("error", error.message || "Unable to add salary range");
    }
  };

  const deleteRange = async (row) => {
    if (!selectedEmployee) return;
    try {
      clearStatus();
      const response = await fetch("http://localhost:8080/api/cgeis/salary", {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ empId: selectedEmployee.id, fromMonth: row.fromMonth, toMonth: row.toMonth })
      });
      if (!response.ok) throw new Error(await getApiErrorMessage(response, "Unable to delete range"));
      showStatus("success", "CGEIS range deleted successfully");
      await loadEmployeeData(selectedEmployee.id);
    } catch (error) {
      showStatus("error", error.message || "Unable to delete range");
    }
  };

  const saveBill = async () => {
    const nextErrors = {};
    if (!selectedEmployee) nextErrors.employee = "Select employee";
    if (!billForm.doPartNumber.trim()) nextErrors.doPartNumber = "Enter DO Part number";
    if (!billForm.doPartDate) nextErrors.doPartDate = "Select DO Part date";
    if (!/^\d{3}$/.test(billForm.billNo.trim())) nextErrors.billNo = "Bill No must be exactly 3 digits";
    if (!billForm.billDate) nextErrors.billDate = "Select bill date";
    if (billForm.doPartDate && billForm.billDate && billForm.doPartDate > billForm.billDate) {
      nextErrors.doPartDate = "DO Part date cannot be after bill date";
    }
    if (Number(billForm.itAmount || 0) < 0) nextErrors.itAmount = "IT amount cannot be negative";
    if (selectedItems.length === 0) nextErrors.selectedRows = "Enter Times for at least one CGEIS row";
    if (selectedItems.some((item) => !item.value || Number(item.value) <= 0)) nextErrors.selectedValue = "Enter valid value for each selected row";
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    try {
      clearStatus();
      const response = await fetch("http://localhost:8080/api/cgeis/bill", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          empId: selectedEmployee.id,
          doPartNumber: billForm.doPartNumber.trim(),
          doPartDate: billForm.doPartDate,
          billNo: billForm.billNo.trim(),
          billDate: billForm.billDate,
          itAmount: Number(billForm.itAmount || 0),
          items: selectedItems.map((item) => ({
            fromMonth: item.fromMonth,
            toMonth: item.toMonth,
            cgeis: item.cgeis,
            value: Number(item.value),
            times: item.times
          }))
        })
      });
      if (!response.ok) throw new Error(await getApiErrorMessage(response, "Unable to save CGEIS bill"));
      setBillForm(emptyBillForm);
      showStatus("success", "CGEIS bill saved successfully");
      await loadEmployeeData(selectedEmployee.id);
    } catch (error) {
      showStatus("error", error.message || "Unable to save CGEIS bill");
    }
  };

  return (
    <div>
      {statusMessage && <div className={`status-banner ${statusType === "error" ? "is-error" : "is-success"}`}>{statusMessage}</div>}

      <div className="page-card">
        <div className="toolbar-row">
          <div>
            <h3 className="section-title">Search Employee</h3>
            <p className="section-subtitle">Select employee and manage monthly CGEIS ranges.</p>
          </div>
        </div>
        <div className="search-shell">
          <div className="search-inline">
            <input type="text" placeholder="Search employee by name or code" value={search} onChange={(e) => { setSearch(e.target.value); setIsSearchOpen(true); }} onFocus={() => { setIsSearchOpen(true); searchEmployees(); }} />
            <button onClick={searchEmployees}>Search</button>
          </div>
          {isSearchOpen && (
            <div className="search-dropdown">
              <div className="search-dropdown__header">
                <label>Category Filter</label>
                <select value={searchCategory} onChange={(e) => setSearchCategory(e.target.value)}>
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
                {filteredEmployees.length === 0 && <div style={{ padding: "12px 14px", color: "#64748b" }}>No employees found</div>}
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
              <p className="helper-text">Category: {formatDisgType(selectedEmployee.disgType)}</p>
              <p className="helper-text">DOB: {formatDate(selectedEmployee.dob)}</p>
            </div>
            <div>
              <h3 className="section-title">Finance Details</h3>
              <p className="helper-text">Basic Pay: {formatAmount(selectedEmployee.financeData?.basicPay || 0)}</p>
              <p className="helper-text">Special Pay: {formatAmount(selectedEmployee.financeData?.specialPay || 0)}</p>
              <p className="helper-text">GPF A/C No: {selectedEmployee.financeData?.gpfAccountNo || "-"}</p>
            </div>
          </div>
        </div>
      )}

      {selectedEmployee && (
        <div className="page-card">
          <div className="toolbar-row">
            <div>
              <h3 className="section-title">Add CGEIS Range</h3>
              <p className="section-subtitle">Monthly values are stored in DB and grouped automatically in the table below.</p>
            </div>
            <button type="button" onClick={() => setIsAddRangeOpen((prev) => !prev)}>
              {isAddRangeOpen ? "Hide Range Form" : "Add CGEIS Range"}
            </button>
          </div>
          {isAddRangeOpen && (
            <div className="form-grid">
              <div>
                <label>From Month</label>
                <input type="month" value={rangeForm.fromMonth ? rangeForm.fromMonth.slice(0, 7) : ""} onChange={(e) => setRangeForm((prev) => ({ ...prev, fromMonth: `${e.target.value}-01` }))} />
                {errors.fromMonth && <p className="error-text">{errors.fromMonth}</p>}
              </div>
              <div>
                <label>To Month</label>
                <input type="month" value={rangeForm.toMonth ? rangeForm.toMonth.slice(0, 7) : ""} onChange={(e) => setRangeForm((prev) => ({ ...prev, toMonth: `${e.target.value}-01` }))} />
                {errors.toMonth && <p className="error-text">{errors.toMonth}</p>}
              </div>
              <div>
                <label>CGEIS</label>
                <input type="number" value={rangeForm.cgeis} onChange={(e) => setRangeForm((prev) => ({ ...prev, cgeis: e.target.value }))} />
                {errors.cgeis && <p className="error-text">{errors.cgeis}</p>}
              </div>
              <div style={{ alignSelf: "end" }}>
                <button onClick={addRange}>Add New Range</button>
              </div>
            </div>
          )}
        </div>
      )}

      {selectedEmployee && (
        <div className="page-card">
          <div className="toolbar-row">
            <div>
              <h3 className="section-title">CGEIS Salary Ranges</h3>
              <p className="section-subtitle">Grouped consecutive months with the same CGEIS value.</p>
            </div>
          </div>
          {errors.selectedRows && <p className="error-text">{errors.selectedRows}</p>}
          {errors.selectedValue && <p className="error-text">{errors.selectedValue}</p>}
          <div className="table-wrap" style={{ maxHeight: "360px" }}>
            <table>
              <thead>
                <tr>
                  <th>From Month</th>
                  <th>To Month</th>
                  <th>CGEIS</th>
                  <th>Value</th>
                  <th>Times</th>
                  <th>Value x Times</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {salaryRows.map((row) => {
                  const key = buildRowKey(row);
                  const times = Number(timesMap[key] || 0);
                  return (
                    <tr key={key}>
                      <td>{formatDate(row.fromMonth).slice(3)}</td>
                      <td>{formatDate(row.toMonth).slice(3)}</td>
                      <td>{formatAmount(row.cgeis)}</td>
                      <td><input type="number" min="0" value={valueMap[key] ?? ""} onChange={(e) => setValueMap((prev) => ({ ...prev, [key]: e.target.value }))} /></td>
                      <td><input type="number" min="0" value={timesMap[key] ?? ""} onChange={(e) => setTimesMap((prev) => ({ ...prev, [key]: e.target.value }))} /></td>
                      <td>{times > 0 && Number(valueMap[key] || 0) > 0 ? formatAmount(Number(valueMap[key]) * times) : "-"}</td>
                      <td>
                        <div className="actions-row">
                          <button
                            type="button"
                            onClick={() => {
                              setRangeForm({ fromMonth: row.toMonth, toMonth: row.toMonth, cgeis: row.cgeis || "" });
                              setIsAddRangeOpen(true);
                            }}
                          >
                            Add New
                          </button>
                          <button type="button" onClick={() => deleteRange(row)} style={{ background: "linear-gradient(135deg, #d32f2f 0%, #b71c1c 100%)" }}>Delete</button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
                {!isLoading && salaryRows.length === 0 && <tr><td colSpan="7" style={{ textAlign: "center" }}>No CGEIS salary ranges found</td></tr>}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {selectedEmployee && (
        <div className="page-card">
          <div className="toolbar-row">
            <div>
              <h3 className="section-title">Prepare CGEIS Bill</h3>
              <p className="section-subtitle">Enter Times for the required rows, then save the bill.</p>
            </div>
          </div>
          <div className="form-grid">
            <div><label>DO Part Number</label><input value={billForm.doPartNumber} onChange={(e) => setBillForm((prev) => ({ ...prev, doPartNumber: e.target.value }))} />{errors.doPartNumber && <p className="error-text">{errors.doPartNumber}</p>}</div>
            <div><label>DO Part Date</label><input type="date" value={billForm.doPartDate} onChange={(e) => setBillForm((prev) => ({ ...prev, doPartDate: e.target.value }))} />{errors.doPartDate && <p className="error-text">{errors.doPartDate}</p>}</div>
            <div><label>Bill No</label><input value={billForm.billNo} onChange={(e) => setBillForm((prev) => ({ ...prev, billNo: e.target.value }))} />{errors.billNo && <p className="error-text">{errors.billNo}</p>}</div>
            <div><label>Bill Date</label><input type="date" value={billForm.billDate} onChange={(e) => setBillForm((prev) => ({ ...prev, billDate: e.target.value }))} />{errors.billDate && <p className="error-text">{errors.billDate}</p>}</div>
            <div><label>Income Tax</label><input type="number" value={billForm.itAmount} onChange={(e) => setBillForm((prev) => ({ ...prev, itAmount: e.target.value }))} />{errors.itAmount && <p className="error-text">{errors.itAmount}</p>}</div>
            <div><label>Educational Cess (4%)</label><input value={formatAmount(eduCess)} readOnly /></div>
            <div><label>Total CGEIS Amount</label><input value={formatAmount(totalAmount)} readOnly /></div>
            <div><label>Total IT Recovery</label><input value={formatAmount(totalIt)} readOnly /></div>
            <div><label>Net Pay</label><input value={formatAmount(netPay)} readOnly /></div>
            <div style={{ alignSelf: "end" }}><button onClick={saveBill}>Save CGEIS Bill</button></div>
          </div>
        </div>
      )}

      {selectedEmployee && (
        <div className="page-card">
          <div className="toolbar-row">
            <div>
              <h3 className="section-title">Saved CGEIS Bills</h3>
              <p className="section-subtitle">Latest bills prepared for the selected employee.</p>
            </div>
          </div>
          <div className="table-wrap" style={{ maxHeight: "260px" }}>
            <table>
              <thead>
                <tr>
                  <th>Bill No</th>
                  <th>Bill Date</th>
                  <th>Total Amount</th>
                  <th>Total IT</th>
                  <th>Net Pay</th>
                  <th>DV No</th>
                </tr>
              </thead>
              <tbody>
                {billHistory.map((row) => (
                  <tr key={row.id}>
                    <td>{row.billNo}</td>
                    <td>{formatDate(row.billDate)}</td>
                    <td>{formatAmount(row.totalAmount)}</td>
                    <td>{formatAmount(row.totalIt)}</td>
                    <td>{formatAmount(row.netPay)}</td>
                    <td>{row.dvNo || "-"}</td>
                  </tr>
                ))}
                {!isLoading && billHistory.length === 0 && <tr><td colSpan="6" style={{ textAlign: "center" }}>No CGEIS bills saved yet</td></tr>}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

export default CgeisPreparePage;
