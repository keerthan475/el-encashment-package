import { useCallback, useEffect, useMemo, useState } from "react";
import { getApiErrorMessage } from "../utils/api";
import { formatAmount, formatDate, formatDisgType } from "../utils/formatters";

function AddMro() {
  const [records, setRecords] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [mroNo, setMroNo] = useState("");
  const [mroDate, setMroDate] = useState("");
  const [mroAmount, setMroAmount] = useState(0);
  const [category, setCategory] = useState("all");
  const [errors, setErrors] = useState({});
  const [statusMessage, setStatusMessage] = useState("");
  const [statusType, setStatusType] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const showStatus = (type, message) => {
    setStatusType(type);
    setStatusMessage(message);
  };

  const loadRecords = useCallback(async () => {
    setIsLoading(true);
    setSelectedIds([]);

    try {
      const response = await fetch(`http://localhost:8080/api/encashment/pending-mro?category=${category}`);
      if (!response.ok) {
        throw new Error("Unable to load pending MRO records");
      }

      const data = await response.json();
      setRecords(data);
    } catch (error) {
      setRecords([]);
      showStatus("error", error.message || "Unable to load pending MRO records");
    } finally {
      setIsLoading(false);
    }
  }, [category]);

  useEffect(() => {
    loadRecords();
  }, [loadRecords]);

  useEffect(() => {
    const selectedRecords = records.filter((record) => selectedIds.includes(record.id));
    const total = selectedRecords.reduce((sum, record) => sum + (record.sumDvAmount || 0), 0);
    setMroAmount(Number(total.toFixed(2)));
  }, [selectedIds, records]);

  const validateForm = () => {
    const newErrors = {};
    const trimmedMroNo = mroNo.trim();
    const selectedRecords = records.filter((record) => selectedIds.includes(record.id));

    if (selectedIds.length === 0) {
      newErrors.selectedIds = "Select at least one record";
    }

    if (!trimmedMroNo) {
      newErrors.mroNo = "Enter MRO number";
    } else if (!/^[A-Za-z0-9/-]+$/.test(trimmedMroNo)) {
      newErrors.mroNo = "MRO number can contain only letters, numbers, / and -";
    }

    if (!mroDate) {
      newErrors.mroDate = "Select MRO date";
    } else {
      if (selectedRecords.some((record) => record.dvDate && record.dvDate > mroDate)) {
        newErrors.mroDate = "MRO date cannot be before the selected DV date";
      }
    }

    if (selectedRecords.some((record) => !record.sumDvAmount || record.sumDvAmount <= 0)) {
      newErrors.mroAmount = "Selected records must have a valid Sum DV Amount";
    }

    if (mroAmount <= 0) {
      newErrors.mroAmount = "MRO amount must be greater than 0";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const toggleRow = (id) => {
    if (selectedIds.includes(id)) {
      setSelectedIds(selectedIds.filter((value) => value !== id));
    } else {
      setSelectedIds([...selectedIds, id]);
    }
    setErrors((prev) => ({ ...prev, selectedIds: "" }));
  };

  const toggleSelectAll = () => {
    if (selectedIds.length === records.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(records.map((record) => record.id));
    }
    setErrors((prev) => ({ ...prev, selectedIds: "" }));
  };

  const submitMro = async () => {
    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);
    showStatus("", "");

    try {
      const response = await fetch("http://localhost:8080/api/encashment/add-mro", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          ids: selectedIds,
          mroNo: mroNo.trim(),
          mroDate,
          mroAmount
        })
      });

      if (!response.ok) {
        const message = await getApiErrorMessage(response, "Unable to save MRO details");
        throw new Error(message || "Unable to save MRO details");
      }

      showStatus("success", "MRO added successfully");
      setSelectedIds([]);
      setMroNo("");
      setMroDate("");
      setErrors({});
      await loadRecords();
    } catch (error) {
      showStatus("error", error.message || "Unable to save MRO details");
    } finally {
      setIsSubmitting(false);
    }
  };

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
            <h2 className="section-title">MRO Details</h2>
            <p className="section-subtitle">Enter one MRO reference for the selected DV records.</p>
          </div>
        </div>

        <div className="form-grid">
          <div>
            <label>MRO No</label>
            <input
              type="text"
              value={mroNo}
              onChange={(e) => {
                setMroNo(e.target.value);
                setErrors((prev) => ({ ...prev, mroNo: "" }));
              }}
            />
            {errors.mroNo && <p className="error-text">{errors.mroNo}</p>}
          </div>

          <div>
            <label>MRO Date</label>
            <input
              type="date"
              lang="en-GB"
              value={mroDate}
              onChange={(e) => {
                setMroDate(e.target.value);
                setErrors((prev) => ({ ...prev, mroDate: "" }));
              }}
            />
            {errors.mroDate && <p className="error-text">{errors.mroDate}</p>}
          </div>

          <div>
            <label>MRO Amount</label>
            <input type="number" value={mroAmount} readOnly />
            {errors.mroAmount && <p className="error-text">{errors.mroAmount}</p>}
          </div>

          <div style={{ alignSelf: "end" }}>
            <button onClick={submitMro} disabled={isSubmitting || isLoading}>
              {isSubmitting ? "Saving..." : "Submit"}
            </button>
          </div>
        </div>
      </div>

      <div className="page-card">
        <div className="toolbar-row">
          <div>
            <h3 className="section-title">Records Pending MRO</h3>
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
            <button onClick={toggleSelectAll} disabled={!records.length}>
              {selectedIds.length === records.length && records.length > 0 ? "Unselect All" : "Select All"}
            </button>
          </div>
        </div>
        {errors.selectedIds && <p className="error-text">{errors.selectedIds}</p>}

        <div className="table-wrap" style={{ maxHeight: "460px" }}>
          <table>
            <thead>
              <tr>
                <th>Select</th>
                <th>Emp Code</th>
                <th>Name</th>
                <th>Category</th>
                <th>Purpose</th>
                <th>DV No</th>
                <th>DV Date</th>
                <th>DV Amount</th>
                <th>DV No Diff</th>
                <th>DV Date Diff</th>
                <th>DV Amount Diff</th>
                <th>DV No Diff Pay</th>
                <th>DV Date Diff Pay</th>
                <th>DV Amount Diff Pay</th>
                <th>Sum DV Amount</th>
              </tr>
            </thead>

            <tbody>
              {records.map((record) => (
                <tr key={record.id}>
                  <td>
                    <input
                      type="checkbox"
                      checked={selectedIds.includes(record.id)}
                      onChange={() => toggleRow(record.id)}
                    />
                  </td>
                  <td>{record.personnel?.empCode || "-"}</td>
                  <td>{record.personnel?.name || "-"}</td>
                  <td>{formatDisgType(record.personnel?.disgType)}</td>
                  <td>{record.purpose}</td>
                  <td>{record.dvNo}</td>
                  <td>{formatDate(record.dvDate)}</td>
                  <td>{formatAmount(record.dvAmount)}</td>
                  <td>{record.dvNoDiff || "-"}</td>
                  <td>{formatDate(record.dvDateDiff)}</td>
                  <td>{formatAmount(record.dvAmountDiff)}</td>
                  <td>{record.dvNoDiffPay || "-"}</td>
                  <td>{formatDate(record.dvDateDiffPay)}</td>
                  <td>{formatAmount(record.dvAmountDiffPay)}</td>
                  <td>{formatAmount(record.sumDvAmount)}</td>
                </tr>
              ))}
              {!isLoading && records.length === 0 && (
                <tr>
                  <td colSpan="15" style={{ textAlign: "center" }}>
                    No pending MRO records found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {isLoading && <p className="helper-text">Loading pending MRO records...</p>}
      </div>
    </div>
  );
}

export default AddMro;
