import { useState } from "react";
import PrepareBill from "./PrepareBill";
import AddBillNo from "./pages/AddBillNo";
import AddDvNo from "./pages/AddDvNo";

function App() {
  const [activePage, setActivePage] = useState("home");

  return (
    <div style={{ display: "flex", height: "100vh", fontFamily: "Arial" }}>
      
      {/* Sidebar */}
      <div
        style={{
          width: "220px",
          background: "#f4f6f9",
          padding: "20px",
          borderRight: "1px solid #ddd"
        }}
      >
        <h3>Menu</h3>

        <div style={{ marginBottom: "10px", cursor: "pointer" }}
             onClick={() => setActivePage("home")}>
          Home
        </div>

        <div style={{ marginBottom: "10px", cursor: "pointer" }}
             onClick={() => setActivePage("prepare")}>
          Prepare Bill
        </div>

        <div style={{ marginBottom: "10px", cursor: "pointer" }}
             onClick={() => setActivePage("addBill")}
        >Add Bill No</div>

        <div style={{ marginBottom: "10px", cursor: "pointer" }}
             onClick={() => setActivePage("addDv")}
        >Add DV No</div>
        <div>Add MRO Details</div>
        <div>Show MRO Details</div>
        <div>View Old Bills</div>
        <div>View Bills Wise Report</div>
        <div>CGEIS Funds</div>
        <div>Budget Estimate</div>
        <div>Exit</div>
      </div>

      {/* Main Content */}
      <div style={{ flex: 1, padding: "10px", background: "#ffffff" }}>
        {activePage === "home" && <h2>Welcome</h2>}
        {activePage === "prepare" && <PrepareBill />}
        {activePage === "addBill" && <AddBillNo />}
        {activePage === "addDv" && <AddDvNo />}

      </div>

    </div>
  );
}

export default App;
