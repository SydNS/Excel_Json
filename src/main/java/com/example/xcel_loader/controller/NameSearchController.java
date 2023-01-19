package com.example.xcel_loader.controller;

import com.example.xcel_loader.service.NameSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin
public class NameSearchController {
    public static final String UPLOAD_BRS_NAMES_BY_FILE = "upload-brs-names";

    @Autowired
    private NameSearchService nameSearchService;

    @PostMapping(value = UPLOAD_BRS_NAMES_BY_FILE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> excelReader(@ModelAttribute("attachmentRequest") BrsNamesUploadRequest attachmentRequest) {
        var resp = nameSearchService.loadBrsNameFromExcel(attachmentRequest);
        return ResponseEntity.status(resp.getStatus()).body(resp);
    }
}
