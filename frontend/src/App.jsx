import { useState } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import Login from "./pages/Login";

function App() {
  const [auth, setAuth] = useState(false);

  return (
    <BrowserRouter>
      <Routes>

        {/* 🔒 Ruta protegida */}
        <Route
          path="/"
          element={auth ? <Dashboard /> : <Navigate to="/login" />}
        />

        {/* 🔓 Login */}
        <Route path="/login" element={<Login onLogin={setAuth} />} />

      </Routes>
    </BrowserRouter>
  );
}

export default App;
