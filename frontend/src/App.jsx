import { useState } from 'react'

function App() {
  const [response, setResponse] = useState("")

  const sendCheck = async () => {
    const data = {
      date: "2026-04-26 17:00:00",
      user: "frontend-test",
      hostname: "laptop",
      checks: { firewall_enabled: true }
    }

    try {
      const res = await fetch("http://localhost:8080/api/checks", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
      })

      const text = await res.text()
      setResponse(text)

    } catch (error) {
      console.error(error)
      setResponse("Error conectando con backend")
    }
  }

  return (
    <div>
      <h1>Blue Deploy Dashboard</h1>
      <button onClick={sendCheck}>Enviar Check</button>
      <p>{response}</p>
    </div>
  )
}

export default App
