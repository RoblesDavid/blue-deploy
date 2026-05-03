import { useEffect, useState } from "react";
import { sendCheck, getChecks } from "../services/api";
import StatsChart from "../components/StatsChart";

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

  const filteredChecks =
    filter === "ALL"
      ? checks
      : checks.filter((check) => check.firewallState === filter);

  const total = checks.length;
  const alerts = checks.filter((check) => check.firewallState === "OFF").length;
  const protectedHosts = checks.filter(
    (check) => check.firewallState === "ON"
  ).length;
  const unknown = checks.filter(
    (check) => check.firewallState === "UNKNOWN"
  ).length;

  return (
    <main style={styles.page}>
      <section style={styles.shell}>
        <header style={styles.header}>
          <div>
            <div style={styles.eyebrow}>Security Operations Center</div>
            <h1 style={styles.title}>Blue Deploy SOC</h1>
            <p style={styles.subtitle}>
              Monitoriza checks, alertas y estado del firewall en tiempo real.
            </p>
          </div>

          <div style={styles.headerActions}>
            <span style={styles.statusPill}>{status}</span>
            <button style={styles.primaryButton} onClick={handleSend}>
              Enviar Check
            </button>
          </div>
        </header>

        <section style={styles.metricsGrid}>
          <article style={styles.metricCard}>
            <span style={styles.metricLabel}>Total checks</span>
            <strong style={styles.metricValue}>{total}</strong>
            <span style={styles.metricHint}>Eventos registrados</span>
          </article>

          <article style={styles.metricCard}>
            <span style={styles.metricLabel}>Protegidos</span>
            <strong style={{ ...styles.metricValue, color: "#16a34a" }}>
              {protectedHosts}
            </strong>
            <span style={styles.metricHint}>Firewall activo</span>
          </article>

          <article style={styles.metricCard}>
            <span style={styles.metricLabel}>Alertas</span>
            <strong style={{ ...styles.metricValue, color: "#dc2626" }}>
              {alerts}
            </strong>
            <span style={styles.metricHint}>Firewall desactivado</span>
          </article>

          <article style={styles.metricCard}>
            <span style={styles.metricLabel}>Unknown</span>
            <strong style={{ ...styles.metricValue, color: "#d97706" }}>
              {unknown}
            </strong>
            <span style={styles.metricHint}>Sin confirmar</span>
          </article>
        </section>

        <section style={styles.contentGrid}>
          <article style={styles.panel}>
            <div style={styles.panelHeader}>
              <div>
                <h2 style={styles.panelTitle}>Distribución</h2>
                <p style={styles.panelText}>Resumen visual de los estados.</p>
              </div>
            </div>

            <div style={styles.chartBox}>
              <StatsChart checks={checks} />
            </div>
          </article>

          <article style={styles.panel}>
            <div style={styles.panelHeader}>
              <div>
                <h2 style={styles.panelTitle}>Timeline</h2>
                <p style={styles.panelText}>Últimos eventos recibidos.</p>
              </div>
            </div>

            <ul style={styles.timeline}>
              {checks.slice(0, 8).map((check) => (
                <li key={check.id} style={styles.timelineItem}>
                  <span
                    style={{
                      ...styles.timelineDot,
                      backgroundColor: getStateColor(check.firewallState),
                    }}
                  />
                  <div>
                    <strong style={styles.timelineTitle}>
                      {check.hostname}
                    </strong>
                    <p style={styles.timelineText}>
                      {check.date} · {check.firewallState}
                    </p>
                  </div>
                </li>
              ))}

              {checks.length === 0 && (
                <li style={styles.emptyState}>No hay eventos todavía.</li>
              )}
            </ul>
          </article>
        </section>

        <section style={styles.panel}>
          <div style={styles.tableTop}>
            <div>
              <h2 style={styles.panelTitle}>Checks recientes</h2>
              <p style={styles.panelText}>
                Filtra los registros por estado del firewall.
              </p>
            </div>

            <div style={styles.filters}>
              {["ALL", "ON", "OFF", "UNKNOWN"].map((item) => (
                <button
                  key={item}
                  style={{
                    ...styles.filterButton,
                    ...(filter === item ? styles.filterButtonActive : {}),
                  }}
                  onClick={() => setFilter(item)}
                >
                  {item}
                </button>
              ))}
            </div>
          </div>

          <div style={styles.tableWrapper}>
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.th}>ID</th>
                  <th style={styles.th}>Hostname</th>
                  <th style={styles.th}>User</th>
                  <th style={styles.th}>Date</th>
                  <th style={styles.th}>Firewall</th>
                </tr>
              </thead>

              <tbody>
                {filteredChecks.map((check) => (
                  <tr key={check.id} style={styles.tr}>
                    <td style={styles.td}>{check.id}</td>
                    <td style={styles.td}>{check.hostname}</td>
                    <td style={styles.td}>{check.user}</td>
                    <td style={styles.td}>{check.date}</td>
                    <td style={styles.td}>
                      <span
                        style={{
                          ...styles.badge,
                          backgroundColor: getStateColor(check.firewallState),
                        }}
                      >
                        {check.firewallState}
                      </span>
                    </td>
                  </tr>
                ))}

                {filteredChecks.length === 0 && (
                  <tr>
                    <td style={styles.emptyTableCell} colSpan="5">
                      No hay checks para este filtro.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      </section>
    </main>
  );
}

