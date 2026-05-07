import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { joinMember } from "../api/api";
import "../style.css";

function Join() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    loginId: "",
    password: "",
    name: "",
    phone: "",
  });

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleJoin = () => {
    joinMember(form)
      .then(() => {
        alert("회원가입 완료");
        navigate("/login");
      })
      .catch(() => {
        alert("회원가입 실패");
      });
  };

  return (
    <div className="page">
      <div className="form-box">
        <h1>회원가입</h1>

        <input name="loginId" placeholder="아이디" onChange={handleChange} />
        <input
          name="password"
          type="password"
          placeholder="비밀번호"
          onChange={handleChange}
        />
        <input name="name" placeholder="이름" onChange={handleChange} />
        <input name="phone" placeholder="전화번호" onChange={handleChange} />

        <button onClick={handleJoin}>회원가입</button>
      </div>
    </div>
  );
}

export default Join;
