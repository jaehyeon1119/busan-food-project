import { Link, useNavigate } from "react-router-dom";
import "../style.css";

function Header() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("loginUser"));

  const handleLogout = () => {
    localStorage.removeItem("loginUser");
    alert("로그아웃 되었습니다.");
    navigate("/");
    window.location.reload();
  };

  return (
    <div className="top-nav">
      <div className="logo" onClick={() => navigate("/")}>
        부산 맛집
      </div>

      <div className="nav-menu">
        {user ? (
          <>
            <span>{user.name}님</span>
            <button onClick={handleLogout}>로그아웃</button>
          </>
        ) : (
          <>
            <Link to="/login">로그인</Link>
            <Link to="/join">회원가입</Link>
          </>
        )}
      </div>
    </div>
  );
}

export default Header;