function getStateColor(state) {
  if (state === "ON") return "#16a34a";
  if (state === "OFF") return "#dc2626";
  return "#d97706";
}

const styles = {
  page: {
    width: "100%",
    minHeight: "100vh",
    padding: "32px",
    boxSizing: "border-box",
    background:
      "radial-gradient(circle at top left, rgba(56, 189, 248, 0.65) 0, transparent 34%), linear-gradient(135deg, #0f172a 0%, #1e3a8a 52%, #0f766e 100%)",
    fontFamily:
      "Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
  },
  shell: {
    width: "100%",
    maxWidth: "1280px",
    margin: "0 auto",
  },
  header: {
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    gap: "24px",
    marginBottom: "28px",
    padding: "28px",
    borderRadius: "28px",
    background: "rgba(255, 255, 255, 0.9)",
    border: "1px solid rgba(255, 255, 255, 0.45)",
    boxShadow: "0 24px 70px rgba(2, 6, 23, 0.25)",
    backdropFilter: "blur(18px)",
  },
  eyebrow: {
    marginBottom: "8px",
    color: "#2563eb",
    fontSize: "13px",
    fontWeight: "800",
    letterSpacing: "0.12em",
    textTransform: "uppercase",
  },
  title: {
    margin: 0,
    color: "#0f172a",
    fontSize: "42px",
    lineHeight: "1",
    fontWeight: "900",
    letterSpacing: "-0.04em",
  },
  subtitle: {
    maxWidth: "620px",
    margin: "12px 0 0",
    color: "#64748b",
    fontSize: "16px",
  },
  headerActions: {
    display: "flex",
    alignItems: "center",
    gap: "12px",
    flexWrap: "wrap",
    justifyContent: "flex-end",
  },
  statusPill: {
    padding: "10px 14px",
    borderRadius: "999px",
    color: "#0f172a",
    background: "#e0f2fe",
    border: "1px solid #bae6fd",
    fontSize: "14px",
    fontWeight: "700",
  },
  primaryButton: {
    padding: "13px 18px",
    border: "none",
    borderRadius: "15px",
    color: "#ffffff",
    cursor: "pointer",
    fontSize: "15px",
    fontWeight: "800",
    background: "linear-gradient(135deg, #2563eb, #14b8a6)",
    boxShadow: "0 18px 35px rgba(37, 99, 235, 0.28)",
  },
  metricsGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(4, minmax(0, 1fr))",
    gap: "18px",
    marginBottom: "18px",
  },
  metricCard: {
    padding: "22px",
    borderRadius: "24px",
    background: "rgba(255, 255, 255, 0.9)",
    border: "1px solid rgba(255, 255, 255, 0.45)",
    boxShadow: "0 18px 45px rgba(2, 6, 23, 0.18)",
    backdropFilter: "blur(18px)",
  },
  metricLabel: {
    display: "block",
    color: "#64748b",
    fontSize: "13px",
    fontWeight: "800",
    textTransform: "uppercase",
    letterSpacing: "0.08em",
  },
  metricValue: {
    display: "block",
    marginTop: "10px",
    color: "#0f172a",
    fontSize: "42px",
    lineHeight: "1",
    fontWeight: "900",
  },
  metricHint: {
    display: "block",
    marginTop: "10px",
    color: "#64748b",
    fontSize: "14px",
  },
  contentGrid: {
    display: "grid",
    gridTemplateColumns: "1.35fr 0.65fr",
    gap: "18px",
    marginBottom: "18px",
  },
  panel: {
    padding: "24px",
    borderRadius: "26px",
    background: "rgba(255, 255, 255, 0.92)",
    border: "1px solid rgba(255, 255, 255, 0.45)",
    boxShadow: "0 22px 60px rgba(2, 6, 23, 0.2)",
    backdropFilter: "blur(18px)",
  },
  panelHeader: {
    display: "flex",
    alignItems: "flex-start",
    justifyContent: "space-between",
    gap: "18px",
    marginBottom: "18px",
  },
  panelTitle: {
    margin: 0,
    color: "#0f172a",
    fontSize: "22px",
    fontWeight: "900",
  },
  panelText: {
    margin: "6px 0 0",
    color: "#64748b",
    fontSize: "14px",
  },
  chartBox: {
    minHeight: "260px",
    overflow: "hidden",
  },
  timeline: {
    display: "flex",
    flexDirection: "column",
    gap: "14px",
    margin: 0,
    padding: 0,
    listStyle: "none",
  },
  timelineItem: {
    display: "flex",
    alignItems: "flex-start",
    gap: "12px",
    padding: "12px",
    borderRadius: "16px",
    background: "#f8fafc",
    border: "1px solid #e2e8f0",
  },
  timelineDot: {
    width: "11px",
    height: "11px",
    marginTop: "7px",
    borderRadius: "999px",
    flex: "0 0 auto",
    boxShadow: "0 0 0 4px rgba(37, 99, 235, 0.1)",
  },
  timelineTitle: {
    display: "block",
    color: "#0f172a",
    fontSize: "14px",
  },
  timelineText: {
    margin: "3px 0 0",
    color: "#64748b",
    fontSize: "13px",
  },
  tableTop: {
    display: "flex",
    alignItems: "flex-start",
    justifyContent: "space-between",
    gap: "18px",
    marginBottom: "18px",
  },
  filters: {
    display: "flex",
    gap: "8px",
    flexWrap: "wrap",
    justifyContent: "flex-end",
  },
  filterButton: {
    padding: "9px 12px",
    borderRadius: "999px",
    border: "1px solid #cbd5e1",
    color: "#334155",
    background: "#ffffff",
    cursor: "pointer",
    fontSize: "13px",
    fontWeight: "800",
  },
  filterButtonActive: {
    color: "#ffffff",
    borderColor: "transparent",
    background: "linear-gradient(135deg, #2563eb, #14b8a6)",
    boxShadow: "0 12px 25px rgba(37, 99, 235, 0.22)",
  },
  tableWrapper: {
    width: "100%",
    overflowX: "auto",
    borderRadius: "18px",
    border: "1px solid #e2e8f0",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
    minWidth: "720px",
    background: "#ffffff",
  },
  th: {
    padding: "14px 16px",
    color: "#475569",
    background: "#f8fafc",
    borderBottom: "1px solid #e2e8f0",
    textAlign: "left",
    fontSize: "13px",
    fontWeight: "900",
    textTransform: "uppercase",
    letterSpacing: "0.06em",
  },
  tr: {
    borderBottom: "1px solid #e2e8f0",
  },
  td: {
    padding: "15px 16px",
    color: "#334155",
    textAlign: "left",
    fontSize: "14px",
  },
  badge: {
    display: "inline-flex",
    alignItems: "center",
    justifyContent: "center",
    minWidth: "82px",
    padding: "7px 10px",
    borderRadius: "999px",
    color: "#ffffff",
    fontSize: "12px",
    fontWeight: "900",
    letterSpacing: "0.04em",
  },
  emptyState: {
    padding: "18px",
    borderRadius: "16px",
    color: "#64748b",
    background: "#f8fafc",
    border: "1px solid #e2e8f0",
    listStyle: "none",
  },
  emptyTableCell: {
    padding: "24px",
    color: "#64748b",
    textAlign: "center",
    fontSize: "14px",
  },
};

export default Dashboard;