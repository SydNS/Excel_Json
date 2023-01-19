package ug.go.ursb.namesearch.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ug.go.ursb.namesearch.api.requests.AnalysisReportRequest;
import ug.go.ursb.namesearch.api.requests.BrsNamesUploadRequest;
import ug.go.ursb.namesearch.api.requests.NameSearchRequest;
import ug.go.ursb.namesearch.api.responses.CustomApiResponse;
import ug.go.ursb.namesearch.api.responses.NameSearchWrapper;
import ug.go.ursb.namesearch.api.responses.NamesCount;
import ug.go.ursb.namesearch.api.responses.SearchFilter;
import ug.go.ursb.namesearch.models.ElasticSearchName;
import ug.go.ursb.namesearch.services.NameSearchService;

import javax.validation.Valid;
import java.net.URI;
import java.util.Date;
import java.util.List;

import static ug.go.ursb.namesearch.helpers.Constants.DATE_FORMAT;

@RestController
@CrossOrigin
public class NameSearchController {
    public static final String UPLOAD_BRS_NAMES_BY_FILE = "upload-brs-names";



    @GetMapping(DIRECT_SEARCH)
    public ResponseEntity<List<ElasticSearchName>> directSearch(@RequestParam String name, @RequestParam(required = false, defaultValue = "5") int maxResults){
        return ResponseEntity.ok(nameSearchService.directSearch(name, maxResults));
    }

    @PostMapping(value = UPLOAD_BRS_NAMES_BY_FILE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> excelReader(@ModelAttribute("attachmentRequest") BrsNamesUploadRequest attachmentRequest) {
        var resp = nameSearchService.loadBrsNameFromExcel(attachmentRequest);
        return ResponseEntity.status(resp.getStatus()).body(resp);
    }
}
