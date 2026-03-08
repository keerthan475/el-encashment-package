import { useEffect, useState } from "react";

function AddDvNo() {

  const [records, setRecords] = useState([]);
  const [selected, setSelected] = useState([]);

  const [dvNo, setDvNo] = useState("");
  const [dvDate, setDvDate] = useState("");
  const [dvAmount, setDvAmount] = useState("");
  const [dvBalance, setDvBalance] = useState("");
  const [recoveryCda, setRecoveryCda] = useState("");
  const [cdaRemarks, setCdaRemarks] = useState("");
  const [recoveryCdaTax, setRecoveryCdaTax] = useState("");
  const [cdaTaxRemarks, setCdaTaxRemarks] = useState("");

  useEffect(() => {
    fetch("http://localhost:8080/api/encashment/pending-dv")
      .then(res => res.json())
      .then(data => setRecords(data));
  }, []);

  const toggleRow = (id) => {
    setSelected(prev =>
      prev.includes(id)
        ? prev.filter(x => x !== id)
        : [...prev, id]
    );
  };

  const selectAll = () => {
    setSelected(records.map(r => r.id));
  };

  const submitDv = async () => {

    if (!dvNo || !dvDate) {
      alert("Enter DV No and Date");
      return;
    }

    if (selected.length === 0) {
      alert("Select at least one record");
      return;
    }

    await fetch("http://localhost:8080/api/encashment/add-dv-no", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        ids: selected,
        dvNo,
        dvDate,
        dvAmount,
        dvBalance,
        recoveryCda,
        cdaRemarks,
        recoveryCdaTax,
        cdaTaxRemarks
      })
    });

    alert("DV details saved");

    setSelected([]);
    window.location.reload();
  };

  return (
    <div style={{padding:"30px"}}>
      <h2>Add DV No</h2>

      <div style={{
        background:"white",
        padding:"20px",
        borderRadius:"6px",
        boxShadow:"0 2px 6px rgba(0,0,0,0.08)",
        marginBottom:"20px",
        display:"grid",
        gridTemplateColumns:"repeat(3,1fr)",
        gap:"20px"
      }}>

        {/* TOP FORM */}
        <div>
          <label>DV No </label>
          <input placeholder="DV No"
            value={dvNo}
            onChange={e=>setDvNo(e.target.value)}
          />
        </div>

        <div>
          <label>DV date </label>
          <input type="date"
            value={dvDate}
            onChange={e=>setDvDate(e.target.value)}
          />
        </div>

        <div>
          <label>DV Amount </label>
          <input placeholder="DV Amount"
            value={dvAmount}
            onChange={e=>setDvAmount(e.target.value)}
          />
        </div>

        <div>
          <label>DV Balance </label>
          <input placeholder="DV Balance"
            value={dvBalance}
            onChange={e=>setDvBalance(e.target.value)}
          />
        </div>

        <div>
          <label>Recovery CDA </label>
          <input placeholder="Recovery CDA"
            value={recoveryCda}
            onChange={e=>setRecoveryCda(e.target.value)}
          />
        </div>

        <div>
          <label>CDA Remarks </label>
          <input placeholder="CDA Remarks"
            value={cdaRemarks}
            onChange={e=>setCdaRemarks(e.target.value)}
          />
        </div>

        <div>
          <label>Recovery CDA Tax </label>
          <input placeholder="Recovery CDA Tax"
            value={recoveryCdaTax}
            onChange={e=>setRecoveryCdaTax(e.target.value)}
          />
        </div>

        <div>
          <label>Recovery CDA Tax Remarks </label>
          <input placeholder="CDA Tax Remarks"
            value={cdaTaxRemarks}
            onChange={e=>setCdaTaxRemarks(e.target.value)}
          />
        </div>

        <div>

          <button onClick={submitDv}>Submit</button>
        </div>

      
      </div>

      {/* TABLE */}
      <button onClick={selectAll}>Select All</button>

      <table border="1" cellPadding="5" style={{marginTop:"10px"}}>
        <thead>
          <tr>
            <th>Select</th>
            <th>ID</th>
            <th>Employee</th>
            <th>Total Amount</th>
            <th>Bill No</th>
          </tr>
        </thead>

        <tbody>
          {records.map(r => (
            <tr key={r.id}>
              <td>
                <input type="checkbox"
                  checked={selected.includes(r.id)}
                  onChange={()=>toggleRow(r.id)}
                />
              </td>
              <td>{r.id}</td>
              <td>{r.empId}</td>
              <td>{r.totalAmount}</td>
              <td>{r.billNo}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default AddDvNo;