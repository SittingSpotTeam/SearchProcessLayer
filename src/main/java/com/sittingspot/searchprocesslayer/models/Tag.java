package com.sittingspot.searchprocesslayer.models;

import jakarta.persistence.Embeddable;

@Embeddable
public record Tag(String key, String value) {
}
