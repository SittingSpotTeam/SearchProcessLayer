package com.sittingspot.searchprocesslayer.models;

import java.util.UUID;

public record QueryResult(UUID spotId, Location location) {
}
