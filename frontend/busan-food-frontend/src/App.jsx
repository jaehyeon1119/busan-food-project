import { Routes, Route } from "react-router-dom";
import RestaurantList from "./pages/RestaurantList";
import RestaurantDetail from "./pages/RestaurantDetail";
import Join from "./pages/Join";
import Login from "./pages/Login";

function App() {
  return (
    <Routes>
      <Route path="/" element={<RestaurantList />} />
      <Route path="/detail/:id" element={<RestaurantDetail />} />
      <Route path="/join" element={<Join />} />
      <Route path="/login" element={<Login />} />
    </Routes>
  );
}

export default App;
