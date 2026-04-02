import { useCallback, useEffect, useMemo, useState } from "react";
import { formatAmount, formatDate, formatDisgType } from "../utils/formatters";

function AddDvNo() {
  const [pendingRecords, setPendingRecords] = useState([]);
  const [processedRecords, setProcessedRecords] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [dvNo, setDvNo] = useState("");
  const [dvDate, setDvDate] = useState("");
  const [dvAmount, setDvAmount] = useState("");
  const [dvBalance, setDvBalance] = useState("");
  const [recoveryCda, setRecoveryCda] = useState("");
  const [cdaRemarks, setCdaRemarks] = useState("");
  const [recoveryCdaTax, setRecoveryCdaTax] = useState("");
  const [cdaTaxRemarks, setCdaTaxRemarks] = useState("");
  const [category, setCategory] = useState("all");
  const [pageSize, setPageSize] = useState(5);
  const [errors, setErrors] = useState({});
  const [statusMessage, setStatusMessage] = useState("");
  const [statusType, setStatusType] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const showStatus = (type, message) => {
    setStatusType(type);
    setStatusMessage(message);
  };

  const parseOptionalNumber = (value) => {
    if (value === "" || value === null || value === undefined) return null;
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : Number.NaN;
  };

  const loadData = useCallback(async () => {
    setIsLoading(true);
    setSelectedIds([]);
    try {
      const [pendingResponse, processedResponse] = await Promise.all([
        fetch(`http://localhost:8080/api/encashment/pending-dv?category=${category}`),
        fetch(`http://localhost:8080/api/encashment/dv-records?category=${category}`)
      ]);
      if (!pendingResponse.ok || !processedResponse.ok) throw new Error("Unable to load DV records");
      setPendingRecords(await pendingResponse.json());
      setProcessedRecords(await processedResponse.json());
    } catch (error) {
      setPendingRecords([]);
      setProcessedRecords([]);
      showStatus("error", error.message || "Unable to load DV records");
    } finally {
      setIsLoading(false);
    }
  }, [category]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const validateForm = () => {
    const nextErrors = {};
    const selectedRecords = pendingRecords.filter((record) => selectedIds.includes(record.id));
    const parsedDvAmount = parseOptionalNumber(dvAmount);
    const parsedDvBalance = parseOptionalNumber(dvBalance);
    const parsedRecoveryCda = parseOptionalNumber(recoveryCda);
    const parsedRecoveryCdaTax = parseOptionalNumber(recoveryCdaTax);
    if (!/^\d{4}$/.test(dvNo.trim())) nextErrors.dvNo = "DV No must be exactly 4 digits";
    if (!dvDate) {
      nextErrors.dvDate = "Select DV date";
    } else {
      if (selectedRecords.some((record) => record.billDate && record.billDate > dvDate)) nextErrors.dvDate = "DV date cannot be before bill date";
    }
    if (selectedIds.length === 0) nextErrors.selectedIds = "Select at least one record";
    if (parsedDvAmount === null || Number.isNaN(parsedDvAmount) || parsedDvAmount <= 0) nextErrors.dvAmount = "Enter valid DV amount greater than 0";
    if (parsedDvBalance !== null && (Number.isNaN(parsedDvBalance) || parsedDvBalance < 0)) nextErrors.dvBalance = "DV balance cannot be negative";
    if (parsedRecoveryCda !== null && (Number.isNaN(parsedRecoveryCda) || parsedRecoveryCda < 0)) nextErrors.recoveryCda = "Recovery CDA cannot be negative";
    if (parsedRecoveryCdaTax !== null && (Number.isNaN(parsedRecoveryCdaTax) || parsedRecoveryCdaTax < 0)) nextErrors.recoveryCdaTax = "Recovery CDA Tax cannot be negative";
    if (parsedRecoveryCda > 0 && !cdaRemarks.trim()) nextErrors.cdaRemarks = "Enter CDA remarks";
    if (parsedRecoveryCdaTax > 0 && !cdaTaxRemarks.trim()) nextErrors.cdaTaxRemarks = "Enter CDA tax remarks";
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const toggleRow = (id) => {
    setSelectedIds((prev) => prev.includes(id) ? prev.filter((value) => value !== id) : [...prev, id]);
    setErrors((prev) => ({ ...prev, selectedIds: "" }));
  };

  const toggleSelectAll = () => {
    setSelectedIds((prev) => prev.length === pendingRecords.length ? [] : pendingRecords.map((record) => record.id));
    setErrors((prev) => ({ ...prev, selectedIds: "" }));
  };

  const submitDv = async () => {
    if (!validateForm()) return;
    setIsSubmitting(true);
    showStatus("", "");
    try {
      const response = await fetch("http://localhost:8080/api/encashment/add-dv-no", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ids: selectedIds,
          dvNo: dvNo.trim(),
          dvDate,
          dvAmount: Number(dvAmount),
          dvBalance: parseOptionalNumber(dvBalance),
          recoveryCda: parseOptionalNumber(recoveryCda),
          cdaRemarks: cdaRemarks.trim(),
          recoveryCdaTax: parseOptionalNumber(recoveryCdaTax),
          cdaTaxRemarks: cdaTaxRemarks.trim()
        })
      });
      if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "Unable to save DV details");
      }
      setDvNo("");
      setDvDate("");
      setDvAmount("");
      setDvBalance("");
      setRecoveryCda("");
      setCdaRemarks("");
      setRecoveryCdaTax("");
      setCdaTaxRemarks("");
      setErrors({});
      showStatus("success", "DV details saved");
      await loadData();
    } catch (error) {
      showStatus("error", error.message || "Unable to save DV details");
    } finally {
      setIsSubmitting(false);
    }
  };

  const tableHeight = pageSize === 5 ? "280px" : "460px";

  const categoryLabel = useMemo(() => {
    if (category === "officer") return "Officer";
    if (category === "staff") return "Staff";
    return "All";
  }, [category]);

  return (
    <div>
      {statusMessage && <div className={`status-banner ${statusType === "error" ? "is-error" : "is-success"}`}>{statusMessage}</div>}

      <div className="page-card">
        <div className="toolbar-row">
          <div>
            <h2 className="section-title">Records Pending DV</h2>
            <p className="section-subtitle">Filtered by: {categoryLabel}</p>
          </div>
          <div className="toolbar-actions">
            <div>
              <label>Show Rows</label>
              <select value={pageSize} onChange={(e) => setPageSize(Number(e.target.value))}><option value={5}>5</option><option value={10}>10</option></select>
            </div>
            <div>
              <label>Category</label>
              <select value={category} onChange={(e) => setCategory(e.target.value)}>
                <option value="all">All</option>
                <option value="officer">Officer</option>
                <option value="staff">Staff</option>
              </select>
            </div>
            <button onClick={toggleSelectAll} disabled={!pendingRecords.length}>{selectedIds.length === pendingRecords.length && pendingRecords.length > 0 ? "Unselect All" : "Select All"}</button>
          </div>
        </div>
        {errors.selectedIds && <p className="error-text">{errors.selectedIds}</p>}
        <div className="table-wrap" style={{ maxHeight: tableHeight }}>
          <table>
            <thead>
              <tr>
                <th>Select</th><th>Bill No</th><th>Bill Date</th><th>Emp Code</th><th>Name</th><th>Division</th><th>Category</th><th>Purpose</th><th>LTC/Event Date</th><th>DO No</th><th>DO Date</th><th>Total Amount</th>
              </tr>
            </thead>
            <tbody>
              {pendingRecords.map((record) => (
                <tr key={record.id}>
                  <td><input type="checkbox" checked={selectedIds.includes(record.id)} onChange={() => toggleRow(record.id)} /></td>
                  <td>{record.billNo}</td>
                  <td>{formatDate(record.billDate)}</td>
                  <td>{record.personnel?.empCode || "-"}</td>
                  <td>{record.personnel?.name || "-"}</td>
                  <td>{record.personnel?.division || "-"}</td>
                  <td>{formatDisgType(record.personnel?.disgType)}</td>
                  <td>{record.purpose}</td>
                  <td>{formatDate(record.eventDate)}</td>
                  <td>{record.doPartNumber}</td>
                  <td>{formatDate(record.doPartDate)}</td>
                  <td>{formatAmount(record.totalAmount)}</td>
                </tr>
              ))}
              {!isLoading && pendingRecords.length === 0 && <tr><td colSpan="12" style={{ textAlign: "center" }}>No pending DV records found</td></tr>}
            </tbody>
          </table>
        </div>
      </div>

      <div className="page-card">
        <div className="toolbar-row">
          <div>
            <h3 className="section-title">DV Details</h3>
            <p className="section-subtitle">Enter one DV reference for the selected bill records.</p>
          </div>
        </div>
        <div className="form-grid">
          <div><label>DV No</label><input placeholder="Enter 4 digit DV No" value={dvNo} onChange={(e) => { setDvNo(e.target.value); setErrors((prev) => ({ ...prev, dvNo: "" })); }} />{errors.dvNo && <p className="error-text">{errors.dvNo}</p>}</div>
          <div><label>DV Date</label><input type="date" lang="en-GB" value={dvDate} onChange={(e) => { setDvDate(e.target.value); setErrors((prev) => ({ ...prev, dvDate: "" })); }} />{errors.dvDate && <p className="error-text">{errors.dvDate}</p>}</div>
          <div><label>DV Amount</label><input type="number" value={dvAmount} onChange={(e) => { setDvAmount(e.target.value); setErrors((prev) => ({ ...prev, dvAmount: "" })); }} />{errors.dvAmount && <p className="error-text">{errors.dvAmount}</p>}</div>
          <div><label>DV Balance</label><input type="number" value={dvBalance} onChange={(e) => { setDvBalance(e.target.value); setErrors((prev) => ({ ...prev, dvBalance: "" })); }} />{errors.dvBalance && <p className="error-text">{errors.dvBalance}</p>}</div>
          <div><label>Recovery CDA</label><input type="number" value={recoveryCda} onChange={(e) => { setRecoveryCda(e.target.value); setErrors((prev) => ({ ...prev, recoveryCda: "", cdaRemarks: "" })); }} />{errors.recoveryCda && <p className="error-text">{errors.recoveryCda}</p>}</div>
          <div><label>CDA Remarks</label><input value={cdaRemarks} onChange={(e) => { setCdaRemarks(e.target.value); setErrors((prev) => ({ ...prev, cdaRemarks: "" })); }} />{errors.cdaRemarks && <p className="error-text">{errors.cdaRemarks}</p>}</div>
          <div><label>Recovery CDA Tax</label><input type="number" value={recoveryCdaTax} onChange={(e) => { setRecoveryCdaTax(e.target.value); setErrors((prev) => ({ ...prev, recoveryCdaTax: "", cdaTaxRemarks: "" })); }} />{errors.recoveryCdaTax && <p className="error-text">{errors.recoveryCdaTax}</p>}</div>
          <div><label>CDA Tax Remarks</label><input value={cdaTaxRemarks} onChange={(e) => { setCdaTaxRemarks(e.target.value); setErrors((prev) => ({ ...prev, cdaTaxRemarks: "" })); }} />{errors.cdaTaxRemarks && <p className="error-text">{errors.cdaTaxRemarks}</p>}</div>
          <div style={{ alignSelf: "end" }}><button onClick={submitDv} disabled={isSubmitting || isLoading}>{isSubmitting ? "Saving..." : "Submit"}</button></div>
        </div>
      </div>

      <div className="page-card">
        <div className="toolbar-row">
          <div>
            <h3 className="section-title">Records With DV No</h3>
            <p className="section-subtitle">Processed DV records for the selected category.</p>
          </div>
        </div>
        <div className="table-wrap" style={{ maxHeight: tableHeight }}>
          <table>
            <thead>
              <tr>
                <th>DV No</th><th>DV Date</th><th>Bill No</th><th>Bill Date</th><th>Emp Code</th><th>Name</th><th>Division</th><th>Category</th><th>Purpose</th><th>DV Amount</th><th>DV Balance</th><th>Recovery CDA</th><th>Recovery CDA Tax</th>
              </tr>
            </thead>
            <tbody>
              {processedRecords.map((record) => (
                <tr key={record.id}>
                  <td>{record.dvNo}</td>
                  <td>{formatDate(record.dvDate)}</td>
                  <td>{record.billNo}</td>
                  <td>{formatDate(record.billDate)}</td>
                  <td>{record.personnel?.empCode || "-"}</td>
                  <td>{record.personnel?.name || "-"}</td>
                  <td>{record.personnel?.division || "-"}</td>
                  <td>{formatDisgType(record.personnel?.disgType)}</td>
                  <td>{record.purpose}</td>
                  <td>{formatAmount(record.dvAmount)}</td>
                  <td>{formatAmount(record.dvBalance)}</td>
                  <td>{formatAmount(record.recoveryCda)}</td>
                  <td>{formatAmount(record.recoveryCdaTax)}</td>
                </tr>
              ))}
              {!isLoading && processedRecords.length === 0 && <tr><td colSpan="13" style={{ textAlign: "center" }}>No processed DV records found</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default AddDvNo;
