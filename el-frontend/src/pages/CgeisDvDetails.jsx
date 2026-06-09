import { useCallback, useEffect, useMemo, useState } from "react";
import { formatAmount, formatDate, formatDisgType } from "../utils/formatters";

function CgeisDvDetails() {
  const [pendingRecords, setPendingRecords] = useState([]);
  const [processedRecords, setProcessedRecords] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [dvNo, setDvNo] = useState("");
  const [dvDate, setDvDate] = useState("");
  const [dvAmount, setDvAmount] = useState("");
  const [category, setCategory] = useState("all");
  const [errors, setErrors] = useState({});
  const [statusMessage, setStatusMessage] = useState("");
  const [statusType, setStatusType] = useState("");

  const showStatus = (type, message) => {
    setStatusType(type);
    setStatusMessage(message);
  };

  const loadData = useCallback(async () => {
    try {
      const [pendingResponse, processedResponse] = await Promise.all([
        fetch(`http://localhost:8080/api/cgeis/pending-dv?category=${category}`),
        fetch(`http://localhost:8080/api/cgeis/processed-dv?category=${category}`)
      ]);
      if (!pendingResponse.ok || !processedResponse.ok) throw new Error("Unable to load CGEIS DV records");
      setPendingRecords(await pendingResponse.json());
      setProcessedRecords(await processedResponse.json());
      setSelectedIds([]);
    } catch (error) {
      setPendingRecords([]);
      setProcessedRecords([]);
      showStatus("error", error.message || "Unable to load CGEIS DV records");
    }
  }, [category]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const validate = () => {
    const nextErrors = {};
    const selectedRecords = pendingRecords.filter((record) => selectedIds.includes(record.id));
    if (!/^\d{4}$/.test(dvNo.trim())) nextErrors.dvNo = "DV No must be exactly 4 digits";
    if (!dvDate) nextErrors.dvDate = "Select DV date";
    else if (selectedRecords.some((record) => record.billDate && record.billDate > dvDate)) nextErrors.dvDate = "DV date cannot be before bill date";
    if (!dvAmount || Number(dvAmount) <= 0) nextErrors.dvAmount = "Enter valid DV amount";
    if (selectedIds.length === 0) nextErrors.selectedIds = "Select at least one record";
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const submitDv = async () => {
    if (!validate()) return;
    try {
      const response = await fetch("http://localhost:8080/api/cgeis/dv", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ids: selectedIds,
          dvNo: dvNo.trim(),
          dvDate,
          dvAmount: Number(dvAmount)
        })
      });
      if (!response.ok) throw new Error(await response.text() || "Unable to save DV details");
      setDvNo("");
      setDvDate("");
      setDvAmount("");
      setErrors({});
      showStatus("success", "CGEIS DV details saved");
      await loadData();
    } catch (error) {
      showStatus("error", error.message || "Unable to save DV details");
    }
  };

  const categoryLabel = useMemo(() => category === "officer" ? "Officer" : category === "staff" ? "Staff" : "All", [category]);

  return (
    <div>
      {statusMessage && <div className={`status-banner ${statusType === "error" ? "is-error" : "is-success"}`}>{statusMessage}</div>}

      <div className="page-card">
        <div className="toolbar-row">
          <div>
            <h3 className="section-title">Pending CGEIS DV Bills</h3>
            <p className="section-subtitle">Filtered by: {categoryLabel}</p>
          </div>
          <div className="toolbar-actions">
            <div>
              <label>Category</label>
              <select value={category} onChange={(e) => setCategory(e.target.value)}>
                <option value="all">All</option>
                <option value="officer">Officer</option>
                <option value="staff">Staff</option>
              </select>
            </div>
            <button onClick={() => setSelectedIds(selectedIds.length === pendingRecords.length ? [] : pendingRecords.map((row) => row.id))}>
              {selectedIds.length === pendingRecords.length && pendingRecords.length > 0 ? "Unselect All" : "Select All"}
            </button>
          </div>
        </div>
        {errors.selectedIds && <p className="error-text">{errors.selectedIds}</p>}
        <div className="table-wrap" style={{ maxHeight: "320px" }}>
          <table>
            <thead>
              <tr><th>Select</th><th>Emp ID</th><th>Reason</th><th>Bill No</th><th>Bill Date</th><th>DO Part No</th><th>DO Part Date</th><th>Ins Rec</th><th>Other Recovery</th><th>Net Amount</th></tr>
            </thead>
            <tbody>
              {pendingRecords.map((row) => (
                <tr key={row.id}>
                  <td><input type="checkbox" checked={selectedIds.includes(row.id)} onChange={() => setSelectedIds((prev) => prev.includes(row.id) ? prev.filter((id) => id !== row.id) : [...prev, row.id])} /></td>
                  <td>{row.personnel?.empCode || "-"}</td><td>{row.reason || "-"}</td><td>{row.billNo}</td><td>{formatDate(row.billDate)}</td><td>{row.doPartNumber || "-"}</td><td>{formatDate(row.doPartDate)}</td><td>{formatAmount(row.insuranceCoverage)}</td><td>{formatAmount(row.otherRecovery)}</td><td>{formatAmount(row.netPay)}</td>
                </tr>
              ))}
              {pendingRecords.length === 0 && <tr><td colSpan="10" style={{ textAlign: "center" }}>No pending CGEIS DV records found</td></tr>}
            </tbody>
          </table>
        </div>
      </div>

      <div className="page-card">
        <div className="form-grid">
          <div><label>DV No</label><input value={dvNo} onChange={(e) => { setDvNo(e.target.value); setErrors((prev) => ({ ...prev, dvNo: "" })); }} />{errors.dvNo && <p className="error-text">{errors.dvNo}</p>}</div>
          <div><label>DV Date</label><input type="date" value={dvDate} onChange={(e) => { setDvDate(e.target.value); setErrors((prev) => ({ ...prev, dvDate: "" })); }} />{errors.dvDate && <p className="error-text">{errors.dvDate}</p>}</div>
          <div><label>DV Amount</label><input type="number" value={dvAmount} onChange={(e) => { setDvAmount(e.target.value); setErrors((prev) => ({ ...prev, dvAmount: "" })); }} />{errors.dvAmount && <p className="error-text">{errors.dvAmount}</p>}</div>
          <div style={{ alignSelf: "end" }}><button onClick={submitDv}>Save DV Details</button></div>
        </div>
      </div>

      <div className="page-card">
        <div className="toolbar-row"><div><h3 className="section-title">Processed CGEIS DV Bills</h3></div></div>
        <div className="table-wrap" style={{ maxHeight: "320px" }}>
          <table>
            <thead>
              <tr><th>Emp ID</th><th>Reason</th><th>Bill No</th><th>Bill Date</th><th>DO Part No</th><th>DO Part Date</th><th>DV No</th><th>DV Date</th><th>DV Amount</th><th>Net Amount</th></tr>
            </thead>
            <tbody>
              {processedRecords.map((row) => (
                <tr key={row.id}>
                  <td>{row.personnel?.empCode || "-"}</td><td>{row.reason || "-"}</td><td>{row.billNo}</td><td>{formatDate(row.billDate)}</td><td>{row.doPartNumber || "-"}</td><td>{formatDate(row.doPartDate)}</td><td>{row.dvNo}</td><td>{formatDate(row.dvDate)}</td><td>{formatAmount(row.dvAmount)}</td><td>{formatAmount(row.netPay)}</td>
                </tr>
              ))}
              {processedRecords.length === 0 && <tr><td colSpan="10" style={{ textAlign: "center" }}>No processed CGEIS DV records found</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default CgeisDvDetails;
