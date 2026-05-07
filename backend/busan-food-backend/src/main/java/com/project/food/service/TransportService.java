package com.project.food.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.food.dto.TransportDto;

@Service
public class TransportService {

    private static final String SERVICE_KEY =
            "829f9e0018ebb9dcf9b53922adc67260a4139cbade1a4df94d3c9869169cab4b";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransportDto getNearbyTransport(double lat, double lng, String region, String address) {
        TransportDto dto = new TransportDto();

        dto.setSubwayText(getNearestSubwayText(lat, lng));
        dto.setBusText(getNearestBusStopText(lat, lng));
        dto.setParkingText(getNearestParkingText(lat, lng));

        return dto;
    }

    private String getNearestSubwayText(double lat, double lng) {
        List<SubwayStation> stations = getSubwayStations();

        SubwayStation nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (SubwayStation station : stations) {
            double distance = distanceMeter(lat, lng, station.lat, station.lng);

            if (distance < minDistance) {
                minDistance = distance;
                nearest = station;
            }
        }

        if (nearest == null) {
            return "🚇 지하철: 인근 지하철역 정보 없음";
        }

        int walkMin = walkingMinute(minDistance);

        return "🚇 지하철: " + nearest.line + " " + nearest.name + "역에서 도보 약 " + walkMin + "분";
    }

