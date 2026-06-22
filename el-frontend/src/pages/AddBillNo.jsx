import { useCallback, useEffect, useMemo, useState } from "react";
import { getApiErrorMessage } from "../utils/api";
import { formatAmount, formatDate, formatDisgType } from "../utils/formatters";

function AddBillNo() {
  const [pendingRecords, setPendingRecords] = useState([]);
  const [billedRecords, setBilledRecords] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [billNo, setBillNo] = useState("");
  const [billDate, setBillDate] = useState("");
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

  const loadData = useCallback(async () => {
    setIsLoading(true);
    setSelectedIds([]);
    try {
      const [pendingResponse, billedResponse] = await Promise.all([
        fetch(`http://localhost:8080/api/encashment/pending-bills?category=${category}`),
        fetch(`http://localhost:8080/api/encashment/billed-records?category=${category}`)
      ]);
      if (!pendingResponse.ok || !billedResponse.ok) throw new Error("Unable to load bill records");
      setPendingRecords(await pendingResponse.json());
      setBilledRecords(await billedResponse.json());
    } catch (error) {
      setPendingRecords([]);
      setBilledRecords([]);
      showStatus("error", error.message || "Unable to load bill records");
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
    if (!/^\d{3}$/.test(billNo.trim())) nextErrors.billNo = "Bill No must be exactly 3 digits";
    if (!billDate) {
      nextErrors.billDate = "Select bill date";
    } else {
      if (selectedRecords.some((record) => record.createdDate && record.createdDate > billDate)) {
        nextErrors.billDate = "Bill date cannot be before created date";
      }
    }
    if (selectedIds.length === 0) nextErrors.selectedIds = "Select at least one record";
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const toggleSelect = (id) => {
    setSelectedIds((prev) => (prev.includes(id) ? prev.filter((value) => value !== id) : [...prev, id]));
    setErrors((prev) => ({ ...prev, selectedIds: "" }));
  };

  const toggleSelectAll = () => {
    setSelectedIds((prev) => (prev.length === pendingRecords.length ? [] : pendingRecords.map((record) => record.id)));
    setErrors((prev) => ({ ...prev, selectedIds: "" }));
  };

  const handleSubmit = async () => {
    if (!validateForm()) return;
    setIsSubmitting(true);
    showStatus("", "");
    try {
      const response = await fetch("http://localhost:8080/api/encashment/add-bill-no", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ids: selectedIds, billNo: billNo.trim(), billDate })
      });
      if (!response.ok) {
        const message = await getApiErrorMessage(response, "Unable to save Bill No");
        throw new Error(message || "Unable to save Bill No");
      }
      setBillNo("");
      setBillDate("");
      setErrors({});
      showStatus("success", "Bill number added successfully");
      await loadData();
    } catch (error) {
      showStatus("error", error.message || "Unable to save Bill No");
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
      {statusMessage && (
        <div className={`status-banner ${statusType === "error" ? "is-error" : "is-success"}`}>
          {statusMessage}
        </div>
      )}

      <div className="page-card">
        <div className="toolbar-row">
          <div>
            <h2 className="section-title">Records Without Bill No</h2>
            <p className="section-subtitle">Filtered by: {categoryLabel}</p>
          </div>
          <div className="toolbar-actions">
            <div>
              <label>Show Rows</label>
              <select value={pageSize} onChange={(e) => setPageSize(Number(e.target.value))}>
                <option value={5}>5</option>
                <option value={10}>10</option>
              </select>
            </div>
            <div>
              <label>Category</label>
              <select value={category} onChange={(e) => setCategory(e.target.value)}>
                <option value="all">All</option>
                <option value="officer">Officer</option>
                <option value="staff">Staff</option>
              </select>
            </div>
            <button onClick={toggleSelectAll} disabled={!pendingRecords.length}>
              {selectedIds.length === pendingRecords.length && pendingRecords.length > 0 ? "Unselect All" : "Select All"}
            </button>
          </div>
        </div>
        {errors.selectedIds && <p className="error-text">{errors.selectedIds}</p>}
        <div className="table-wrap" style={{ maxHeight: tableHeight }}>
          <table>
            <thead>
              <tr>
                <th>Select</th>
                <th>Emp Code</th>
                <th>Name</th>
                <th>Division</th>
                <th>Category</th>
                <th>Purpose</th>
                <th>LTC/Event Date</th>
                <th>Block Period</th>
                <th>EL Days</th>
                <th>HPL Days</th>
                <th>DO No</th>
                <th>DO Date</th>
                <th>Total Amount</th>
              </tr>
            </thead>
            <tbody>
              {pendingRecords.map((record) => (
                <tr key={record.id}>
                  <td><input type="checkbox" checked={selectedIds.includes(record.id)} onChange={() => toggleSelect(record.id)} /></td>
                  <td>{record.personnel?.empCode || "-"}</td>
                  <td>{record.personnel?.name || "-"}</td>
                  <td>{record.personnel?.division || "-"}</td>
                  <td>{formatDisgType(record.personnel?.disgType)}</td>
                  <td>{record.purpose}</td>
                  <td>{formatDate(record.eventDate)}</td>
                  <td>{record.blockPeriod || "-"}</td>
                  <td>{record.elDays}</td>
                  <td>{record.hplDays}</td>
                  <td>{record.doPartNumber}</td>
                  <td>{formatDate(record.doPartDate)}</td>
                  <td>{formatAmount(record.totalAmount)}</td>
                </tr>
              ))}
              {!isLoading && pendingRecords.length === 0 && <tr><td colSpan="13" style={{ textAlign: "center" }}>No pending bill records found</td></tr>}
            </tbody>
          </table>
        </div>
      </div>

      <div className="page-card">
        <div className="toolbar-row">
          <div>
            <h3 className="section-title">Bill Details</h3>
            <p className="section-subtitle">Enter one Bill No and Bill Date for the selected records.</p>
          </div>
        </div>
        <div className="form-grid">
          <div>
            <label>Bill No</label>
            <input
              placeholder="Enter 3 digit Bill No"
              value={billNo}
              onChange={(e) => {
                setBillNo(e.target.value);
                setErrors((prev) => ({ ...prev, billNo: "" }));
              }}
            />
            {errors.billNo && <p className="error-text">{errors.billNo}</p>}
          </div>
          <div>
            <label>Bill Date</label>
            <input
              type="date"
              lang="en-GB"
              value={billDate}
              onChange={(e) => {
                setBillDate(e.target.value);
                setErrors((prev) => ({ ...prev, billDate: "" }));
              }}
            />
            {errors.billDate && <p className="error-text">{errors.billDate}</p>}
          </div>
          <div style={{ alignSelf: "end" }}>
            <button onClick={handleSubmit} disabled={isSubmitting || isLoading || pendingRecords.length === 0}>
              {isSubmitting ? "Saving..." : "Save Bill Number"}
            </button>
          </div>
        </div>
      </div>

      <div className="page-card">
        <div className="toolbar-row">
          <div>
            <h3 className="section-title">Records With Bill No</h3>
            <p className="section-subtitle">Processed records for the selected category.</p>
          </div>
        </div>
        <div className="table-wrap" style={{ maxHeight: tableHeight }}>
          <table>
            <thead>
              <tr>
                <th>Bill No</th>
                <th>Bill Date</th>
                <th>Emp Code</th>
                <th>Name</th>
                <th>Division</th>
                <th>Category</th>
                <th>Purpose</th>
                <th>LTC/Event Date</th>
                <th>Block Period</th>
                <th>EL Days</th>
                <th>HPL Days</th>
                <th>DO No</th>
                <th>DO Date</th>
                <th>Total Amount</th>
              </tr>
            </thead>
            <tbody>
              {billedRecords.map((record) => (
                <tr key={record.id}>
                  <td>{record.billNo}</td>
                  <td>{formatDate(record.billDate)}</td>
                  <td>{record.personnel?.empCode || "-"}</td>
                  <td>{record.personnel?.name || "-"}</td>
                  <td>{record.personnel?.division || "-"}</td>
                  <td>{formatDisgType(record.personnel?.disgType)}</td>
                  <td>{record.purpose}</td>
                  <td>{formatDate(record.eventDate)}</td>
                  <td>{record.blockPeriod || "-"}</td>
                  <td>{record.elDays}</td>
                  <td>{record.hplDays}</td>
                  <td>{record.doPartNumber}</td>
                  <td>{formatDate(record.doPartDate)}</td>
                  <td>{formatAmount(record.totalAmount)}</td>
                </tr>
              ))}
              {!isLoading && billedRecords.length === 0 && <tr><td colSpan="14" style={{ textAlign: "center" }}>No billed records found</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default AddBillNo;
