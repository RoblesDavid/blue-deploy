import { useEffect, useState } from "react";
import { sendCheck, getChecks } from "../services/api";
import StatsChart from "../components/StatsChart"

function Dashboard() {
  const [checks, setChecks] = useState([]);
  const [status, setStatus] = useState("Idle");
  const [filter, setFilter] = useState("ALL");

  async function loadChecks() {
    try {
      const data = await getChecks();

      const mapped = data.map((check) => {
        let state = "UNKNOWN";

        if (check.firewallEnabled === true) state = "ON";
        else if (check.firewallEnabled === false) state = "OFF";

        return {
          ...check,
          firewallState: state,
        };
      });

      setChecks(mapped);
    } catch (error) {
      console.error("Error cargando checks:", error);
    }
  }

  useEffect(() => {
    loadChecks();
    const interval = setInterval(loadChecks, 5000);
    return () => clearInterval(interval);
  }, []);

  async function handleSend() {
    setStatus("Enviando...");

    const payload = {
      date: new Date().toISOString(),
      user: "soc-analyst",
      hostname: "frontend-node",
      checks: {
        firewall_enabled: Math.random() > 0.5,
      },
    };

    try {
      const response = await sendCheck(payload);
      setStatus(response);
      loadChecks();
    } catch (error) {
      console.error(error);
      setStatus("Error");
    }
  }

  // 🔎 FILTRO
  const filteredChecks =
    filter === "ALL"
      ? checks
      : checks.filter((c) => c.firewallState === filter);

  // 📊 MÉTRICAS
  const total = checks.length;
  const alerts = checks.filter((c) => c.firewallState === "OFF").length;
  const unknown = checks.filter((c) => c.firewallState === "UNKNOWN").length;

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>🛡️ BLUE DEPLOY SOC</h1>

      <button style={styles.button} onClick={handleSend}>
        Enviar Check
      </button>

      <p>{status}</p>

      {/* 📊 PANEL MÉTRICAS */}
      <div style={styles.metrics}>
        <div>Total: {total}</div>
        <div style={{ color: "#da3633" }}>Alertas: {alerts}</div>
        <div style={{ color: "#d29922" }}>Unknown: {unknown}</div>
      </div>

      {/* 🔎 FILTROS */}
      <div style={styles.filters}>
        <button onClick={() => setFilter("ALL")}>ALL</button>
        <button onClick={() => setFilter("ON")}>ON</button>
        <button onClick={() => setFilter("OFF")}>OFF</button>
        <button onClick={() => setFilter("UNKNOWN")}>UNKNOWN</button>
      </div>


      {/* 📊 GRÁFICA */}
      <StatsChart checks={checks} />

      {/* 📋 TABLA */}
      <table style={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Hostname</th>
            <th>User</th>
            <th>Date</th>
            <th>Firewall</th>
          </tr>
        </thead>
        <tbody>
          {filteredChecks.map((check) => (
            <tr
              key={check.id}
              style={
                check.firewallState === "OFF"
                  ? styles.rowAlert
                  : undefined
              }
            >
              <td>{check.id}</td>
              <td>{check.hostname}</td>
              <td>{check.user}</td>
              <td>{check.date}</td>
              <td>
                <span
                  style={{
                    ...styles.badge,
                    backgroundColor:
                      check.firewallState === "ON"
                        ? "#238636"
                        : check.firewallState === "OFF"
                        ? "#da3633"
                        : "#d29922",
                  }}
                >
                  {check.firewallState}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* 🕒 TIMELINE SIMPLE */}
      <h2 style={{ marginTop: "40px" }}>🕒 Timeline</h2>
      <ul style={styles.timeline}>
        {checks.slice(0, 10).map((c) => (
          <li key={c.id}>
            [{c.date}] {c.hostname} → {c.firewallState}
          </li>
        ))}
      </ul>
    </div>
  );
}

const styles = {
  container: {
    backgroundColor: "#0d1117",
    color: "#c9d1d9",
    minHeight: "100vh",
    padding: "2rem",
    fontFamily: "monospace",
  },
  title: {
    color: "#58a6ff",
  },
  button: {
    backgroundColor: "#238636",
    color: "white",
    border: "none",
    padding: "10px 20px",
    cursor: "pointer",
    marginBottom: "20px",
  },
  metrics: {
    display: "flex",
    gap: "20px",
    marginBottom: "20px",
    fontWeight: "bold",
  },
  filters: {
    marginBottom: "20px",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
  },
  badge: {
    padding: "5px 10px",
    borderRadius: "5px",
    color: "white",
    fontWeight: "bold",
  },
  rowAlert: {
    backgroundColor: "#2d1111",
  },
  timeline: {
    marginTop: "10px",
  },
};
export default Dashboard;