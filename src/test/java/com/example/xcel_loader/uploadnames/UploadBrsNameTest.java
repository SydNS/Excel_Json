package ug.go.ursb.namesearch.uploadnames;


import org.junit.Test;
import ug.go.ursb.namesearch.ResourceTest;
import ug.go.ursb.namesearch.TestHelper;
import ug.go.ursb.namesearch.api.NameSearchController;

import java.net.HttpURLConnection;

import static com.jayway.restassured.RestAssured.given;

public class UploadBrsNameTest extends ResourceTest {



    @Test
    public void when_a_name_search_request_is_received_it_is_successful(){

        var newName = given().log()
                .everything()
                .header("Content-Type", "application/json")
                .body(TestHelper.generateFakeNameRequest("Ham Soft"))
                .expect()
                .statusCode(HttpURLConnection.HTTP_ACCEPTED)
                .when()
                .post(NameSearchController.SEARCH_PATH)
                .body();

                newName.prettyPrint();



        var attachmentResp = given().log()
                .everything()
                .multiPart("file", TestHelper.getSampleFile() )
//                .multiPart("file", TestHelper.getSampleFile() , "application/vnd.ms-excel")
                .expect()
//                .statusCode(HttpURLConnection.HTTP_OK)
                .when()
                .post(NameSearchController.UPLOAD_BRS_NAMES_BY_FILE);
        attachmentResp.prettyPrint();


    }



}
