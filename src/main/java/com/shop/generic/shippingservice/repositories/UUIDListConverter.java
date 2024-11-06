package com.shop.generic.shippingservice.repositories;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Converter
public class UUIDListConverter implements AttributeConverter<List<UUID>, String> {

    // Convert List<UUID> to a comma-separated String to store in the database
    @Override
    public String convertToDatabaseColumn(final List<UUID> uuidList) {
        if (uuidList == null || uuidList.isEmpty()) {
            return null; // Store null or empty string if the list is null/empty
        }
        // Join the UUIDs as a comma-separated string
        return uuidList.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));
    }

    // Convert a comma-separated String from the database back to a List<UUID>
    @Override
    public List<UUID> convertToEntityAttribute(final String uuidString) {
        if (uuidString == null || uuidString.isEmpty()) {
            return null; // Return null if the string is empty or null
        }
        // Split the string by commas and convert each to a UUID
        return Arrays.stream(uuidString.split(","))
                .map(String::trim)
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }
}
