package test;

import com.google.gson.Gson;
import io.restassured.response.Response;
import model.Booking;
import network.Network;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RestfulBookerTest {
    private Response response;
    private Gson gson;
    private String url;
    private static Integer bookingId;

    public RestfulBookerTest() {
        gson = new Gson();
    }

    @BeforeClass
    public void setup() {
        Network.getInstance().createAuthToken("admin", "password123");
    }

    @Test
    public void testPingEndpoint() {
        try {
            String url = "https://restful-booker.herokuapp.com/ping";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            Assert.assertEquals(responseCode, 201, "Expected response code is 201");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("Response Body: " + response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Request failed: " + e.getMessage());
        }
    }

    @Test(priority = 1)
    public void testCreateBooking() {
        url = "/booking";
        String newBookingJson = "{\n" +
                "    \"firstname\": \"Jim\",\n" +
                "    \"lastname\": \"Brown\",\n" +
                "    \"totalprice\": 111,\n" +
                "    \"depositpaid\": true,\n" +
                "    \"bookingdates\": {\n" +
                "        \"checkin\": \"2024-01-01\",\n" +
                "        \"checkout\": \"2024-01-10\"\n" +
                "    },\n" +
                "    \"additionalneeds\": \"Breakfast\"\n" +
                "}";

        response = Network.getInstance().postRequest(url, newBookingJson);
        Assert.assertEquals(response.statusCode(), 200, "Status code mismatch");

        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);

        bookingId = response.jsonPath().getInt("bookingid");
        Assert.assertNotNull(bookingId, "Booking ID should not be null");
    }

    @Test(priority = 2)
    public void testGetBookingById() {
        Assert.assertNotNull(bookingId, "Booking ID is required for this test");
        
        url = "/booking/" + bookingId;
        response = Network.getInstance().getList(url);
        
        Assert.assertEquals(response.statusCode(), 200, "Status code mismatch");
        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);

        Booking booking = gson.fromJson(responseBody, Booking.class);
        Assert.assertNotNull(booking, "Booking object should not be null");
        Assert.assertEquals(booking.getFirstname(), "Jim", "Firstname mismatch");
        Assert.assertEquals(booking.getLastname(), "Brown", "Lastname mismatch");
        Assert.assertEquals(booking.getTotalprice(), 111, "Total price mismatch");
        Assert.assertTrue(booking.isDepositpaid(), "Deposit paid mismatch");
        Assert.assertEquals(booking.getAdditionalneeds(), "Breakfast", "Additional needs mismatch");
    }

    @Test(priority = 3)
    public void testUpdateBooking() {
        Assert.assertNotNull(bookingId, "Booking ID is required for this test");
        
        url = "/booking/" + bookingId;
        String updatedBookingJson = "{\n" +
                "    \"firstname\": \"James\",\n" +
                "    \"lastname\": \"Brown\",\n" +
                "    \"totalprice\": 150,\n" +
                "    \"depositpaid\": false,\n" +
                "    \"bookingdates\": {\n" +
                "        \"checkin\": \"2024-02-01\",\n" +
                "        \"checkout\": \"2024-02-10\"\n" +
                "    },\n" +
                "    \"additionalneeds\": \"Dinner\"\n" +
                "}";

        response = Network.getInstance().putRequest(url, updatedBookingJson);
        Assert.assertEquals(response.statusCode(), 200, "Status code mismatch");

        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);

        Booking booking = gson.fromJson(responseBody, Booking.class);
        Assert.assertEquals(booking.getFirstname(), "James", "Firstname mismatch");
        Assert.assertEquals(booking.getLastname(), "Brown", "Lastname mismatch");
    }

    @Test(priority = 4)
    public void partialTestUpdateBooking() {
        Assert.assertNotNull(bookingId, "Booking ID is required for this test");
        
        url = "/booking/" + bookingId;
        String partialUpdatedBookingJson = "{\n" +
                "    \"firstname\": \"James\",\n" +
                "    \"lastname\": \"Brown\"\n" +
                "}";

        response = Network.getInstance().patchRequest(url, partialUpdatedBookingJson);
        Assert.assertEquals(response.statusCode(), 200, "Status code mismatch");

        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);

        Booking booking = gson.fromJson(responseBody, Booking.class);
        Assert.assertEquals(booking.getFirstname(), "James", "Firstname mismatch");
        Assert.assertEquals(booking.getLastname(), "Brown", "Lastname mismatch");
    }

    @Test(priority = 5)
    public void testDeleteBooking() {
        Assert.assertNotNull(bookingId, "Booking ID is required for this test");
        
        url = "/booking/" + bookingId;
        response = Network.getInstance().deleteRequest(url);
        Assert.assertEquals(response.statusCode(), 201, "Status code mismatch");

        Response getResponse = Network.getInstance().getList(url);
        Assert.assertEquals(getResponse.statusCode(), 404, "Booking was not deleted");
    }
}
