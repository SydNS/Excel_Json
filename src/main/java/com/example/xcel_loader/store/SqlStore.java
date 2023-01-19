package ug.go.ursb.namesearch.store;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ug.go.ursb.namesearch.api.responses.AnalysisReport;
import ug.go.ursb.namesearch.api.responses.NameScore;
import ug.go.ursb.namesearch.api.responses.NameSearchWrapper;
import ug.go.ursb.namesearch.api.responses.SearchFilter;
import ug.go.ursb.namesearch.exceptions.NameSearchException;
import ug.go.ursb.namesearch.helpers.Utils;
import ug.go.ursb.namesearch.helpers.analyze.JaroWinkler;
import ug.go.ursb.namesearch.helpers.analyze.Metaphone;
import ug.go.ursb.namesearch.models.DictionaryWord;
import ug.go.ursb.namesearch.models.ElasticSearchName;
import ug.go.ursb.namesearch.models.NameSearch;
import ug.go.ursb.namesearch.models.NameSearchSpecifications;
import ug.go.ursb.namesearch.repositories.MyElasticSearchRepository;
import ug.go.ursb.namesearch.repositories.NameSearchRepository;

import java.util.*;

import static ug.go.ursb.namesearch.helpers.CompanyNameTypes.getStatus;
import static ug.go.ursb.namesearch.helpers.CompanyNameTypes.getTypes;
import static ug.go.ursb.namesearch.helpers.Constants.*;

@Component
public class MySQLNameSearchStore implements NameSearchStore{

    @Autowired
    private NameSearchRepository nameSearchRepository;

    @Autowired
    private MyElasticSearchRepository myElasticSearchRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private DictionaryStore dictionaryStore;

    @Override
    public void clear() {
        dictionaryStore.clear();
        nameSearchRepository.deleteAll();
        myElasticSearchRepository.deleteAll();
    }

    @Override
    public UUID saveNewName(NameSearch name) {

        if(name.getId() == null){
            name.setId(UUID.randomUUID());
        }

        Optional<NameSearch> checkName = nameSearchRepository.findById(name.getId());

        if(checkName.isPresent()){

            checkName.map(updateName->{
                updateName.setName(name.getName());
                updateName.setNo(name.getNo());
                updateName.setStatus(name.getStatus());
                updateName.setType(name.getType());
                updateName.setSubType(name.getSubType());
               return nameSearchRepository.saveAndFlush(updateName);
            });

            saveInElasticSearch(name);

            return checkName.get().getId();
        } else {
            var results = nameSearchRepository.saveAndFlush(name);

            saveInElasticSearch(results);
        }

        return name.getId();
    }

    @Override
    public NameSearchWrapper updateStatus(UUID wrapper) {
        Optional<NameSearch> checkName = nameSearchRepository.findById(wrapper);

        if(checkName.isEmpty()){
            throw new NameSearchException(Utils.translate("name_not_found"));
        }

        return NameSearchWrapper.nameSearchWrapper(checkName.get());
    }

    @Override
    public List<NameSearchWrapper> names(String name, String type, String status, Date from, Date to, int maxResults, String scope) {

        List<ElasticSearchName> searchResults = elasticSearchByName(name, maxResults, scope);

        List<NameSearchWrapper> nameSearchWrappers = new ArrayList<>();

        searchResults.forEach(nameSearch -> nameSearchWrappers.add(NameSearchWrapper.nameSearchWrapper(nameSearch,compareWords(nameSearch.getName(),name))));

        return Utils.sortByText(nameSearchWrappers);
    }

    @Override
    public long count() {
        return nameSearchRepository.count();
    }

    @Override
    public boolean existsByName(String name) {
        return nameSearchRepository.existsByName(name);
    }

    @Override
    public List<ElasticSearchName> elasticSearchByName(String term, int maxResults, String scope) {

        String name = term.trim().toUpperCase();

        List<ElasticSearchName> searchResults = new ArrayList<>();

        QueryStringQueryBuilder stringQuery;

        if(SEARCH_SCOPE_TEXT.equals(scope)){
            stringQuery = QueryBuilders.queryStringQuery(name).field("name").field("no").field("nGramName").analyzeWildcard(true);
        } else if(SEARCH_SCOPE_SOUND.equals(scope)){
            stringQuery = QueryBuilders.queryStringQuery(name).field("nameMetaphone").field("nameDoubleMetaphone");
        } else {
            stringQuery = QueryBuilders.queryStringQuery(name).field("nameMetaphone").field("nameDoubleMetaphone").field("name").field("no").field("nGramName").analyzeWildcard(true);
        }

        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(stringQuery)
                .withMaxResults(maxResults)
                .build();

        SearchHits<ElasticSearchName> results = elasticsearchOperations.search(searchQuery, ElasticSearchName.class, IndexCoordinates.of(ElasticSearchName.NAME_SEARCH_INDEX));

        results.forEach(hint->{
            ElasticSearchName obje = hint.getContent();
            obje.setScore(hint.getScore());
            searchResults.add(obje);
        });

        return searchResults;
    }

