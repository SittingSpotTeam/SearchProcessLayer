package com.sittingspot.searchprocesslayer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sittingspot.searchprocesslayer.DTO.QueryInDTO;
import com.sittingspot.searchprocesslayer.models.Area;
import com.sittingspot.searchprocesslayer.models.QueryResult;
import com.sittingspot.searchprocesslayer.models.Tag;
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

@RestController("/api/v1")
public class SearchProcessLayerController {
    
    @Value("${sittingspot.querydl.url}")
    private String querydUrl;
    
    @Value("${sittingspot.querydl.api.version}")
    private String querydlApiVersion;
    
    @Value("${sittingspot.searchlogic.url}")
    private String searchLogicUrl;
    
    @Value("${sittingspot.searchlogic.api.version}")
    private String searchLogicApiVersion;

    @GetMapping("/")
    public List<QueryResult> search(@RequestParam("location") Area location,
                                    @RequestParam(value = "tags",required = false) List<Tag> tags,
                                    @RequestParam(value = "labels",required = false) List<String> labels) throws IOException, InterruptedException {
        
        var client = HttpClient.newHttpClient();
        
        var searchRequest = HttpRequest.newBuilder().uri(URI.create("http://" + searchLogicUrl + searchLogicApiVersion + "/?location=" + location + "&tags=" + tags + "&labels=" + labels)).build();
        var searchResult = client.send(searchRequest, HttpResponse.BodyHandlers.ofString());
        
        if (searchResult.statusCode() != 200) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }
        
        List<QueryResult> data = new ObjectMapper().readerForListOf(QueryResult.class).readValue(searchResult.body());
        
        var queryPostRequest = HttpRequest.newBuilder()
                                       .uri(URI.create("http://" + querydUrl + querydlApiVersion + "/"))
                                       .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(new QueryInDTO(location,tags,labels,data))))
                                       .build();
        
        var queryResult = client.send(queryPostRequest, HttpResponse.BodyHandlers.ofString());
        
        return data;
    }
}
