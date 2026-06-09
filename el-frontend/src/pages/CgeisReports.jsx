import { useCallback, useEffect, useMemo, useState } from "react";
import { formatAmount, formatDate, formatDisgType } from "../utils/formatters";

function CgeisReports() {
  const [records, setRecords] = useState([]);
  const [category, setCategory] = useState("all");
  const [statusMessage, setStatusMessage] = useState("");

  const loadReports = useCallback(async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/cgeis/reports?category=${category}`);
      if (!response.ok) throw new Error("Unable to load CGEIS reports");
      setRecords(await response.json());
      setStatusMessage("");
    } catch (error) {
      setRecords([]);
      setStatusMessage(error.message || "Unable to load CGEIS reports");
    }
  }, [category]);

  useEffect(() => {
    loadReports();
  }, [loadReports]);

  const categoryLabel = useMemo(() => category === "officer" ? "Officer" : category === "staff" ? "Staff" : "All", [category]);

  return (
    <div>
      {statusMessage && <div className="status-banner is-error">{statusMessage}</div>}
      <div className="page-card">
        <div className="toolbar-row">
          <div>
            <h3 className="section-title">CGEIS Reports</h3>
            <p className="section-subtitle">Filtered by: {categoryLabel}. Reports open in a new browser tab.</p>
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
          </div>
        </div>
        <div className="table-wrap" style={{ maxHeight: "420px" }}>
          <table>
            <thead>
              <tr><th>Reason</th><th>Bill No</th><th>Bill Date</th><th>Emp Code</th><th>Name</th><th>Category</th><th>Total Amount</th><th>Insurance Coverage</th><th>Other Recovery</th><th>Net Amount</th><th>Action</th></tr>
            </thead>
            <tbody>
              {records.map((row) => (
                <tr key={row.id}>
                  <td>{row.reason || "-"}</td><td>{row.billNo}</td><td>{formatDate(row.billDate)}</td><td>{row.personnel?.empCode || "-"}</td><td>{row.personnel?.name || "-"}</td><td>{formatDisgType(row.personnel?.disgType)}</td><td>{formatAmount(row.totalAmount)}</td><td>{formatAmount(row.insuranceCoverage)}</td><td>{formatAmount(row.otherRecovery)}</td><td>{formatAmount(row.netPay)}</td>
                  <td>
                    <div className="actions-row">
                      <button type="button" onClick={() => window.open(`http://localhost:8080/api/cgeis/bill-report/${row.id}`, "_blank")}>View Deduction</button>
                      <button type="button" onClick={() => window.open(`http://localhost:8080/api/cgeis/it-report?billNo=${encodeURIComponent(row.billNo)}`, "_blank")}>View Sanction</button>
                    </div>
                  </td>
                </tr>
              ))}
              {records.length === 0 && <tr><td colSpan="11" style={{ textAlign: "center" }}>No CGEIS reports found</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default CgeisReports;
