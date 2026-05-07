import axios from "axios";

const API = "http://localhost:8080/api";

// 음식점
export const getRestaurants = () => axios.get(`${API}/restaurants`);

export const getRestaurantDetail = (id) =>
  axios.get(`${API}/restaurants/${id}`);

// 리뷰
export const getReviews = (id) => axios.get(`${API}/reviews/${id}`);

export const writeReview = (data) => axios.post(`${API}/reviews`, data);

export const deleteReview = (id) => axios.delete(`${API}/reviews/${id}`);

// 평점 통계
export const getStats = (id) => axios.get(`${API}/restaurants/${id}/stats`);

// 회원가입
export const joinMember = (data) => axios.post(`${API}/members/join`, data);

// 로그인
export const loginMember = (data) => axios.post(`${API}/members/login`, data);

// 교통편
export const getNearbyTransport = (lat, lng, region, address) =>
  axios.get(`${API}/transport/nearby`, {
    params: {
      lat,
      lng,
      region,
      address,
    },
  });
