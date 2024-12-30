package network;

import io.restassured.response.Response;
import utils.Constants;

import static io.restassured.RestAssured.given;

public class Network {
    private static Network instance = new Network();
    private String authToken;

    private Network() {}

    public static Network getInstance() {
        return instance;
    }

    public String createAuthToken(String username, String password) {
        String url = Constants.BASE_URL + "/auth";
        String body = "{\n" +
                "    \"username\" : \"" + username + "\",\n" +
                "    \"password\" : \"" + password + "\"\n" +
                "}";

        Response response = given()
                .contentType("application/json")
                .body(body)
                .when()
                .post(url)
                .then()
                .extract().response();

        // Yanıtı loglayın
        System.out.println("Response Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Statü kodu kontrolü
        if (response.statusCode() == 200) {
            authToken = response.jsonPath().getString("token");
        } else {
            throw new RuntimeException("Failed to create auth token, status code: " + response.statusCode());
        }

        return authToken;
    }

    public Response getList(String addUrl) {
        String url = Constants.BASE_URL + addUrl;
        return given()
                .contentType("application/json")
                .when()
                .get(url)
                .then()
                .extract().response();
    }

    public Response postRequest(String addUrl, String requestBody) {
        String url = Constants.BASE_URL + addUrl;
        return given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(url)
                .then()
                .extract().response();
    }

    public Response putRequest(String addUrl, String requestBody) {
        String url = Constants.BASE_URL + addUrl;
        return given()
                .contentType("application/json")
                .cookie("token", authToken)
                .body(requestBody)
                .when()
                .put(url)
                .then()
                .extract().response();
    }

    public Response patchRequest(String addUrl, String requestBody) {
        String url = Constants.BASE_URL + addUrl;
        return given()
                .contentType("application/json")
                .cookie("token", authToken)
                .body(requestBody)
                .when()
                .patch(url)
                .then()
                .extract().response();
    }

    public Response deleteRequest(String addUrl) {
        String url = Constants.BASE_URL + addUrl;
        return given()
                .contentType("application/json")
                .cookie("token", authToken)
                .when()
                .delete(url)
                .then()
                .extract().response();
    }
}
