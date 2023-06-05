package com.example.aquafarm.Weather.Service;

import com.example.aquafarm.Aquafarm.Domain.AquafarmInfo;
import com.example.aquafarm.Weather.DTO.GeolocationDTO;
import com.example.aquafarm.Weather.DTO.RiseSetInfoDTO;
import com.example.aquafarm.Weather.Domain.WeatherInfo;
import com.example.aquafarm.Weather.Repository.WeatherInfoRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class RiseSetInfoService {

    private final WeatherInfoRepository weatherInfoRepository;
    private final GeocodingService geocodingService;
    public RiseSetInfoService(WeatherInfoRepository weatherInfoRepository, GeocodingService geocodingService) {
        this.weatherInfoRepository = weatherInfoRepository;
        this.geocodingService = geocodingService;
    }
    public RiseSetInfoDTO getRiseSetInfo(String locdate, int weatherId) throws IOException {

        WeatherInfo weatherInfo = weatherInfoRepository.findById(weatherId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid weather ID"));

        // 주소를 위도와 경도로 변환
        GeolocationDTO location = geocodingService.getGeolocationFromAddress(weatherInfo.getAddress());
        if (location == null) {
            throw new IllegalStateException("Failed to convert address to geolocation");
        }

        String latitude = String.valueOf(location.getLat());
        String longitude = String.valueOf(location.getLng());

        // 주소, 위도, 경도 출력
        System.out.println("주소: " + weatherInfo.getAddress());
        System.out.println("위도: " + location.getLat());
        System.out.println("경도: " + location.getLng());

        // 기본값 설정
        if (latitude == null) {
            latitude = "33";
        }
        if (longitude == null) {
            longitude = "126.5";
        }


        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B090041/openapi/service/RiseSetInfoService/getLCRiseSetInfo"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=9tfrUPQ2c78KRdrN7%2F3cCiHrqC8Oe%2FoB3GhGUeiYM2FFoJDrV9%2FR3KG9rBVgPXEQAXdSuQPQgeRAhzjFbF0RRA%3D%3D"); /*서비스 키*/
        urlBuilder.append("&" + URLEncoder.encode("locdate", "UTF-8") + "=" + URLEncoder.encode(locdate, "UTF-8")); /*날짜*/
        urlBuilder.append("&" + URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(longitude, "UTF-8")); /*지역*/
        urlBuilder.append("&" + URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode(latitude, "UTF-8")); /*지역*/
        urlBuilder.append("&" + URLEncoder.encode("dnYn", "UTF-8") + "=" + URLEncoder.encode("Y", "UTF-8"));

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("응답 코드: " + conn.getResponseCode());

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            // 응답 데이터를 DTO에 매핑
            RiseSetInfoDTO riseSetInfoDTO = parseResponse(sb.toString());

            return riseSetInfoDTO;
        }
    }

    private RiseSetInfoDTO parseResponse(String response) {
        RiseSetInfoDTO riseSetInfoDTO = new RiseSetInfoDTO();

        // XML 파싱 및 응답 데이터를 DTO에 매핑하는 로직 작성
        // 아래는 예시로 response 문자열을 XML 파싱하여 값을 추출하여 DTO에 매핑하는 부분입니다.
        // 실제로는 XML 파서를 사용하여 응답 데이터를 파싱하고 값을 매핑해야 합니다.
        riseSetInfoDTO.setResultCode(getTagValue(response, "resultCode"));
        riseSetInfoDTO.setResultMsg(getTagValue(response, "resultMsg"));
        riseSetInfoDTO.setAste(getTagValue(response, "aste"));
        riseSetInfoDTO.setAstm(getTagValue(response, "astm"));
        riseSetInfoDTO.setCivile(getTagValue(response, "civile"));
        riseSetInfoDTO.setCivilm(getTagValue(response, "civilm"));
        riseSetInfoDTO.setLatitude(getTagValue(response, "latitude"));
        riseSetInfoDTO.setLatitudeNum(getTagValue(response, "latitudeNum"));
        riseSetInfoDTO.setLocation(getTagValue(response, "location"));
        riseSetInfoDTO.setLocdate(getTagValue(response, "locdate"));
        riseSetInfoDTO.setLongitude(getTagValue(response, "longitude"));
        riseSetInfoDTO.setLongitudeNum(getTagValue(response, "longitudeNum"));
        riseSetInfoDTO.setMoonrise(getTagValue(response, "moonrise"));
        riseSetInfoDTO.setMoonset(getTagValue(response, "moonset"));
        riseSetInfoDTO.setMoontransit(getTagValue(response, "moontransit"));
        riseSetInfoDTO.setNaute(getTagValue(response, "naute"));
        riseSetInfoDTO.setNautm(getTagValue(response, "nautm"));
        riseSetInfoDTO.setSunrise(getTagValue(response, "sunrise"));
        riseSetInfoDTO.setSunset(getTagValue(response, "sunset"));
        riseSetInfoDTO.setSuntransit(getTagValue(response, "suntransit"));

        return riseSetInfoDTO;
    }

    private String getTagValue(String response, String tagName) {
        int startIndex = response.indexOf("<" + tagName + ">");
        int endIndex = response.indexOf("</" + tagName + ">");
        if (startIndex != -1 && endIndex != -1) {
            return response.substring(startIndex + tagName.length() + 2, endIndex).trim();
        }
        return null;
    }
}