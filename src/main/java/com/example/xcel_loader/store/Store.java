package com.example.xcel_loader.store;

import com.example.xcel_loader.model.NameSearch;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Store {

    void clear();

    UUID saveNewName(NameSearch name);

    NameSearch findById(UUID id);

    List<NameSearch> findAll();

    Optional<NameSearch> findByNoOrId(String no, UUID id);

    NameSearch updateToRegistered(String reservationNo, String registrationNo);

    boolean existsByName(String name);


    void removeName(UUID searchId);

    NameSearch updateStatus(String no, String toStatus);

}
