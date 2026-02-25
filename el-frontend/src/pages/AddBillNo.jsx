import { useEffect, useState } from "react";

function AddBillNo() {

  const [records, setRecords] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [billNo, setBillNo] = useState("");
  const [billDate, setBillDate] = useState("");

  // load pending records
  useEffect(() => {
    fetch("http://localhost:8080/api/encashment/pending-bills")
      .then(res => res.json())
      .then(data => setRecords(data));
  }, []);

  // toggle checkbox
  const toggleSelect = (id) => {
    setSelectedIds(prev =>
      prev.includes(id)
        ? prev.filter(x => x !== id)
        : [...prev, id]
    );
  };

  // select all rows
  const selectAll = () => {
    setSelectedIds(records.map(r => r.id));
  };

  // submit update
  const handleSubmit = async () => {

    if (!billNo || !billDate) {
      alert("Enter Bill No and Date");
      return;
    }

    if (selectedIds.length === 0) {
      alert("Select at least one record");
      return;
    }

    await fetch("http://localhost:8080/api/encashment/add-bill-no", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        ids: selectedIds,
        billNo,
        billDate
      })
    });

    alert("Bill number added successfully");

    // reload records
    window.location.reload();
  };

  return (
    <div style={{ padding: "30px" }}>
      <h2>Add Bill Number</h2>

      <div style={{ marginBottom: "15px" }}>
        <input
          placeholder="Bill Number"
          value={billNo}
          onChange={(e)=>setBillNo(e.target.value)}
        />

        <input
          type="date"
          value={billDate}
          onChange={(e)=>setBillDate(e.target.value)}
          style={{ marginLeft: "10px" }}
        />

        <button onClick={selectAll} style={{ marginLeft: "10px" }}>
          Select All
        </button>
      </div>

      <table border="1" cellPadding="8">
        <thead>
          <tr>
            <th>Select</th>
            <th>Employee ID</th>
            <th>Purpose</th>
            <th>Total Amount</th>
            <th>Date</th>
          </tr>
        </thead>

        <tbody>
          {records.map(r => (
            <tr key={r.id}>
              <td>
                <input
                  type="checkbox"
                  checked={selectedIds.includes(r.id)}
                  onChange={()=>toggleSelect(r.id)}
                />
              </td>
              <td>{r.empId}</td>
              <td>{r.purpose}</td>
              <td>{r.totalAmount}</td>
              <td>{r.createdDate}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <button
        style={{
          marginTop: "20px",
          padding: "10px 20px",
          background: "#1976d2",
          color: "white",
          border: "none"
        }}
        onClick={handleSubmit}
      >
        Save Bill Number
      </button>

    </div>
  );
}

export default AddBillNo;