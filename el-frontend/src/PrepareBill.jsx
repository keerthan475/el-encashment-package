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
  };

  const handlePurposeChange = async (value) => {
    setPurpose(value);
    setShowBlockPeriod(false);
    setRetirementDate("");
    setSpecialDate("");

    if (value === "Retirement" && selectedEmployee) {
      const response = await fetch(
        `http://localhost:8080/api/personnel/${selectedEmployee.id}/retirement-date`
      );
      const data = await response.json();
      setRetirementDate(data);
    }
  };

  return (
    <div>
      <h2>Prepare Bill</h2>

      {/* Search Section */}
      <div style={{ marginBottom: "20px" }}>
        <input
          type="text"
          placeholder="Search employee..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          onFocus={searchEmployees}
          style={{ padding: "8px", width: "250px" }}
        />
        <button onClick={searchEmployees} style={{ marginLeft: "10px" }}>
          Search
        </button>

        {employees.length > 0 && (
          <div
            style={{
              border: "1px solid #ccc",
              width: "250px",
              background: "white",
              position: "absolute"
            }}
          >
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

      {selectedEmployee && (
        <div
          style={{
            display: "flex",
            gap: "50px",
            border: "1px solid #ddd",
            padding: "20px"
          }}
        >
          {/* Left Section */}
          <div style={{ flex: 1 }}>
            <h4>Employee Details</h4>
            <p>Name: {selectedEmployee.name}</p>
            <p>Division: {selectedEmployee.division}</p>
            <p>DOB: {selectedEmployee.dob}</p>
            <p>Basic Pay: {selectedEmployee.financeData?.basicPay}</p>
          </div>

          {/* Right Section */}
          <div style={{ flex: 1 }}>
            <h4>Purpose</h4>

            <select
              onChange={(e) => handlePurposeChange(e.target.value)}
              style={{ padding: "8px", width: "200px" }}
            >
              <option value="">Select</option>
              <option value="Home_Town">Home Town</option>
              <option value="All_India">All India</option>
              <option value="Retirement">Retirement</option>
              <option value="Expired">Expired</option>
              <option value="VRS">VRS</option>
              <option value="Resigned">Resigned</option>
            </select>

            {/* Home Town / All India */}
            {(purpose === "Home_Town" || purpose === "All_India") && (
              <div style={{ marginTop: "15px" }}>
                <input
                  type="date"
                  value={ltcStartDate}
                  onChange={(e) => setLtcStartDate(e.target.value)}
                />
                <button
                  style={{ marginLeft: "10px" }}
                  onClick={() => setShowBlockPeriod(true)}
                >
                  Go
                </button>

                {showBlockPeriod && (
                  <div style={{ marginTop: "10px" }}>
                    <select>
                      <option>2022-2025</option>
                      <option>2026-2029</option>
                    </select>
                  </div>
                )}
              </div>
            )}

            {/* Retirement */}
            {purpose === "Retirement" && retirementDate && (
              <p style={{ marginTop: "10px" }}>
                Retirement Date: {retirementDate}
              </p>
            )}

            {/* Other Cases */}
            {(purpose === "Expired" ||
              purpose === "VRS" ||
              purpose === "Resigned") && (
              <div style={{ marginTop: "10px" }}>
                <input
                  type="date"
                  value={specialDate}
                  onChange={(e) => setSpecialDate(e.target.value)}
                />
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default PrepareBill;
