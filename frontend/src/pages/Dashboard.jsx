import { useEffect, useState } from "react";
import { sendCheck, getChecks } from "../services/api";

function Dashboard() {
  const [checks, setChecks] = useState([]);
  const [status, setStatus] = useState("Idle");

  async function loadChecks() {
    try {
      const data = await getChecks();
      setChecks(data);
    } catch (error) {
      console.error("Error cargando checks:", error);
    }
  }

  useEffect(() => {
    loadChecks();
  }, []);

  async function handleSend() {
    setStatus("Enviando...");

    const payload = {
      date: new Date().toISOString(),
      user: "frontend-user",
      hostname: "frontend-host",
      checks: {
        firewall_enabled: true,
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

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>BLUE DEPLOY SOC</h1>

      <button style={styles.button} onClick={handleSend}>
        Enviar Check
      </button>

      <p>{status}</p>

      <table style={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Hostname</th>
            <th>User</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          {checks.map((check) => (
            <tr key={check.id}>
              <td>{check.id}</td>
              <td>{check.hostname}</td>
              <td>{check.user}</td>
              <td>{check.createdAt}</td>
            </tr>
          ))}
        </tbody>
      </table>
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
  table: {
    width: "100%",
    borderCollapse: "collapse",
    marginTop: "20px",
  },
};

export default Dashboard;