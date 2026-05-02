import { useEffect, useState } from "react";
import { sendCheck, getChecks } from "../services/api";

function Dashboard() {
  const [checks, setChecks] = useState([]);
  const [status, setStatus] = useState("Idle");
  const [loading, setLoading] = useState(false);

  // 🔥 Cargar checks desde backend
  async function loadChecks() {
    try {
      setLoading(true);
      const data = await getChecks();

      // ✔ Backend manda datos ya procesados → no parseamos JSON
      setChecks(data);

    } catch (error) {
      console.error("Error cargando checks:", error);
      setStatus("Error cargando datos");
    } finally {
      setLoading(false);
    }
  }

  // 🔁 Auto-refresh
  useEffect(() => {
    loadChecks();

    const interval = setInterval(loadChecks, 5000);

    return () => clearInterval(interval);
  }, []);

  // 📤 Enviar nuevo check
  async function handleSend() {
    setStatus("Enviando...");

    const payload = {
      date: new Date().toISOString(),
      user: "frontend-user",
      hostname: "frontend-host",
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
      setStatus("Error al enviar");
    }
  }

  // 🎨 Render estado firewall (estilo SOC)
  function renderFirewallBadge(value) {
    let label = "UNKNOWN";
    let color = "#d29922"; // warning

    if (value === true) {
      label = "ON";
      color = "#238636"; // verde
    } else if (value === false) {
      label = "OFF";
      color = "#da3633"; // rojo
    }

    return (
      <span style={{ ...styles.badge, backgroundColor: color }}>
        {label}
      </span>
    );
  }

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>🛡️ BLUE DEPLOY SOC</h1>

      <button style={styles.button} onClick={handleSend}>
        Enviar Check
      </button>

      <p>{status}</p>

      {loading && <p>Cargando datos...</p>}

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
          {checks.map((check) => (
            <tr key={check.id}>
              <td>{check.id}</td>
              <td>{check.hostname}</td>
              <td>{check.user}</td>
              <td>{check.date}</td>
              <td>{renderFirewallBadge(check.firewallEnabled)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// 🎨 Estilos SOC
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
  table: {
    width: "100%",
    borderCollapse: "collapse",
    marginTop: "20px",
  },
  badge: {
    padding: "5px 10px",
    borderRadius: "5px",
    color: "white",
    fontWeight: "bold",
  },
};

export default Dashboard;