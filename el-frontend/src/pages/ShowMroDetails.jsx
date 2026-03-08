import { useEffect, useState } from "react";

function ShowMroDetails(){

  const [records,setRecords] = useState([]);

  const fetchRecords = async () => {
    const res = await fetch("http://localhost:8080/api/encashment/mro-details");
    const data = await res.json();
    setRecords(data);
  };

  useEffect(()=>{
    fetchRecords();
  },[]);

  return(

  <div style={{background:"#f5f7fb",minHeight:"100vh"}}>

    <div style={{padding:"30px"}}>

      <h2>Show MRO Details</h2>

      <div style={{
        background:"white",
        padding:"20px",
        borderRadius:"6px",
        boxShadow:"0 2px 6px rgba(0,0,0,0.08)"
      }}>

        <table border="1" cellPadding="8" style={{marginTop:"15px",width:"100%"}}>

          <thead>
            <tr>

              <th>Employee</th>
              <th>Purpose</th>

              <th>Bill No</th>

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

            {records.map(r => (

              <tr key={r.id}>

                <td>{r.empId}</td>
                <td>{r.purpose}</td>

                <td>{r.billNo}</td>

                <td>{r.dvNo}</td>
                <td>{r.dvDate}</td>
                <td>{r.dvAmount}</td>

                <td>{r.sumDvAmount}</td>

                <td>{r.mroNo}</td>
                <td>{r.mroDate}</td>
                <td>{r.mroAmount}</td>

              </tr>

            ))}

          </tbody>

        </table>

      </div>

    </div>

  </div>

  );
}

export default ShowMroDetails;