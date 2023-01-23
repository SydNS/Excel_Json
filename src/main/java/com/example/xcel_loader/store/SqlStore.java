package com.example.xcel_loader.store;

import com.example.xcel_loader.exception.NameSearchException;
import com.example.xcel_loader.model.NameSearch;
import com.example.xcel_loader.repository.NameSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.xcel_loader.model.Constants.STATUS_REGISTERED;


@Component
public class SqlStore implements Store {

    @Autowired
    private NameSearchRepository nameSearchRepository;

    @Override
    public void clear() {
//        nameSearchRepository.deleteAll();
    }

    @Override
    public UUID saveNewName(NameSearch name) {

        if (name.getId() == null) {
            name.setId(UUID.randomUUID());
        }

        Optional<NameSearch> checkName = nameSearchRepository.findById(name.getId());

        if (checkName.isPresent()) {

            checkName.map(updateName -> {
                updateName.setName(name.getName());
                updateName.setNo(name.getNo());
                updateName.setStatus(name.getStatus());
                updateName.setType(name.getType());
                updateName.setSubType(name.getSubType());
                return nameSearchRepository.saveAndFlush(updateName);
            });


            return checkName.get().getId();
        } else {
            var results = nameSearchRepository.saveAndFlush(name);

//            saveInElasticSearch(results);
        }

        return name.getId();
    }


    @Override
    public NameSearch findById(UUID id) {
        var checkById = nameSearchRepository.findById(id);
        return checkById.get();
    }

    @Override
    public List<NameSearch> findAll() {
        return nameSearchRepository.findAll();
    }

    @Override
    public Optional<NameSearch> findByNoOrId(String no, UUID id) {
        return nameSearchRepository.findByNoOrIdOrderByCreatedAtDesc(no, id);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public NameSearch updateToRegistered(String reservationNo, String registrationNo) {

        var registeredAt = new Date();

        var nameSearch = nameSearchRepository.findByNoOrderByCreatedAtDesc(reservationNo);

        nameSearch.map(updateNameSearch -> {
            updateNameSearch.setNo(registrationNo);
            updateNameSearch.setCreatedAt(registeredAt);
            updateNameSearch.setStatus(STATUS_REGISTERED);
            return nameSearchRepository.saveAndFlush(updateNameSearch);
        });


        if (nameSearch.isEmpty()) {
            throw new NameSearchException("Failed getting updated entity");
        }


        return nameSearch.get();
    }

    @Override
    public boolean existsByNo(String name) {
        return nameSearchRepository.existsByNo(name);
    }

    @Override
    public void removeName(UUID searchId) {

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public NameSearch updateStatus(String no, String toStatus) {
        var nameByNo = nameSearchRepository.findByNoOrderByCreatedAtDesc(no);

        if (nameByNo.isEmpty()) {
            throw new NameSearchException("Invalid number: " + no);
        }

        var oldStatus = nameByNo.get().getStatus();

        if (oldStatus.equals(toStatus)) {
            throw new NameSearchException("This entity already has status: " + toStatus);
        }

        nameByNo.map(newStatus -> {
            newStatus.setStatus(toStatus);
            return nameSearchRepository.saveAndFlush(newStatus);
        });

        return nameByNo.get();
    }


}
