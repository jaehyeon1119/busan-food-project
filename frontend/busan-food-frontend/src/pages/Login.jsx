import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginMember } from "../api/api";
import "../style.css";

function Login() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    loginId: "",
    password: "",
  });

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleLogin = () => {
    loginMember(form)
      .then((res) => {
        if (res.data) {
          localStorage.setItem("loginUser", JSON.stringify(res.data));
          alert("로그인 성공");
          navigate("/");
        } else {
          alert("아이디 또는 비밀번호가 틀렸습니다.");
        }
      })
      .catch(() => {
        alert("로그인 실패");
      });
  };

  return (
    <div className="page">
      <div className="form-box">
        <h1>로그인</h1>

        <input name="loginId" placeholder="아이디" onChange={handleChange} />
        <input
          name="password"
          type="password"
          placeholder="비밀번호"
          onChange={handleChange}
        />

        <button onClick={handleLogin}>로그인</button>
      </div>
    </div>
  );
}

export default Login;
