package com.example.xcel_loader.uploadnames;


import com.example.xcel_loader.controller.CustomApiResponse;
import com.example.xcel_loader.controller.NameSearchController;
import com.example.xcel_loader.utils.ResourceTest;
import com.example.xcel_loader.utils.TestHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.net.HttpURLConnection;

import static com.jayway.restassured.RestAssured.given;


public class UploadBrsNameTest extends ResourceTest {


    @Test
    public void when_a_name_search_request_is_received_it_is_successful() {


        var attachmentResp = given().log()
                .everything()
                .multiPart("file", TestHelper.getXlsFile())
//                .multiPart("file", TestHelper.getSampleFile() , "application/vnd.ms-excel")
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .when()
                .post(NameSearchController.UPLOAD_BRS_NAMES_BY_FILE);
        attachmentResp.prettyPrint();


    }


    @Test
    public void when_a_name_search_request_is_received_and_excel_already_save_brns_it_is_successful() throws JsonProcessingException {


        var objectMapper = new ObjectMapper();
        var attachmentResp = given().log()
                .everything()
                .multiPart("file", TestHelper.getXlsFile())
//                .multiPart("file", TestHelper.getSampleFile() , "application/vnd.ms-excel")
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .when()
                .post(NameSearchController.UPLOAD_BRS_NAMES_BY_FILE)
                .prettyPrint();


        CustomApiResponse customApiResponse = objectMapper.readValue(attachmentResp, CustomApiResponse.class);
        var originals=customApiResponse.getData();

        var duplicate = given().log()
                .everything()
                .multiPart("file", TestHelper.getXlsFile())
//                .multiPart("file", TestHelper.getSampleFile() , "application/vnd.ms-excel")
                .expect()
                .statusCode(HttpURLConnection.HTTP_CONFLICT)
                .when()
                .post(NameSearchController.UPLOAD_BRS_NAMES_BY_FILE)
                .prettyPrint();

        CustomApiResponse customApiResponse2 = objectMapper.readValue(duplicate, CustomApiResponse.class);
        var names=customApiResponse.getData();



    }

}
