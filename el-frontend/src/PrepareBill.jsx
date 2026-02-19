import { useState } from "react";

function PrepareBill() {
  const [search, setSearch] = useState("");
  const [employees, setEmployees] = useState([]);
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [purpose, setPurpose] = useState("");
  const [ltcStartDate, setLtcStartDate] = useState("");
  const [retirementDate, setRetirementDate] = useState("");
  const [specialDate, setSpecialDate] = useState("");
  const [showBlockPeriod, setShowBlockPeriod] = useState(false);
  const [elDays, setElDays] = useState(0);
  const [hplDays, setHplDays] = useState(0);
  const [availableBlocks, setAvailableBlocks] = useState([]);
  const [elAmount, setElAmount] = useState(0);
  const [hplAmount, setHplAmount] = useState(0);
  const [totalAmount, setTotalAmount] = useState(0);
  const [showCalculation, setShowCalculation] = useState(false);
  const [doPartNumber, setDoPartNumber] = useState("");
  const [doPartDate, setDoPartDate] = useState("");
  const [blockPeriod, setBlockPeriod] = useState("");


  const [itApplicable, setItApplicable] = useState("");
  const [itPercent, setItPercent] = useState(0);
  const [itAmount, setItAmount] = useState(0);
  const [eduCess, setEduCess] = useState(0);
  const [itRecovery, setItRecovery] = useState(0);

  const [otherRecovery, setOtherRecovery] = useState(0);
  const [otherRemark, setOtherRemark] = useState("");
  const [otherTaxable, setOtherTaxable] = useState(0);
  const [otherTaxableRemark, setOtherTaxableRemark] = useState("");

  const [grandTotal, setGrandTotal] = useState(0);




  const generateBlocks = () => {
    if (!ltcStartDate) return;

    const year = new Date(ltcStartDate).getFullYear();

    let baseYear;
    let size;

    if (purpose === "Home_Town") {
      baseYear = 2016;
      size = 2;
    } else {
      baseYear = 2018;
      size = 4;
    }

    // find current block start
    const diff = year - baseYear;
    const blockIndex = Math.floor(diff / size);
    const currentStart = baseYear + blockIndex * size;
    const currentEnd = currentStart + size - 1;

    let blocks = [];

    // include previous block iff year == currentblock start
    if (year === currentStart && blockIndex > 0) {
      const prevStart = currentStart - size;
      const prevEnd = currentStart - 1;
      blocks.push(`${prevStart}-${prevEnd}`);
    }

    // include current block
    blocks.push(`${currentStart}-${currentEnd}`);

    setAvailableBlocks(blocks);
  };


  const searchEmployees = async () => {
    const response = await fetch(
      `http://localhost:8080/api/personnel/search?name=${search}`
    );
    const data = await response.json();
    setEmployees(data);
  };

  const handleSelectEmployee = (emp) => {
    setSelectedEmployee(emp);
    setEmployees([]);
    setPurpose("");
    setLtcStartDate("");
    setAvailableBlocks([]);
    setElDays(0);
    setHplDays(0);
    setElAmount(0);
    setHplAmount(0);
    setTotalAmount(0);
    setShowCalculation(false);
  };

  const handlePurposeChange = async (value) => {
    setPurpose(value);
    setShowBlockPeriod(false);
    setRetirementDate("");
    setSpecialDate("");
    setLtcStartDate("");
    setAvailableBlocks([]);
    setElDays(0);
    setHplDays(0);
    setElAmount(0);
    setHplAmount(0);
    setTotalAmount(0);
    setShowCalculation(false);

    if (value === "Retirement" && selectedEmployee) {
      const response = await fetch(
        `http://localhost:8080/api/personnel/${selectedEmployee.id}/retirement-date`
      );
      const data = await response.json();
      setRetirementDate(data);
    }
  };

  const calculateAmounts = () => {
    const basic = selectedEmployee.financeData?.basicPay || 0;
    const daPercent = 50; // temporary, later from backend
    const da = (basic * daPercent) / 100;
    const payPlusDa = basic + da;

    const elAmt = (payPlusDa * elDays) / 30;
    const hplAmt = ((payPlusDa * hplDays) / 30)/2;

    setElAmount(elAmt);
    setHplAmount(hplAmt);
    setTotalAmount(elAmt + hplAmt);
    setShowCalculation(true);
  };

  const computeIT = () => {
    if (itApplicable === "No") {
      setItAmount(0);
      setEduCess(0);
      setItRecovery(0);
      return;
    }

    const it = (totalAmount * itPercent) / 100;
    const cess = it * 0.04;

    setItAmount(it);
    setEduCess(cess);
    setItRecovery(it + cess);
  };

  const handleITChange = (value) => {
    const it = Number(value);
    const cess = it * 0.04;

    setItAmount(it);
    setEduCess(cess);
    setItRecovery(it + cess);
  };

  const computeGrandTotal = () => {
    const grand =
      totalAmount -
      (itRecovery + Number(otherRecovery) + Number(otherTaxable));

    setGrandTotal(grand);
    saveBill();
  };

  const saveBill = async () => {
    await fetch("http://localhost:8080/api/encashment/save", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        empId: selectedEmployee.id,
        purpose,
        elDays,
        hplDays,
        elAmount,
        hplAmount,
        totalAmount,
        itAmount,
        eduCess,
        itRecovery,
        otherRecovery,
        otherTaxable,
        grandTotal,
        doPartNumber,
        doPartDate,
        eventDate: ltcStartDate,
        blockPeriod
      })
    });

    alert("Bill saved successfully!");
  };



  return (
  <div style={{ background: "#f5f7fb", minHeight: "100vh" }}>

    {/* MAIN CONTENT */}
    <div style={{ flex: 1, padding: "30px" }}>
      
      <h2>Prepare Bill</h2>

      {/* SEARCH */}
      <div style={{ marginBottom: "20px" }}>
        <input
          type="text"
          placeholder="Search employee..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          onFocus={searchEmployees}
          style={{ padding: "8px", width: "300px" }}
        />
        <button onClick={searchEmployees} style={{ marginLeft: "10px" }}>
          Search
        </button>

        {employees.length > 0 && (
          <div style={{
            border: "1px solid #ccc",
            width: "300px",
            background: "white",
            position: "absolute"
          }}>
            {employees.map((emp) => (
              <div
                key={emp.id}
                style={{ padding: "8px", cursor: "pointer" }}
                onClick={() => handleSelectEmployee(emp)}
              >
                {emp.name} | {emp.empCode}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* EMPLOYEE DETAILS */}
      {selectedEmployee && (
        <div style={{
          background: "white",
          padding: "20px",
          borderRadius: "6px",
          boxShadow: "0 2px 6px rgba(0,0,0,0.08)",
          marginBottom: "20px",
          display: "grid",
          gridTemplateColumns: "1fr 1fr",
          gap: "20px"
        }}>
          <div>
            <h4>Employee Details</h4>
            <p>Name: {selectedEmployee.name}</p>
            <p>Division: {selectedEmployee.division}</p>
            <p>DOB: {selectedEmployee.dob}</p>
            <p>GPF: {selectedEmployee.financeData?.gpfAccountNo}</p>
          </div>

          <div>
            <h4>Financial Details</h4>
            <p>Basic Pay: {selectedEmployee.financeData?.basicPay}</p>
            <p>DA %: 50</p>
            <p>DA Amount: {selectedEmployee.financeData?.basicPay * 0.5}</p>
            <p><b>Total Pay: {selectedEmployee.financeData?.basicPay * 1.5}</b></p>
          </div>
        </div>
      )}

      {/* SECTION 1 */}
      {selectedEmployee && (
        <div style={{
          background: "white",
          padding: "20px",
          borderRadius: "6px",
          boxShadow: "0 2px 6px rgba(0,0,0,0.08)",
          marginBottom: "20px"
        }}>

          <h3>Section 1 — Purpose & Leave Details</h3>

          <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "20px", marginTop: "15px" }}>

            {/* PURPOSE */}
            <div>
              <label>Purpose</label>
              <select value={purpose} onChange={(e)=>handlePurposeChange(e.target.value)}>
                <option value="">Select</option>
                <option value="Home_Town">Home Town</option>
                <option value="All_India">All India</option>
                <option value="Retirement">Retirement</option>
                <option value="Expired">Expired</option>
                <option value="VRS">VRS</option>
                <option value="Resigned">Resigned</option>
              </select>
            </div>

            {/* Dynamic Date Field */}
            {purpose && (
              <div>
                <label>
                  {purpose === "Home_Town" || purpose === "All_India"
                    ? "LTC Date"
                    : purpose === "Retirement"
                    ? "Retirement Date"
                    : "Event Date"}
                </label>

                <input
                  type="date"
                  value={ltcStartDate}
                  onChange={(e) => {
                    setLtcStartDate(e.target.value);
                    generateBlocks();
                  }}
                />
              </div>
            )}


            {/* BLOCK PERIOD */}
            {(purpose === "Home_Town" || purpose === "All_India") && availableBlocks.length>0 && (
              <div>
                <label>Block Period</label>
                <select onChange={(e)=>setBlockPeriod(e.target.value)}>
                  {availableBlocks.map(b => <option key={b}>{b}</option>)}
                </select>
              </div>
            )}

            {/* EL */}
            {purpose && (
              <div>
                <label>EL Days</label>
                <input
                  type="number"
                  value={elDays}
                  min="0"
                  max={(purpose==="Home_Town"||purpose==="All_India")?10:300}
                  onChange={(e)=>setElDays(Number(e.target.value))}
                />
              </div>
            )}

            {/* HPL */}
            {purpose && purpose!=="Home_Town" && purpose!=="All_India" && (
              <div>
                <label>HPL Days</label>
                <input
                  type="number"
                  value={hplDays}
                  min="0"
                  max={300-elDays}
                  onChange={(e)=>setHplDays(Number(e.target.value))}
                />
              </div>
            )}

            {/* DOPART NUMBER */}
            <div>
              <label>DoPart Number</label>
              <input
                type="text"
                value={doPartNumber}
                onChange={(e)=>setDoPartNumber(e.target.value)}
              />
            </div>

            {/* DOPART DATE */}
            <div>
              <label>DoPart Date</label>
              <input
                type="date"
                value={doPartDate}
                onChange={(e)=>setDoPartDate(e.target.value)}
              />
            </div>

            {/* GO BUTTON */}
            <div style={{ alignSelf:"end" }}>
              <button onClick={calculateAmounts}>GO</button>
            </div>

          </div>

        </div>
      )}

      {/* Section 2 — Calculation */}
      {totalAmount > 0 && (
        <div style={{
          marginTop: "25px",
          padding: "20px",
          border: "1px solid #ddd",
          borderRadius: "8px",
          background: "#fff"
        }}>
          <h3>Section 2 — Calculation</h3>

          <p>
            EL Amount = (Basic + DA) × EL Days / 30  
            <br />
            = ₹{elAmount.toFixed(2)}
          </p>

          <p>
            HPL Amount = ((Basic + DA) × HPL Days / 30) / 2  
            <br />
            = ₹{hplAmount.toFixed(2)}
          </p>

          <h4>Total = ₹{totalAmount.toFixed(2)}</h4>
        </div>
      )}

      {/* SECTION 3 — TAX & RECOVERY */}
      {totalAmount > 0 && (
        <div style={{
          marginTop: "25px",
          padding: "20px",
          border: "1px solid #ddd",
          borderRadius: "8px",
          background: "#fff"
        }}>

          <h3>Section 3 — Tax & Recovery</h3>

          {/* IT Applicable */}
          <div style={{marginTop:"10px"}}>
            <label>Is IT Recovery Applicable?</label>
            <select
              value={itApplicable}
              onChange={(e)=>setItApplicable(e.target.value)}
            >
              <option value="">Select</option>
              <option value="Yes">Yes</option>
              <option value="No">No</option>
            </select>
          </div>

          {/* IT % */}
          {itApplicable==="Yes" && (
            <div style={{marginTop:"10px"}}>
              <label>IT %</label>
              <select onChange={(e)=>setItPercent(Number(e.target.value))}>
                {[0,5,10,15,20,25,30,35,40,45,50].map(v=>(
                  <option key={v} value={v}>{v}%</option>
                ))}
              </select>
            </div>
          )}

          {/* GO FOR IT */}
          {itApplicable && (
            <button
              style={{marginTop:"10px"}}
              onClick={computeIT}
            >
              Calculate IT
            </button>
          )}

          {/* IT RESULT */}
          {itApplicable && (
            <div style={{marginTop:"15px"}}>
              <p>IT Amount:
                <input
                  type="number"
                  value={itAmount}
                  onChange={(e)=>handleITChange(e.target.value)}
                />
              </p>
              <p>Edu Cess (4%): ₹{eduCess.toFixed(2)}</p>
              <p><b>IT Recovery: ₹{itRecovery.toFixed(2)}</b></p>
            </div>
          )}

          {/* OTHER RECOVERY */}
          <hr/>

          <div>
            <label>Other Recovery</label>
            <input
              type="number"
              value={otherRecovery}
              onChange={(e)=>setOtherRecovery(e.target.value)}
            />
            <input
              type="text"
              placeholder="Remark"
              value={otherRemark}
              onChange={(e)=>setOtherRemark(e.target.value)}
            />
          </div>

          <div style={{marginTop:"10px"}}>
            <label>Other Recovery Taxable</label>
            <input
              type="number"
              value={otherTaxable}
              onChange={(e)=>setOtherTaxable(e.target.value)}
            />
            <input
              type="text"
              placeholder="Taxable Remark"
              value={otherTaxableRemark}
              onChange={(e)=>setOtherTaxableRemark(e.target.value)}
            />
          </div>

          {/* FINAL BUTTON */}
          <button
            style={{marginTop:"15px"}}
            onClick={computeGrandTotal}
          >
            Final Calculate
          </button>

          {/* GRAND TOTAL */}
          {grandTotal > 0 && (
            <h3 style={{marginTop:"15px"}}>
              Grand Total =
              Total − (IT Recovery + Other Recovery + Other Taxable)
              <br/>
              = ₹{grandTotal.toFixed(2)}
            </h3>
          )}

        </div>
      )}

    </div>
  </div>
);

}

export default PrepareBill;
