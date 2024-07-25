package com.sittingspot.searchprocesslayer.DTO;

import com.sittingspot.searchprocesslayer.models.Area;
import com.sittingspot.searchprocesslayer.models.QueryResult;
import com.sittingspot.searchprocesslayer.models.Tag;

import java.io.Serializable;
import java.util.List;

public record QueryInDTO(Area area, List<Tag> tags, List<String> labels, List<QueryResult> results) implements Serializable {
}