    private String getNearestBusStopText(double lat, double lng) {
        try {
            String url = "https://apis.data.go.kr/6260000/BusanBIMS/busStopList"
                    + "?serviceKey=" + SERVICE_KEY
                    + "&pageNo=1"
                    + "&numOfRows=8780";

            String xml = restTemplate.getForObject(url, String.class);

            List<BusStop> busStops = parseBusStops(xml);

            BusStop nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (BusStop stop : busStops) {
                double distance = distanceMeter(lat, lng, stop.lat, stop.lng);

                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = stop;
                }
            }

            if (nearest == null) {
                return "🚌 버스: 인근 정류장 정보 없음";
            }

            int walkMin = walkingMinute(minDistance);

            String arsText = nearest.arsNo == null || nearest.arsNo.isBlank()
                    ? "ARS 정보 없음"
                    : "ARS " + nearest.arsNo;

            return "🚌 버스: " + nearest.name + " 정류장(" + arsText + ") 도보 약 " + walkMin + "분";

        } catch (Exception e) {
            e.printStackTrace();
            return "🚌 버스: 인근 정류장 이용 가능";
        }
    }

    private String getNearestParkingText(double lat, double lng) {
        try {
            String url = "https://apis.data.go.kr/6260000/BusanPblcPrkngInfoService/getPblcPrkngInfo"
                    + "?serviceKey=" + SERVICE_KEY
                    + "&pageNo=1"
                    + "&numOfRows=615"
                    + "&resultType=json";

            String json = restTemplate.getForObject(url, String.class);

            Map<String, Object> root = objectMapper.readValue(json, Map.class);
            Map<String, Object> response = (Map<String, Object>) root.get("response");
            Map<String, Object> body = (Map<String, Object>) response.get("body");
            Map<String, Object> items = (Map<String, Object>) body.get("items");

            Object itemObject = items.get("item");

            List<Map<String, Object>> itemList = new ArrayList<>();

            if (itemObject instanceof List) {
                itemList = (List<Map<String, Object>>) itemObject;
            } else if (itemObject instanceof Map) {
                itemList.add((Map<String, Object>) itemObject);
            }

            Parking nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (Map<String, Object> item : itemList) {
                String name = getString(item, "pkNam");
                String count = getString(item, "pkCnt");
                String tenMin = getString(item, "tenMin");
                String dayMax = getString(item, "ftDay");
                String addFee = getString(item, "feeAdd");

                double itemLat = getDouble(item, "xCdnt");
                double itemLng = getDouble(item, "yCdnt");

                if (name.isEmpty() || itemLat == 0 || itemLng == 0) {
                    continue;
                }

                double distance = distanceMeter(lat, lng, itemLat, itemLng);

                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = new Parking(name, count, tenMin, dayMax, addFee);
                }
            }

            if (nearest == null) {
                return "🚗 주차: 매장 주차 여부는 현장 확인 필요 / 인근 공영주차장 정보 없음";
            }

            String countText = nearest.count.isEmpty()
                    ? "주차면수 정보 없음"
                    : nearest.count + "면";

            String feeText = makeParkingFeeText(nearest.tenMin, nearest.addFee, nearest.dayMax);

            return "🚗 주차: 매장 주차 여부는 현장 확인 필요 / 인근 공영주차장: "
                    + nearest.name + " / " + countText + " / " + feeText;

        } catch (Exception e) {
            e.printStackTrace();
            return "🚗 주차: 매장 주차 여부는 현장 확인 필요 / 인근 공영주차장 조회 실패";
        }
    }

    private String makeParkingFeeText(String tenMin, String addFee, String dayMax) {
        List<String> feeList = new ArrayList<>();

        if (!tenMin.isEmpty()) {
            if ("0".equals(tenMin)) {
                feeList.add("10분 요금 무료 또는 현장 확인 필요");
            } else {
                feeList.add("10분 " + formatWon(tenMin) + "원");
            }
        }

        if (!addFee.isEmpty() && !"0".equals(addFee)) {
            feeList.add("추가 " + formatWon(addFee) + "원");
        }

        if (!dayMax.isEmpty() && !"0".equals(dayMax)) {
            feeList.add("일 최대 " + formatWon(dayMax) + "원");
        }

        if (feeList.isEmpty()) {
            return "요금 정보 없음";
        }

        return String.join(" / ", feeList);
    }

    private List<BusStop> parseBusStops(String xml) {
        List<BusStop> list = new ArrayList<>();

        String[] items = xml.split("<item>");

        for (String item : items) {
            if (!item.contains("</item>")) {
                continue;
            }

            String name = getXmlValue(item, "bstopnm");
            String arsNo = getXmlValue(item, "arsno");
            double lng = parseDouble(getXmlValue(item, "gpsx"));
            double lat = parseDouble(getXmlValue(item, "gpsy"));

            if (!name.isEmpty() && lat != 0 && lng != 0) {
                list.add(new BusStop(name, arsNo, lat, lng));
            }
        }

        return list;
    }

    private String getXmlValue(String xml, String tagName) {
        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";

        int start = xml.indexOf(startTag);
        int end = xml.indexOf(endTag);

        if (start == -1 || end == -1) {
            return "";
        }

        return xml.substring(start + startTag.length(), end).trim();
    }

    private List<SubwayStation> getSubwayStations() {
        List<SubwayStation> list = new ArrayList<>();

        // 부산 1호선
        list.add(new SubwayStation("1호선", "다대포해수욕장", 35.0486, 128.9655));
        list.add(new SubwayStation("1호선", "다대포항", 35.0576, 128.9713));
        list.add(new SubwayStation("1호선", "낫개", 35.0651, 128.9796));
        list.add(new SubwayStation("1호선", "신장림", 35.0745, 128.9774));
        list.add(new SubwayStation("1호선", "장림", 35.0816, 128.9777));
        list.add(new SubwayStation("1호선", "동매", 35.0897, 128.9737));
        list.add(new SubwayStation("1호선", "신평", 35.0950, 128.9605));
        list.add(new SubwayStation("1호선", "하단", 35.1061, 128.9668));
        list.add(new SubwayStation("1호선", "당리", 35.1035, 128.9737));
        list.add(new SubwayStation("1호선", "사하", 35.0998, 128.9835));
        list.add(new SubwayStation("1호선", "괴정", 35.0999, 128.9925));
        list.add(new SubwayStation("1호선", "대티", 35.1030, 129.0007));
        list.add(new SubwayStation("1호선", "서대신", 35.1100, 129.0123));
        list.add(new SubwayStation("1호선", "동대신", 35.1108, 129.0177));
        list.add(new SubwayStation("1호선", "토성", 35.1007, 129.0206));
        list.add(new SubwayStation("1호선", "자갈치", 35.0979, 129.0267));
        list.add(new SubwayStation("1호선", "남포", 35.0970, 129.0340));
        list.add(new SubwayStation("1호선", "중앙", 35.1038, 129.0366));
        list.add(new SubwayStation("1호선", "부산역", 35.1152, 129.0392));
        list.add(new SubwayStation("1호선", "초량", 35.1210, 129.0424));
        list.add(new SubwayStation("1호선", "부산진", 35.1278, 129.0476));
        list.add(new SubwayStation("1호선", "좌천", 35.1343, 129.0541));
        list.add(new SubwayStation("1호선", "범일", 35.1413, 129.0593));
        list.add(new SubwayStation("1호선", "범내골", 35.1472, 129.0595));
        list.add(new SubwayStation("1호선", "서면", 35.1577, 129.0592));
        list.add(new SubwayStation("1호선", "부전", 35.1626, 129.0627));
        list.add(new SubwayStation("1호선", "양정", 35.1704, 129.0695));
        list.add(new SubwayStation("1호선", "시청", 35.1798, 129.0763));
        list.add(new SubwayStation("1호선", "연산", 35.1860, 129.0814));
        list.add(new SubwayStation("1호선", "교대", 35.1959, 129.0803));
        list.add(new SubwayStation("1호선", "동래", 35.2055, 129.0785));
        list.add(new SubwayStation("1호선", "명륜", 35.2124, 129.0797));
        list.add(new SubwayStation("1호선", "온천장", 35.2211, 129.0866));
        list.add(new SubwayStation("1호선", "부산대", 35.2302, 129.0893));
        list.add(new SubwayStation("1호선", "장전", 35.2380, 129.0876));
        list.add(new SubwayStation("1호선", "구서", 35.2472, 129.0917));
        list.add(new SubwayStation("1호선", "두실", 35.2572, 129.0915));
        list.add(new SubwayStation("1호선", "남산", 35.2656, 129.0922));
        list.add(new SubwayStation("1호선", "범어사", 35.2731, 129.0921));
        list.add(new SubwayStation("1호선", "노포", 35.2838, 129.0954));

        // 부산 2호선
        list.add(new SubwayStation("2호선", "장산", 35.1699, 129.1767));
        list.add(new SubwayStation("2호선", "중동", 35.1667, 129.1684));
        list.add(new SubwayStation("2호선", "해운대", 35.1636, 129.1586));
        list.add(new SubwayStation("2호선", "동백", 35.1614, 129.1477));
        list.add(new SubwayStation("2호선", "벡스코", 35.1690, 129.1386));
        list.add(new SubwayStation("2호선", "센텀시티", 35.1689, 129.1316));
        list.add(new SubwayStation("2호선", "민락", 35.1670, 129.1215));
        list.add(new SubwayStation("2호선", "수영", 35.1656, 129.1149));
        list.add(new SubwayStation("2호선", "광안", 35.1578, 129.1135));
        list.add(new SubwayStation("2호선", "금련산", 35.1496, 129.1108));
        list.add(new SubwayStation("2호선", "남천", 35.1427, 129.1079));
        list.add(new SubwayStation("2호선", "경성대부경대", 35.1375, 129.1005));
        list.add(new SubwayStation("2호선", "대연", 35.1351, 129.0927));
        list.add(new SubwayStation("2호선", "못골", 35.1347, 129.0844));
        list.add(new SubwayStation("2호선", "지게골", 35.1358, 129.0744));
        list.add(new SubwayStation("2호선", "문현", 35.1390, 129.0670));
        list.add(new SubwayStation("2호선", "국제금융센터부산은행", 35.1458, 129.0667));
        list.add(new SubwayStation("2호선", "전포", 35.1531, 129.0651));
        list.add(new SubwayStation("2호선", "서면", 35.1577, 129.0592));
        list.add(new SubwayStation("2호선", "부암", 35.1573, 129.0504));
        list.add(new SubwayStation("2호선", "가야", 35.1559, 129.0424));
        list.add(new SubwayStation("2호선", "동의대", 35.1540, 129.0320));
        list.add(new SubwayStation("2호선", "개금", 35.1530, 129.0205));
        list.add(new SubwayStation("2호선", "냉정", 35.1511, 129.0122));
        list.add(new SubwayStation("2호선", "주례", 35.1506, 129.0038));
        list.add(new SubwayStation("2호선", "감전", 35.1557, 128.9911));
        list.add(new SubwayStation("2호선", "사상", 35.1625, 128.9847));
        list.add(new SubwayStation("2호선", "덕포", 35.1733, 128.9831));
        list.add(new SubwayStation("2호선", "모덕", 35.1687, 128.9894));
        list.add(new SubwayStation("2호선", "모라", 35.1910, 128.9958));
        list.add(new SubwayStation("2호선", "구남", 35.1969, 129.0085));
        list.add(new SubwayStation("2호선", "구명", 35.2025, 129.0118));
        list.add(new SubwayStation("2호선", "덕천", 35.2104, 129.0050));
        list.add(new SubwayStation("2호선", "수정", 35.2232, 129.0122));
        list.add(new SubwayStation("2호선", "화명", 35.2345, 129.0134));
        list.add(new SubwayStation("2호선", "율리", 35.2454, 129.0125));
        list.add(new SubwayStation("2호선", "동원", 35.2587, 129.0173));
        list.add(new SubwayStation("2호선", "금곡", 35.2679, 129.0176));
        list.add(new SubwayStation("2호선", "호포", 35.2813, 129.0175));
        list.add(new SubwayStation("2호선", "증산", 35.3087, 129.0104));
        list.add(new SubwayStation("2호선", "부산대양산캠퍼스", 35.3176, 129.0144));
        list.add(new SubwayStation("2호선", "남양산", 35.3255, 129.0146));
        list.add(new SubwayStation("2호선", "양산", 35.3388, 129.0265));

        // 부산 3호선
        list.add(new SubwayStation("3호선", "수영", 35.1656, 129.1149));
        list.add(new SubwayStation("3호선", "망미", 35.1713, 129.1069));
        list.add(new SubwayStation("3호선", "배산", 35.1735, 129.0951));
        list.add(new SubwayStation("3호선", "물만골", 35.1767, 129.0857));
        list.add(new SubwayStation("3호선", "연산", 35.1860, 129.0814));
        list.add(new SubwayStation("3호선", "거제", 35.1886, 129.0738));
        list.add(new SubwayStation("3호선", "종합운동장", 35.1914, 129.0675));
        list.add(new SubwayStation("3호선", "사직", 35.1988, 129.0649));
        list.add(new SubwayStation("3호선", "미남", 35.2054, 129.0680));
        list.add(new SubwayStation("3호선", "만덕", 35.2134, 128.9842));
        list.add(new SubwayStation("3호선", "남산정", 35.2130, 128.9882));
        list.add(new SubwayStation("3호선", "숙등", 35.2118, 128.9963));
        list.add(new SubwayStation("3호선", "덕천", 35.2104, 129.0050));
        list.add(new SubwayStation("3호선", "구포", 35.2067, 128.9990));
        list.add(new SubwayStation("3호선", "강서구청", 35.2115, 128.9805));
        list.add(new SubwayStation("3호선", "체육공원", 35.2192, 128.9637));
        list.add(new SubwayStation("3호선", "대저", 35.2138, 128.9506));

        // 부산 4호선
        list.add(new SubwayStation("4호선", "미남", 35.2054, 129.0680));
        list.add(new SubwayStation("4호선", "동래", 35.2055, 129.0785));
        list.add(new SubwayStation("4호선", "수안", 35.2013, 129.0839));
        list.add(new SubwayStation("4호선", "낙민", 35.1948, 129.0917));
        list.add(new SubwayStation("4호선", "충렬사", 35.1921, 129.0975));
        list.add(new SubwayStation("4호선", "명장", 35.1953, 129.1044));
        list.add(new SubwayStation("4호선", "서동", 35.2048, 129.1098));
        list.add(new SubwayStation("4호선", "금사", 35.2158, 129.1151));
        list.add(new SubwayStation("4호선", "반여농산물시장", 35.2172, 129.1243));
        list.add(new SubwayStation("4호선", "석대", 35.2187, 129.1326));
        list.add(new SubwayStation("4호선", "영산대", 35.2250, 129.1456));
        list.add(new SubwayStation("4호선", "윗반송", 35.2299, 129.1532));
        list.add(new SubwayStation("4호선", "고촌", 35.2360, 129.1508));
        list.add(new SubwayStation("4호선", "안평", 35.2462, 129.1523));

        // 동해선
        list.add(new SubwayStation("동해선", "부전", 35.1626, 129.0627));
        list.add(new SubwayStation("동해선", "거제해맞이", 35.1810, 129.0691));
        list.add(new SubwayStation("동해선", "거제", 35.1886, 129.0738));
        list.add(new SubwayStation("동해선", "교대", 35.1959, 129.0803));
        list.add(new SubwayStation("동해선", "동래", 35.2055, 129.0785));
        list.add(new SubwayStation("동해선", "안락", 35.1966, 129.1010));
        list.add(new SubwayStation("동해선", "부산원동", 35.1926, 129.1142));
        list.add(new SubwayStation("동해선", "재송", 35.1882, 129.1205));
        list.add(new SubwayStation("동해선", "센텀", 35.1793, 129.1241));
        list.add(new SubwayStation("동해선", "벡스코", 35.1690, 129.1386));
        list.add(new SubwayStation("동해선", "신해운대", 35.1818, 129.1765));
        list.add(new SubwayStation("동해선", "송정", 35.1812, 129.2027));
        list.add(new SubwayStation("동해선", "오시리아", 35.1962, 129.2155));
        list.add(new SubwayStation("동해선", "기장", 35.2445, 129.2184));
        list.add(new SubwayStation("동해선", "일광", 35.2648, 129.2332));
        list.add(new SubwayStation("동해선", "좌천", 35.3125, 129.2426));
        list.add(new SubwayStation("동해선", "월내", 35.3282, 129.2799));

        // 부산김해경전철
        list.add(new SubwayStation("부산김해경전철", "사상", 35.1625, 128.9847));
        list.add(new SubwayStation("부산김해경전철", "괘법르네시떼", 35.1636, 128.9778));
        list.add(new SubwayStation("부산김해경전철", "서부산유통지구", 35.1663, 128.9615));
        list.add(new SubwayStation("부산김해경전철", "공항", 35.1713, 128.9485));
        list.add(new SubwayStation("부산김해경전철", "덕두", 35.1820, 128.9548));
        list.add(new SubwayStation("부산김해경전철", "등구", 35.1962, 128.9567));
        list.add(new SubwayStation("부산김해경전철", "대저", 35.2138, 128.9506));
        list.add(new SubwayStation("부산김해경전철", "평강", 35.2184, 128.9368));
        list.add(new SubwayStation("부산김해경전철", "대사", 35.2245, 128.9281));
        list.add(new SubwayStation("부산김해경전철", "불암", 35.2315, 128.9205));
        list.add(new SubwayStation("부산김해경전철", "지내", 35.2346, 128.9122));
        list.add(new SubwayStation("부산김해경전철", "김해대학", 35.2436, 128.9057));
        list.add(new SubwayStation("부산김해경전철", "인제대", 35.2494, 128.9026));
        list.add(new SubwayStation("부산김해경전철", "김해시청", 35.2278, 128.8892));
        list.add(new SubwayStation("부산김해경전철", "부원", 35.2264, 128.8817));
        list.add(new SubwayStation("부산김해경전철", "봉황", 35.2271, 128.8746));
        list.add(new SubwayStation("부산김해경전철", "수로왕릉", 35.2286, 128.8665));
        list.add(new SubwayStation("부산김해경전철", "박물관", 35.2350, 128.8715));
        list.add(new SubwayStation("부산김해경전철", "연지공원", 35.2408, 128.8715));
        list.add(new SubwayStation("부산김해경전철", "장신대", 35.2475, 128.8674));
        list.add(new SubwayStation("부산김해경전철", "가야대", 35.2566, 128.8656));

        return list;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value == null) {
            return "";
        }

        return String.valueOf(value).trim();
    }

    private double getDouble(Map<String, Object> map, String key) {
        return parseDouble(getString(map, key));
    }

    private double parseDouble(String value) {
        try {
            if (value == null || value.isBlank() || "-".equals(value)) {
                return 0;
            }

            return Double.parseDouble(value.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private int walkingMinute(double meter) {
        return Math.max(1, (int) Math.ceil(meter / 67.0));
    }

    private double distanceMeter(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLng / 2)
                        * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    private String formatWon(String value) {
        try {
            int number = Integer.parseInt(value);
            return String.format("%,d", number);
        } catch (Exception e) {
            return value;
        }
    }

    private static class SubwayStation {
        String line;
        String name;
        double lat;
        double lng;

        SubwayStation(String line, String name, double lat, double lng) {
            this.line = line;
            this.name = name;
            this.lat = lat;
            this.lng = lng;
        }
    }

    private static class BusStop {
        String name;
        String arsNo;
        double lat;
        double lng;

        BusStop(String name, String arsNo, double lat, double lng) {
            this.name = name;
            this.arsNo = arsNo;
            this.lat = lat;
            this.lng = lng;
        }
    }

    private static class Parking {
        String name;
        String count;
        String tenMin;
        String dayMax;
        String addFee;

        Parking(String name, String count, String tenMin, String dayMax, String addFee) {
            this.name = name;
            this.count = count;
            this.tenMin = tenMin;
            this.dayMax = dayMax;
            this.addFee = addFee;
        }
    }
}