    @Override
    public UUID saveInElasticSearch(NameSearch nameSearch) {

        var elasticNameSearch = ElasticSearchName.elasticSearchName(nameSearch);

        var checkName = myElasticSearchRepository.findById(nameSearch.getId());

        if(checkName.isPresent()){

            checkName.map(newUpdate->{
                newUpdate.setName(nameSearch.getName());
                newUpdate.setStatus(nameSearch.getStatus());
                newUpdate.setNo(nameSearch.getNo());
                newUpdate.setType(nameSearch.getType());
                newUpdate.setSubType(nameSearch.getSubType());
               return myElasticSearchRepository.save(newUpdate);
            });

            return checkName.get().getId();
        }

        System.out.println("Adding: "+elasticNameSearch.getName()+" vs "+elasticNameSearch.getNameFull()+" "+elasticNameSearch.getStatus()+" "+elasticNameSearch.getNo());

        ElasticSearchName res = myElasticSearchRepository.save(elasticNameSearch);
        return res.getId();
    }

    @Override
    public NameSearch findById(UUID id) {
        var checkById = nameSearchRepository.findById(id);
        var checkById2 = myElasticSearchRepository.findById(id);

        if(checkById.isEmpty() && checkById2.isEmpty()){
            throw new NameSearchException(Utils.translate("name_not_found"));
        }

        return checkById.get();
    }

    @Override
    public SearchFilter searchFilters() {

        SearchFilter searchFilter = new SearchFilter();

        searchFilter.setTypes(getTypes());
        searchFilter.setStatuses(getStatus());

        return searchFilter;
    }

    @Override
    public boolean deleteIndex(String indexName) {
        return true;
    }

    @Override
    public NameScore compareWords(String word1, String word2) {
        word1 = word1.trim().toUpperCase();
        word2 = word2.trim().toUpperCase();

        NameScore nameScore = new NameScore();
        nameScore.setSound(Metaphone.compare(word1,word2));
        nameScore.setText(JaroWinkler.comparePct(word1,word2));
        return nameScore;
    }

