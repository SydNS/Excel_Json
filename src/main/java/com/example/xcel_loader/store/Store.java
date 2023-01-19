package ug.go.ursb.namesearch.store;

import org.springframework.data.jpa.domain.Specification;
import ug.go.ursb.namesearch.api.responses.AnalysisReport;
import ug.go.ursb.namesearch.api.responses.NameScore;
import ug.go.ursb.namesearch.api.responses.NameSearchWrapper;
import ug.go.ursb.namesearch.api.responses.SearchFilter;
import ug.go.ursb.namesearch.models.ElasticSearchName;
import ug.go.ursb.namesearch.models.NameSearch;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Store {

    void clear();

    UUID saveNewName(NameSearch name);

    NameSearchWrapper updateStatus(UUID wrapper);

    List<NameSearchWrapper> names(String name, String type, String status, Date from, Date to, int maxResults, String scope);

    long count();

    boolean existsByName(String name);

    List<ElasticSearchName> elasticSearchByName(String nameText, int maxResults, String scope);

    UUID saveInElasticSearch(NameSearch nameSearch);

    NameSearch findById(UUID id);

    SearchFilter searchFilters();

    boolean deleteIndex(String indexName);

    NameScore compareWords(String word1, String word2);

    AnalysisReport analysisReport(String searchName);

    NameSearch updateStatus(String no, String toStatus);

    NameSearch updateToRegistered(String reservationNo, String registrationNo);

    Optional<NameSearch> findByNoOrId(String no, UUID id);

    long countElastic();

    List<ElasticSearchName> elasticNames();

    void removeName(UUID searchId);

    List<NameSearch> findByExactMatch(String exactMatch);

    List<NameSearch> findWhereContainsThis(String searchPhrase, int maxResults);

    List<NameSearch> searchBySpecifications(Specification<NameSearch> specs, int maxResults);
}
