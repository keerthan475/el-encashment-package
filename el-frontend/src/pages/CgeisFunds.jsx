import { useState } from "react";
import CgeisPreparePage from "./CgeisPreparePage";
import CgeisDvDetails from "./CgeisDvDetails";
import CgeisReports from "./CgeisReports";

const tabs = [
  { id: "prepare", label: "Add Funds / Prepare Bill" },
  { id: "dv", label: "Add DV Details" },
  { id: "reports", label: "Reports" }
];

function CgeisFunds({ onExit }) {
  const [activeTab, setActiveTab] = useState("prepare");

  const renderTab = () => {
    if (activeTab === "dv") return <CgeisDvDetails />;
    if (activeTab === "reports") return <CgeisReports />;
    return <CgeisPreparePage />;
  };

  return (
    <div>
      <div className="page-card">
        <div className="toolbar-row">
          <div>
            <h2 className="section-title">CGEIS Funds</h2>
            <p className="section-subtitle">Manage CGEIS salary ranges, prepare bills, add DV details, and open reports.</p>
          </div>
          <div className="toolbar-actions">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                type="button"
                onClick={() => setActiveTab(tab.id)}
                style={activeTab === tab.id ? {} : { background: "#e2e8f0", color: "#18243d", boxShadow: "none" }}
              >
                {tab.label}
              </button>
            ))}
            <button type="button" onClick={onExit} style={{ background: "linear-gradient(135deg, #475569 0%, #334155 100%)" }}>
              Exit
            </button>
          </div>
        </div>
      </div>

      {renderTab()}
    </div>
  );
}

export default CgeisFunds;
