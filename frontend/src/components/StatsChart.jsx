import {
  Chart as ChartJS,
  BarElement,
  CategoryScale,
  LinearScale,
} from "chart.js";
import { Bar } from "react-chartjs-2";

ChartJS.register(BarElement, CategoryScale, LinearScale);

function StatsChart({ checks }) {
  const data = {
    labels: ["Firewall", "Antivirus", "Disk", "Updates"],
    datasets: [
      {
        label: "Checks OK",
        data: [
          checks.filter(c => c.firewallEnabled === true).length,
          checks.filter(c => c.antivirusEnabled === true).length,
          checks.filter(c => c.diskEncrypted === true).length,
          checks.filter(c => c.updatesEnabled === true).length,
        ],
      },
    ],
  };

  return <Bar data={data} />;
}

export default StatsChart;