import { useEffect, useState } from "react";


function AddMro() {

  const [records,setRecords] = useState([]);
  const [selectedIds,setSelectedIds] = useState([]);

  const [mroNo,setMroNo] = useState("");
  const [mroDate,setMroDate] = useState("");

  const [mroAmount,setMroAmount] = useState(0);
  const [category,setCategory] = useState("all");

  // fetch pending MRO records
  const fetchRecords = async () => {
    const res = await fetch(`http://localhost:8080/api/encashment/pending-mro?category=${category}`);
    const data = await res.json();
    setRecords(data);
  };

  useEffect(()=>{
    fetchRecords();
  },[category]);

  // select row
  const toggleRow = (id) => {

    if(selectedIds.includes(id)){
      setSelectedIds(selectedIds.filter(x=>x!==id));
    }else{
      setSelectedIds([...selectedIds,id]);
    }

  };

  // select all / unselect all
  const toggleSelectAll = () => {

    if(selectedIds.length === records.length){
      setSelectedIds([]);
    }else{
      setSelectedIds(records.map(r=>r.id));
    }

  };

  // calculate MRO amount automatically
  useEffect(()=>{

    const selectedRecords = records.filter(r =>
      selectedIds.includes(r.id)
    );

    const total = selectedRecords.reduce(
      (sum,r)=> sum + (r.sumDvAmount || 0),
      0
    );

    setMroAmount(total);

  },[selectedIds,records]);

  // submit MRO
  const submitMro = async () => {

    if(selectedIds.length === 0){
      alert("Select at least one record");
      return;
    }

    if(!mroNo || !mroDate){
      alert("Enter MRO No and Date");
      return;
    }

    await fetch("http://localhost:8080/api/encashment/add-mro",{
      method:"POST",
      headers:{
        "Content-Type":"application/json"
      },
      body:JSON.stringify({
        ids:selectedIds,
        mroNo,
        mroDate,
        mroAmount
      })
    });

    alert("MRO added successfully");

    setSelectedIds([]);
    setMroNo("");
    setMroDate("");

    fetchRecords();

  };

  return(

  <div style={{background:"#f5f7fb",minHeight:"100vh"}}>

    <div style={{padding:"30px"}}>

      <h2>Add MRO</h2>

      {/* MRO FORM */}

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

        <div>
          <label>MRO No</label>
          <input
            type="text"
            value={mroNo}
            onChange={(e)=>setMroNo(e.target.value)}
          />
        </div>

        <div>
          <label>MRO Date</label>
          <input
            type="date"
            value={mroDate}
            onChange={(e)=>setMroDate(e.target.value)}
          />
        </div>

        <div>
          <label>MRO Amount</label>
          <input
            type="number"
            value={mroAmount}
            readOnly
          />
        </div>

        <div>
          <label>
          <input
          type="radio"
          value="all"
          checked={category==="all"}
          onChange={(e)=>setCategory(e.target.value)}
          />
          All
          </label>

          <label>
          <input
          type="radio"
          value="officer"
          checked={category==="officer"}
          onChange={(e)=>setCategory(e.target.value)}
          />
          Officer
          </label>

          <label>
          <input
          type="radio"
          value="staff"
          checked={category==="staff"}
          onChange={(e)=>setCategory(e.target.value)}
          />
          Staff
          </label>
        </div>

        <div style={{alignSelf:"end"}}>
          <button onClick={submitMro}>
            Submit
          </button>
        </div>

      </div>

      {/* TABLE */}

      <div style={{
        background:"white",
        padding:"20px",
        borderRadius:"6px",
        boxShadow:"0 2px 6px rgba(0,0,0,0.08)"
      }}>

        <button onClick={toggleSelectAll}>
          {selectedIds.length === records.length ? "Unselect All" : "Select All"}
        </button>
        
        <table border="1" cellPadding="8" style={{marginTop:"15px",width:"100%"}}>

          <thead>
            <tr>

              <th>Select</th>

              <th>Employee</th>
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

            {records.map(r => (

              <tr key={r.id}>

                <td>
                  <input
                    type="checkbox"
                    checked={selectedIds.includes(r.id)}
                    onChange={()=>toggleRow(r.id)}
                  />
                </td>

                <td>{r.empId}</td>
                <td>{r.purpose}</td>

                <td>{r.dvNo}</td>
                <td>{r.dvDate}</td>
                <td>{r.dvAmount}</td>

                <td>{r.dvNoDiff}</td>
                <td>{r.dvDateDiff}</td>
                <td>{r.dvAmountDiff}</td>

                <td>{r.dvNoDiffPay}</td>
                <td>{r.dvDateDiffPay}</td>
                <td>{r.dvAmountDiffPay}</td>

                <td>{r.sumDvAmount}</td>

              </tr>

            ))}

          </tbody>

        </table>

      </div>

    </div>

  </div>

  );

}

export default AddMro;