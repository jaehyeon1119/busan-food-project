import Header from "../components/Header";
import { useEffect, useState, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  getRestaurantDetail,
  getReviews,
  writeReview,
  deleteReview,
  getStats,
  getNearbyTransport,
} from "../api/api";
import "../style.css";

function RestaurantDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const loginUser = JSON.parse(localStorage.getItem("loginUser"));

  const mapRef = useRef(null);

  const [restaurant, setRestaurant] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [stats, setStats] = useState({
    avgRating: 0,
    reviewCount: 0,
  });

  const [transport, setTransport] = useState({
    subwayText: "🚇 지하철 정보 조회 중",
    busText: "🚌 버스 정보 조회 중",
    parkingText: "🚗 주차 정보 조회 중",
  });

  const [rating, setRating] = useState(5);
  const [content, setContent] = useState("");

  const avgRating = Number(
    stats.avgRating || stats.AVGRATING || stats.avg_rating || 0,
  );

  const reviewCount = Number(
    stats.reviewCount || stats.REVIEWCOUNT || stats.review_count || 0,
  );

  const loadRestaurant = () => {
    getRestaurantDetail(id)
      .then((res) => {
        setRestaurant(res.data);
      })
      .catch((err) => {
        console.error("음식점 상세 조회 실패", err);
      });
  };

  const loadReviews = () => {
    getReviews(id)
      .then((res) => {
        setReviews(res.data);
      })
      .catch((err) => {
        console.error("리뷰 조회 실패", err);
        setReviews([]);
      });
  };

  const loadStats = () => {
    getStats(id)
      .then((res) => {
        setStats(res.data || { avgRating: 0, reviewCount: 0 });
      })
      .catch((err) => {
        console.error("평점/리뷰수 조회 실패", err);
        setStats({ avgRating: 0, reviewCount: 0 });
      });
  };

  const loadTransport = (restaurantData) => {
    const lat = Number(restaurantData.lat);
    const lng = Number(restaurantData.lng);

    if (!lat || !lng) {
      setTransport({
        subwayText: "🚇 지하철: 위치 정보 없음",
        busText: "🚌 버스: 위치 정보 없음",
        parkingText: "🚗 주차: 위치 정보 없음",
      });
      return;
    }

    getNearbyTransport(
      lat,
      lng,
      restaurantData.region || "",
      restaurantData.address || "",
    )
      .then((res) => {
        setTransport(res.data);
      })
      .catch((err) => {
        console.error("교통편 조회 실패", err);
        setTransport({
          subwayText: `🚇 지하철: ${restaurantData.region} 주요역 하차 후 도보 약 5~10분`,
          busText: "🚌 버스: 인근 정류장 이용 가능",
          parkingText: "🚗 주차: 매장 문의 필요",
        });
      });
  };

  useEffect(() => {
    loadRestaurant();
    loadReviews();
    loadStats();
  }, [id]);

  useEffect(() => {
    if (restaurant) {
      loadTransport(restaurant);
    }
  }, [restaurant]);

  useEffect(() => {
    if (!window.naver || !restaurant || !mapRef.current) return;

    const lat = Number(restaurant.lat);
    const lng = Number(restaurant.lng);

    if (!lat || !lng) return;

    const position = new window.naver.maps.LatLng(lat, lng);

    const map = new window.naver.maps.Map(mapRef.current, {
      center: position,
      zoom: 17,
    });

    new window.naver.maps.Marker({
      position: position,
      map: map,
    });
  }, [restaurant]);

  const openMap = () => {
    if (!restaurant) return;

    window.open(
      `https://map.naver.com/p/search/${encodeURIComponent(restaurant.name)}`,
      "_blank",
    );
  };

  const handleWrite = () => {
    if (!loginUser) {
      alert("로그인 후 작성 가능");
      navigate("/login");
      return;
    }

    if (!content.trim()) {
      alert("리뷰 내용을 입력하세요.");
      return;
    }

    writeReview({
      restaurantId: Number(id),
      memberId: loginUser.memberId,
      rating: Number(rating),
      content: content,
    }).then(() => {
      alert("리뷰 등록 완료");
      setRating(5);
      setContent("");
      loadReviews();
      loadStats();
    });
  };

  const handleDelete = (reviewId) => {
    if (!window.confirm("삭제할까요?")) return;

    deleteReview(reviewId).then(() => {
      alert("삭제 완료");
      loadReviews();
      loadStats();
    });
  };

  if (!restaurant) {
    return (
      <>
        <Header />
        <div className="detail-page">
          <div className="section-box">로딩중...</div>
        </div>
      </>
    );
  }

  return (
    <>
      <Header />

      <div className="detail-page">
        <button className="back-btn" onClick={() => navigate("/")}>
          ← 목록으로
        </button>

        <section className="detail-visual-card">
          <div className="detail-image-wrap">
            <img
              className="detail-img"
              src={restaurant.mainImage}
              alt={restaurant.name}
            />
          </div>

          <div className="detail-info">
            <span className="detail-badge">
              {restaurant.category} · {restaurant.region}
            </span>

            <h1>{restaurant.name}</h1>

            <div className="rating-box">
              <span>{"⭐".repeat(Math.round(avgRating))}</span>
              <strong>{avgRating.toFixed(1)}</strong>
              <span>({reviewCount}개 리뷰)</span>
            </div>

            <p>📍 {restaurant.address || "주소 정보 없음"}</p>
            <p>📞 {restaurant.tel || "연락처 정보 없음"}</p>
            <p>⏰ {restaurant.openTime || "운영시간 정보 없음"}</p>
          </div>

          <div className="map-box" ref={mapRef}></div>
        </section>

        <section className="section-box">
          <div className="section-title-row">
            <h2>매장 정보</h2>
            <span>Store Info</span>
          </div>

          <p className="description-text">
            {restaurant.description || "매장 정보가 없습니다."}
          </p>
        </section>

        <section className="section-box">
          <div className="section-title-row">
            <h2>교통편</h2>
            <span>Transport</span>
          </div>

          <div className="transport-box">
            <p>{transport.subwayText}</p>
            <p>{transport.busText}</p>
            <p>{transport.parkingText}</p>

            <button onClick={openMap}>네이버 지도 길찾기</button>
          </div>
        </section>

        <section className="section-box">
          <div className="section-title-row">
            <h2>메뉴 목록</h2>
            <span>Menu</span>
          </div>

          {restaurant.mainMenu ? (
            restaurant.mainMenu.split(",").map((menu, idx) => (
              <div className="menu-row" key={idx}>
                <span>{menu.trim()}</span>
              </div>
            ))
          ) : (
            <p className="empty-text">등록된 메뉴가 없습니다.</p>
          )}
        </section>

        <section className="section-box">
          <div className="section-title-row">
            <h2>리뷰 작성</h2>
            <span>Review</span>
          </div>

          {loginUser ? (
            <div className="review-form">
              <p className="login-name">{loginUser.name}님</p>

              <select
                value={rating}
                onChange={(e) => setRating(Number(e.target.value))}
              >
                <option value={5}>5점</option>
                <option value={4}>4점</option>
                <option value={3}>3점</option>
                <option value={2}>2점</option>
                <option value={1}>1점</option>
              </select>

              <textarea
                placeholder="리뷰를 입력하세요"
                value={content}
                onChange={(e) => setContent(e.target.value)}
              />

              <button onClick={handleWrite}>리뷰 등록</button>
            </div>
          ) : (
            <p className="empty-text">로그인 후 리뷰를 작성할 수 있습니다.</p>
          )}
        </section>

        <section className="section-box">
          <div className="section-title-row">
            <h2>리뷰 목록</h2>
            <span>Comments</span>
          </div>

          {reviews.length === 0 ? (
            <p className="empty-text">등록된 리뷰가 없습니다.</p>
          ) : (
            reviews.map((r) => (
              <div className="review-box" key={r.reviewId}>
                <p className="review-writer">👤 {r.memberName}</p>
                <p className="review-rating">⭐ {r.rating}</p>
                <p className="review-content">{r.content}</p>

                {loginUser && loginUser.memberId === r.memberId && (
                  <div className="review-actions">
                    <button onClick={() => handleDelete(r.reviewId)}>
                      삭제
                    </button>
                  </div>
                )}
              </div>
            ))
          )}
        </section>
      </div>
    </>
  );
}

export default RestaurantDetail;
