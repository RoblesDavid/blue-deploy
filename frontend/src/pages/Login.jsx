import { useState } from "react";
import { useNavigate } from "react-router-dom";

function Login({ onLogin }) {
  const [user, setUser] = useState("");
  const [pass, setPass] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  function handleLogin(event) {
    event?.preventDefault();

    if (user === "admin" && pass === "admin") {
      setError("");
      onLogin(true);
      navigate("/");
      return;
    }

    setError("Usuario o contraseña incorrectos");
  }

  return (
    <main style={styles.page}>
      <section style={styles.card}>
        <div style={styles.brand}>
          <div style={styles.logo}>BD</div>

          <div>
            <h1 style={styles.title}>Blue Deploy</h1>
            <p style={styles.subtitle}>Panel de monitoreo y despliegue</p>
          </div>
        </div>

        <form onSubmit={handleLogin} style={styles.form}>
          <label style={styles.label}>
            Usuario
            <input
              style={styles.input}
              type="text"
              placeholder="Ingresa tu usuario"
              value={user}
              onChange={(e) => setUser(e.target.value)}
            />
          </label>

          <label style={styles.label}>
            Contraseña
            <input
              style={styles.input}
              type="password"
              placeholder="Ingresa tu contraseña"
              value={pass}
              onChange={(e) => setPass(e.target.value)}
            />
          </label>

          {error && <p style={styles.error}>{error}</p>}

          <button type="submit" style={styles.button}>
            Iniciar sesión
          </button>
        </form>

        <p style={styles.hint}>Credenciales de prueba: admin / admin</p>
      </section>
    </main>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    padding: "24px",
    background:
      "radial-gradient(circle at top left, #38bdf8 0, transparent 32%), linear-gradient(135deg, #0f172a 0%, #1e3a8a 55%, #0f766e 100%)",
    fontFamily:
      "Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
  },
  card: {
    width: "100%",
    maxWidth: "430px",
    padding: "34px",
    borderRadius: "28px",
    background: "rgba(255, 255, 255, 0.92)",
    boxShadow: "0 30px 80px rgba(2, 6, 23, 0.38)",
    backdropFilter: "blur(18px)",
    border: "1px solid rgba(255, 255, 255, 0.45)",
  },
  brand: {
    display: "flex",
    alignItems: "center",
    gap: "16px",
    marginBottom: "30px",
  },
  logo: {
    width: "58px",
    height: "58px",
    display: "grid",
    placeItems: "center",
    borderRadius: "18px",
    color: "#ffffff",
    fontSize: "20px",
    fontWeight: "800",
    letterSpacing: "0.5px",
    background: "linear-gradient(135deg, #2563eb, #14b8a6)",
    boxShadow: "0 16px 35px rgba(37, 99, 235, 0.35)",
  },
  title: {
    margin: 0,
    color: "#0f172a",
    fontSize: "30px",
    lineHeight: "1.1",
  },
  subtitle: {
    margin: "6px 0 0",
    color: "#64748b",
    fontSize: "14px",
  },
  form: {
    display: "flex",
    flexDirection: "column",
    gap: "18px",
  },
  label: {
    display: "flex",
    flexDirection: "column",
    gap: "8px",
    color: "#334155",
    fontSize: "14px",
    fontWeight: "700",
  },
  input: {
    width: "100%",
    boxSizing: "border-box",
    padding: "14px 16px",
    borderRadius: "14px",
    border: "1px solid #cbd5e1",
    outline: "none",
    color: "#0f172a",
    background: "#f8fafc",
    fontSize: "15px",
  },
  error: {
    margin: 0,
    padding: "12px 14px",
    borderRadius: "12px",
    color: "#b91c1c",
    background: "#fee2e2",
    fontSize: "14px",
    fontWeight: "600",
  },
  button: {
    marginTop: "4px",
    padding: "15px 18px",
    border: "none",
    borderRadius: "16px",
    color: "#ffffff",
    cursor: "pointer",
    fontSize: "16px",
    fontWeight: "800",
    background: "linear-gradient(135deg, #2563eb, #14b8a6)",
    boxShadow: "0 18px 35px rgba(37, 99, 235, 0.32)",
  },
  hint: {
    margin: "24px 0 0",
    color: "#64748b",
    textAlign: "center",
    fontSize: "13px",
  },
};

export default Login;