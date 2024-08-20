package com.sittingspot.searchprocesslayer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sittingspot.searchprocesslayer.DTO.QueryInDTO;
import com.sittingspot.searchprocesslayer.models.Area;
import com.sittingspot.searchprocesslayer.models.Location;
import com.sittingspot.searchprocesslayer.models.QueryResult;
import com.sittingspot.searchprocesslayer.models.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestController("/api/v1")
public class SearchProcessLayerController {
    
    @Value("${sittingspot.querydl.url}")
    private String querydUrl;
    
    @Value("${sittingspot.searchlogic.url}")
    private String searchLogicUrl;

    @GetMapping("/")
    public List<QueryResult> search(@RequestParam("x") Double x,
                                    @RequestParam("y") Double y,
                                    @RequestParam("area") Double area,
                                    @RequestParam(value = "tags",required = false) List<Tag> tags,
                                    @RequestParam(value = "labels",required = false) List<String> labels) throws IOException, InterruptedException {
        
        var client = HttpClient.newHttpClient();
        var location = new Area(new Location(x,y),area);
        // forward the request to the logic layer
        var searchRequestUrl = "http://" + searchLogicUrl  + "?x="+x+"&y="+y+"&area="+area;
        if(tags != null){
            searchRequestUrl += "&tags="+tags;
        }
        if(labels != null){
            searchRequestUrl += "&labels="+labels;
        }
        log.info("Sending request: " + searchRequestUrl);
        var searchRequest = HttpRequest.newBuilder().uri(URI.create(searchRequestUrl)).build();
        var searchResult = client.send(searchRequest, HttpResponse.BodyHandlers.ofString());

        log.info("Got response code " + searchResult.statusCode());
        if (searchResult.statusCode() != 200) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }
        
        List<QueryResult> data = new ObjectMapper().readerForListOf(QueryResult.class).readValue(searchResult.body());
        log.info("Sending request: "+"http://" + querydUrl);
        // once the result comes back update the query dl with the query just completed
        var queryPostRequest = HttpRequest.newBuilder()
                                       .uri(URI.create("http://" + querydUrl))
                                       .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(new QueryInDTO(location,tags,labels,data))))
                                       .build();
        
        var queryResult = client.send(queryPostRequest, HttpResponse.BodyHandlers.ofString());
        log.info("Got response code " + queryResult.statusCode());
        return data;
    }
}
