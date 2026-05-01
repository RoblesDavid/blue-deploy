/**
- Maneja estado (useState)
- Construye payload
- Llama a API
- Muestra respuesta
*/

import { useState } from "react";
import { sendCheck } from "../services/api";

export default function Dashboard() {
  const [status, setStatus] = useState("Idle");

  const handleSend = async () => {
    setStatus("Enviando...");

    const payload = {
      date: new Date().toISOString(),
      user: "soc-analyst",
      hostname: "frontend-node",
      checks: { firewall_enabled: true },
    };

    try {
      const res = await sendCheck(payload);
      setStatus("✔ " + res);
    } catch (err) {
      setStatus("❌ Error");
    }
  };

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>BLUE DEPLOY SOC</h1>

      <div style={styles.card}>
        <h2>System Check</h2>
        <button style={styles.button} onClick={handleSend}>
          Enviar Check
        </button>
        <p style={styles.status}>{status}</p>
      </div>
    </div>
  );
}

const styles = {
  container: {
    backgroundColor: "#0d1117",
    color: "#c9d1d9",
    minHeight: "100vh",
    padding: "40px",
    fontFamily: "monospace",
  },
  title: {
    color: "#58a6ff",
  },
  card: {
    backgroundColor: "#161b22",
    padding: "20px",
    borderRadius: "10px",
    width: "300px",
    boxShadow: "0 0 10px rgba(0,0,0,0.5)",
  },
  button: {
    backgroundColor: "#238636",
    color: "white",
    border: "none",
    padding: "10px",
    cursor: "pointer",
    marginTop: "10px",
  },
  status: {
    marginTop: "10px",
    color: "#8b949e",
  },
};