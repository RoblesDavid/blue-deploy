//Centraliza llamadas HTTP
//Permite cambiar backend sin tocar UI

const API_URL = "http://localhost:8080/api/checks";

export async function sendCheck(data) {
  const res = await fetch(API_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
}
  export async function getChecks() {
    const res = await fetch(API_URL);
    return await res.json();
  }