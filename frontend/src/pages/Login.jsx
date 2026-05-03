import { useState } from "react";
import { useNavigate } from "react-router-dom";

function Login({ onLogin }) {
  const [user, setUser] = useState("");
  const [pass, setPass] = useState("");
  const navigate = useNavigate();

  function handleLogin() {
    if (user === "admin" && pass === "admin") {
      onLogin(true);
      navigate("/"); // 🔥 REDIRECCIÓN
    }
  }

  return (
    <div>
      <input placeholder="User" onChange={(e) => setUser(e.target.value)} />
      <input
        type="password"
        placeholder="Password"
        onChange={(e) => setPass(e.target.value)}
      />
      <button onClick={handleLogin}>Login</button>
    </div>
  );
}

export default Login;