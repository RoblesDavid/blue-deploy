const API_URL = "http://localhost:8080/api/checks";

//sendCheck() → POST al backend
export async function sendCheck(data) {
  const response = await fetch(API_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  return await response.text();
}

//getChecks() → GET de todos los registros
export async function getChecks() {
  const response = await fetch(API_URL);
  return await response.json();
}