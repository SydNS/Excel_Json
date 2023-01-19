package com.example.xcel_loader.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BrnLists {
    private List<UUID> newNames;
    private List<NameSearch> duplicateNames;
}
