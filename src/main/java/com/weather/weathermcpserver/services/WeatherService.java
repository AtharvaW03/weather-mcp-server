package com.weather.weathermcpserver.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class WeatherService {
    private final RestClient restClient;


    public WeatherService(){
        this.restClient = RestClient.builder()
                .baseUrl("https://api.weather.gov")
                .defaultHeader("Accept", "application/geo_+json")
                .defaultHeader("User-Agent", "WeatherApiClient/1.0 (your@email.com")
                .build();
    }

    /*
    @JsonProperty is used to define a different name for a property in
    the JSON data than the name of field in java class
     */

    /*
    @JsonProperty(name), tells Jackson ObjectMapper to map
    the JSON property name to the annotated Java field's name.
     */

    /*
    @JsonIgnoreProperties is used to ignore specific fields
    or unknown properties during JSON serialization and
    deserialization in Java
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Points(@JsonProperty("properties") Props properties){
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Props(@JsonProperty("forecast") String forecast){

        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Forecast(@JsonProperty("properties") Props properties){
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Props(@JsonProperty("periods") List<Period> periods){

        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Period(@JsonProperty("number") Integer number, @JsonProperty("name") String name,
                         @JsonProperty("startTime") String startTime, @JsonProperty("endTime") String endTime,
                         /* using Boolean instead of boolean here.
                          Boolean is object, boolean is primitive
                          Boolean can be true, false or null
                           */
                         @JsonProperty("isDayTime") Boolean isDayTime, @JsonProperty("temperature") Integer temperature,
                         @JsonProperty("temperatureUnit") String temperatureUnit,
                         @JsonProperty("temperatureTrend") String temperatureTrend,
                         @JsonProperty("probabilityOfPrecipitation")Map probabilityOfPrecipitation,
                         @JsonProperty("windSpeed") String windSpeed, @JsonProperty("windDirection") String windDirection,
                         @JsonProperty("icon") String icon, @JsonProperty("shortForecast") String shortForecast,
                         @JsonProperty("detailedForecast") String detailedForecast){

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Alert(@JsonProperty("features") List<Feature> features){
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Feature(@JsonProperty("properties")Properties properties){

        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Properties(@JsonProperty("event") String event, @JsonProperty("areaDesc") String areaDesc,
                                 @JsonProperty("severity") String severity, @JsonProperty("description") String description,
                                 @JsonProperty("instruction") String instruction){

        }
    }

    /**
     * Get forecast for a specific latitude/longitude
     * @param latitude Latitude
     * @param longitude Longitude
     * @return the forecast for the given location
     * @throws RestClientException if the request fails
     */
    @Tool(description = "Get weather forecast details for a specific latitude/longitude")
    public String getWeatherForecastByLocation(double latitude, double longitude){
        var points = restClient.get()
                .uri("/points/{latitude},{longitude}", latitude, longitude)
                .retrieve()
                .body(Points.class);

        var forecast = restClient.get().uri(points.properties().forecast()).retrieve().body(Forecast.class);

        String forecastText = forecast.properties.periods().stream().map(p -> {
            return String.format("""
                    %s:
                    Temperature: %s %s
                    Wind: %s %s
                    Forecast: %s
                    """, p.name(), p.temperature(), p.temperatureUnit(), p.windSpeed(), p.windDirection(),
                    p.detailedForecast());
        }).collect(Collectors.joining());

        return forecastText;
    }

    /**
     * Get alerts for a specific area
     * @param state area code. Two-letter US state code (e.g. NY, CA)
     * @param Human readable alert information
     * @throws RestClientException if the request fails
     */
    @Tool(description = "Get weather data for a US state. Input is Two-letter US state code (e.g. NY, CA")
    public String getAlerts(@ToolParam(description = "Two-letter US state code (e.g. NY, CA") String state){
        Alert alert = restClient.get().uri("/alerts/active/area/{state}").retrieve().body(Alert.class);

        return alert.features()
                .stream()
                .map(f -> String.format("""
                        Event: %s
                        Area: %s
                        Severity: %s
                        Description: %s
                        Instructions: %s
                        """, f.properties().event(), f.properties().areaDesc(), f.properties().severity(),
                        f.properties().description(), f.properties().instruction()))
                .collect(Collectors.joining("\n"));
    }

    public static void main(String[] args){
        WeatherService client = new WeatherService();
        System.out.println(client.getWeatherForecastByLocation(12.910283495293427, 77.68279775127904));
        System.out.println(client.getAlerts("NY"));
    }


}
