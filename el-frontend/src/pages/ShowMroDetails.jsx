import { useCallback, useEffect, useMemo, useState } from "react";
import { formatAmount, formatDate, formatDisgType } from "../utils/formatters";

function ShowMroDetails() {
  const [records, setRecords] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [category, setCategory] = useState("all");

  const fetchRecords = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const response = await fetch(`http://localhost:8080/api/encashment/mro-details?category=${category}`);
      if (!response.ok) {
        throw new Error("Unable to load MRO details");
      }

      const data = await response.json();
      setRecords(data);
    } catch (error) {
      setRecords([]);
      setErrorMessage(error.message || "Unable to load MRO details");
    } finally {
      setIsLoading(false);
    }
  }, [category]);

  useEffect(() => {
    fetchRecords();
  }, [fetchRecords]);

  const categoryLabel = useMemo(() => {
    if (category === "officer") return "Officer";
    if (category === "staff") return "Staff";
    return "All";
  }, [category]);

  const filteredRecords = useMemo(() => {
    if (category === "officer") {
      return records.filter((record) => record.personnel?.disgType === 1);
    }
    if (category === "staff") {
      return records.filter((record) => record.personnel?.disgType === 2 || record.personnel?.disgType === 3);
    }
    return records;
  }, [records, category]);

  return (
    <div>
      {errorMessage && <div className="status-banner is-error">{errorMessage}</div>}

      <div className="page-card">
        <div className="toolbar-row">
          <div>
            <h2 className="section-title">MRO Records</h2>
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
          </div>
        </div>
        <div className="table-wrap" style={{ maxHeight: "460px" }}>
          <table>
            <thead>
              <tr>
                <th>Emp Code</th>
                <th>Employee</th>
                <th>Category</th>
                <th>Purpose</th>
                <th>Bill No</th>
                <th>Bill Date</th>
                <th>DV No</th>
                <th>DV Date</th>
                <th>DV Amount</th>
                <th>Sum DV Amount</th>
                <th>MRO No</th>
                <th>MRO Date</th>
                <th>MRO Amount</th>
              </tr>
            </thead>

            <tbody>
              {filteredRecords.map((record) => (
                <tr key={record.id}>
                  <td>{record.personnel?.empCode || "-"}</td>
                  <td>{record.personnel?.name || record.empId}</td>
                  <td>{formatDisgType(record.personnel?.disgType)}</td>
                  <td>{record.purpose}</td>
                  <td>{record.billNo}</td>
                  <td>{formatDate(record.billDate)}</td>
                  <td>{record.dvNo}</td>
                  <td>{formatDate(record.dvDate)}</td>
                  <td>{formatAmount(record.dvAmount)}</td>
                  <td>{formatAmount(record.sumDvAmount)}</td>
                  <td>{record.mroNo}</td>
                  <td>{formatDate(record.mroDate)}</td>
                  <td>{formatAmount(record.mroAmount)}</td>
                </tr>
              ))}
              {!isLoading && filteredRecords.length === 0 && (
                <tr>
                  <td colSpan="13" style={{ textAlign: "center" }}>
                    No MRO records found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {isLoading && <p className="helper-text">Loading MRO details...</p>}
      </div>
    </div>
  );
}

export default ShowMroDetails;
