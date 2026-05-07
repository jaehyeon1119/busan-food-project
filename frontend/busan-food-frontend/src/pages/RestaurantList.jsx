import Header from "../components/Header";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getRestaurants } from "../api/api";
import "../style.css";

function RestaurantList() {
  const [list, setList] = useState([]);
  const [keyword, setKeyword] = useState("");
  const [region, setRegion] = useState("");

  const navigate = useNavigate();

  useEffect(() => {
    getRestaurants().then((res) => {
      setList(res.data);
    });
  }, []);

  const regionList = [...new Set(list.map((r) => r.region).filter(Boolean))];

  const filteredList = list.filter((r) => {
    const nameMatch = r.name?.toLowerCase().includes(keyword.toLowerCase());
    const regionMatch = region === "" || r.region === region;
    return nameMatch && regionMatch;
  });

  return (
    <>
      <Header />

      <div className="page">
        <header className="main-header">
          <h1>부산 맛집 리스트</h1>
          <p>
            대표메뉴, 리뷰, 매장정보, 네이버 지도를 한 번에 확인하는 웹서비스
          </p>

          <div className="filter-box">
            <input
              className="search-input"
              type="text"
              placeholder="음식점 이름 검색"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
            />

            <select
              className="region-select"
              value={region}
              onChange={(e) => setRegion(e.target.value)}
            >
              <option value="">전체 구</option>
              {regionList.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </div>
        </header>

        <div className="restaurant-grid">
          {filteredList.length === 0 ? (
            <div className="empty-box">
              <p>검색 결과가 없습니다.</p>
            </div>
          ) : (
            filteredList.map((r) => (
              <div className="restaurant-card" key={r.restaurantId}>
                <img
                  className="restaurant-img"
                  src={r.mainImage}
                  alt={r.name}
                />

                <div className="restaurant-info">
                  <p className="category">
                    {r.category} · {r.region}
                  </p>

                  <h2>{r.name}</h2>

                  <p>대표메뉴: {r.mainMenu}</p>

                  <div className="list-rating-box">
                    <span>⭐ {Number(r.rating || 0).toFixed(1)}</span>
                    <span>{r.reviewCount || 0}개 리뷰</span>
                  </div>

                  <button onClick={() => navigate(`/detail/${r.restaurantId}`)}>
                    상세보기
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </>
  );
}

export default RestaurantList;