    @Override
    public AnalysisReport analysisReport(String searchName) {

        List<NameSearchWrapper> textResults = names(searchName,"","",null,null,5,SEARCH_SCOPE_TEXT);

        List<NameSearchWrapper> soundResults = names(searchName,"","",null,null,5,SEARCH_SCOPE_SOUND);

        List<NameSearchWrapper> sound = Utils.sortBySound(soundResults);

        List<NameSearchWrapper> text = Utils.sortByText(textResults);

        NameScore namescore = new NameScore();

        if(sound.size() > 0){
            namescore.setSound(sound.get(0).getScore().getSound());
        }

        if(text.size() > 0){
            namescore.setText(text.get(0).getScore().getText());
        }

        var dictionaryWords  = dictionaryStore.words(null);

        var flagged = new ArrayList<DictionaryWord>();

        dictionaryWords.forEach(dictionaryWord -> {

            var name = searchName.toUpperCase();
            var flag = dictionaryWord.getWord().toUpperCase();
            var position = dictionaryWord.getPosition();

            if(position.equalsIgnoreCase(STATUS_FLAGGED_POSITION_ANY)){
                if(name.contains(flag)){
                    flagged.add(dictionaryWord);
                }
            } else if(position.equalsIgnoreCase(STATUS_FLAGGED_POSITION_START)){
                if(name.startsWith(flag)){
                    flagged.add(dictionaryWord);
                }
            } else if(position.equalsIgnoreCase(STATUS_FLAGGED_POSITION_END)){
                if(name.endsWith(flag)){
                    flagged.add(dictionaryWord);
                }
            }

        });

        AnalysisReport analysisReport = new AnalysisReport();
        analysisReport.setFlags(flagged);
        analysisReport.setName(searchName);
        analysisReport.setSound(sound);
        analysisReport.setText(text);
        analysisReport.setNameScore(namescore);

        return analysisReport;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NameSearch updateStatus(String no, String toStatus) {
        var nameByNo = nameSearchRepository.findByNoOrderByCreatedAtDesc(no);

        if(nameByNo.isEmpty()){
            throw new NameSearchException("Invalid number: "+no);
        }

        var oldStatus = nameByNo.get().getStatus();

        if(oldStatus.equals(toStatus)){
            throw new NameSearchException("This entity already has status: "+toStatus);
        }

        nameByNo.map(newStatus->{
            newStatus.setStatus(toStatus);
            return nameSearchRepository.saveAndFlush(newStatus);
        });

        var elasticSearch = myElasticSearchRepository.findById(nameByNo.get().getId());

        if(elasticSearch.isEmpty()){
            throw new NameSearchException("This entity can not be searched");
        }

        elasticSearch.map(newStatus->{
            newStatus.setStatus(toStatus);
           return myElasticSearchRepository.save(newStatus);
        });

        var nameSearch = elasticSearch.get();

        if(!nameSearch.getStatus().equals(nameByNo.get().getStatus())){
           throw new NameSearchException("Changing status failed, try again");
        }

        return nameByNo.get();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NameSearch updateToRegistered(String reservationNo, String registrationNo) {

        var registeredAt = new Date();

        var nameSearch = nameSearchRepository.findByNoOrderByCreatedAtDesc(reservationNo);

        nameSearch.map(updateNameSearch->{
            updateNameSearch.setNo(registrationNo);
            updateNameSearch.setCreatedAt(registeredAt);
            updateNameSearch.setStatus(STATUS_REGISTERED);
            return nameSearchRepository.saveAndFlush(updateNameSearch);
        });

        var elasticSearchName = myElasticSearchRepository.findByNo(reservationNo);

        elasticSearchName.map(updateElasticSearchName->{
            updateElasticSearchName.setNo(registrationNo);
            updateElasticSearchName.setCreatedAt(registeredAt);
            updateElasticSearchName.setStatus(STATUS_REGISTERED);
            return myElasticSearchRepository.save(updateElasticSearchName);
        });

        if(elasticSearchName.isEmpty()){
            throw new NameSearchException("Failed to update index entity, try again.");
        }

        if(nameSearch.isEmpty()){
            throw new NameSearchException("Failed getting updated entity");
        }

        if(!nameSearch.get().getStatus().equals(elasticSearchName.get().getStatus())){
            throw new NameSearchException("Failed getting updated entity: "+nameSearch.get().getStatus()+" vs "+elasticSearchName.get().getStatus());
        }

        return nameSearch.get();
    }

    @Override
    public Optional<NameSearch> findByNoOrId(String no, UUID id) {
        return nameSearchRepository.findByNoOrIdOrderByCreatedAtDesc(no,id);
    }

    @Override
    public long countElastic() {
        return myElasticSearchRepository.count();
    }

    @Override
    public List<ElasticSearchName> elasticNames() {
        List<ElasticSearchName> elasticSearchNames = new ArrayList<>();

        myElasticSearchRepository.findAll().forEach(elasticSearchNames::add);

        return elasticSearchNames;
    }

    @Override
    public void removeName(UUID searchId) {
        myElasticSearchRepository.deleteById(searchId);
        nameSearchRepository.deleteById(searchId);
    }

    @Override
    public List<NameSearch> findByExactMatch(String exactMatch) {
        return nameSearchRepository.findByName(exactMatch);
    }

    @Override
    public List<NameSearch> findWhereContainsThis(String searchPhrase, int maxResults) {
        var specs = NameSearchSpecifications.nameSearchSpecificationStart()
                .and(NameSearchSpecifications.nameSearchByName(searchPhrase));

        specs = specs.or(NameSearchSpecifications.nameSearchByNameContains(searchPhrase));

        specs = specs.or(NameSearchSpecifications.nameSearchByName(Utils.reverseWords(searchPhrase)));

        specs = specs.or(NameSearchSpecifications.nameSearchByNameContains(Utils.reverseWords(searchPhrase)));

        var splits = searchPhrase.split(" ");

        for (String split : splits) {
            specs = specs.or(NameSearchSpecifications.nameSearchByNameContains(split));
        }

        return nameSearchRepository.findAll(specs, Pageable.ofSize(maxResults)).getContent();
    }


}
