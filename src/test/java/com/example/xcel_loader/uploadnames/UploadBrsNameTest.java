package com.example.xcel_loader.uploadnames;


import com.example.xcel_loader.controller.NameSearchController;
import com.example.xcel_loader.utils.ResourceTest;
import com.example.xcel_loader.utils.TestHelper;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;


public class UploadBrsNameTest extends ResourceTest {



    @Test
    public void when_a_name_search_request_is_received_it_is_successful(){



        var attachmentResp = given().log()
                .everything()
                .multiPart("file", TestHelper.getXlsFile() )
//                .multiPart("file", TestHelper.getSampleFile() , "application/vnd.ms-excel")
                .expect()
//                .statusCode(HttpURLConnection.HTTP_OK)
                .when()
                .post(NameSearchController.UPLOAD_BRS_NAMES_BY_FILE);
        attachmentResp.prettyPrint();


    }



}
