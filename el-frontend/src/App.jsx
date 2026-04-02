import { useState } from "react";
import "./App.css";
import PrepareBill from "./PrepareBill";
import AddBillNo from "./pages/AddBillNo";
import AddDvNo from "./pages/AddDvNo";
import AddMro from "./pages/AddMro";
import ShowMroDetails from "./pages/ShowMroDetails";

const navItems = [
  { id: "home", label: "Home", section: "Workspace" },
  { id: "prepare", label: "Prepare Bill", section: "Operations" },
  { id: "addBill", label: "Add Bill No", section: "Operations" },
  { id: "addDv", label: "Add DV No", section: "Operations" },
  { id: "addMro", label: "Add MRO Details", section: "Operations" },
  { id: "showMro", label: "Show MRO Details", section: "Operations" },
  { id: "legacy", label: "Legacy Reports", section: "Reports", disabled: true },
  { id: "budget", label: "Budget Estimate", section: "Reports", disabled: true }
];

const pageMeta = {
  home: {
    title: "EL Encashment Management System",
    subtitle: "Use the left menu to prepare bills, assign Bill No and DV No, add MRO details, and review posted records."
  },
  prepare: {
    title: "Prepare Bill",
    subtitle: "Build and validate encashment proposals with employee context, leave calculations, and deduction review."
  },
  addBill: {
    title: "Add Bill Number",
    subtitle: "Assign bill numbers to prepared cases and monitor processed records in the same workspace."
  },
  addDv: {
    title: "Add DV Number",
    subtitle: "Post DV details in bulk, review bill references, and track completed DV entries."
  },
  addMro: {
    title: "Add MRO Details",
    subtitle: "Recover outstanding DV amounts through a controlled MRO workflow."
  },
  showMro: {
    title: "Show MRO Details",
    subtitle: "Review all posted MRO records with billing and DV context."
  }
};

function HomePage() {
  return (
    <div className="basic-home">
      <div className="basic-home__card">
        <h2>Welcome</h2>
        <p>Select a module from the left menu to continue working.</p>
        <div className="basic-home__grid">
          <div className="basic-home__item">
            <strong>Prepare Bill</strong>
            <span>Create, update, or delete bill records before Bill No is added.</span>
          </div>
          <div className="basic-home__item">
            <strong>Add Bill No</strong>
            <span>Assign Bill No to pending records and review billed records.</span>
          </div>
          <div className="basic-home__item">
            <strong>Add DV No</strong>
            <span>Enter DV details for billed records and review processed entries.</span>
          </div>
          <div className="basic-home__item">
            <strong>Add / Show MRO</strong>
            <span>Post MRO recovery and review completed MRO records.</span>
          </div>
        </div>
      </div>
    </div>
  );
}

function App() {
  const [activePage, setActivePage] = useState("home");
  const activeMeta = pageMeta[activePage] || pageMeta.home;
  const sections = [...new Set(navItems.map((item) => item.section))];

  const renderContent = () => {
    if (activePage === "prepare") return <PrepareBill />;
    if (activePage === "addBill") return <AddBillNo />;
    if (activePage === "addDv") return <AddDvNo />;
    if (activePage === "addMro") return <AddMro />;
    if (activePage === "showMro") return <ShowMroDetails />;
    return <HomePage />;
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand__mark">EL</div>
          <div>
            <div className="brand__title">Encashment Suite</div>
            <div className="brand__subtitle">Finance Admin</div>
          </div>
        </div>

        <nav className="sidebar-nav">
          {sections.map((section) => (
            <div key={section} className="sidebar-section">
              <div className="sidebar-section__title">{section}</div>
              {navItems
                .filter((item) => item.section === section)
                .map((item) => (
                  <button
                    key={item.id}
                    type="button"
                    className={`sidebar-link ${activePage === item.id ? "is-active" : ""}`}
                    onClick={() => !item.disabled && setActivePage(item.id)}
                    disabled={item.disabled}
                  >
                    <span className="sidebar-link__dot" />
                    <span>{item.label}</span>
                  </button>
                ))}
            </div>
          ))}
        </nav>
      </aside>

      <main className="main-shell">
        <header className="topbar">
          <div>
            <div className="topbar__eyebrow">EL Encashment Management System</div>
            <h1>{activeMeta.title}</h1>
            <p>{activeMeta.subtitle}</p>
          </div>
          <div className="topbar__panel">
            <div className="topbar__panel-label">Current Workspace</div>
            <div className="topbar__panel-value">Accounts & Leave Recovery</div>
          </div>
        </header>

        <section className="content-area">{renderContent()}</section>
      </main>
    </div>
  );
}

export default App;
