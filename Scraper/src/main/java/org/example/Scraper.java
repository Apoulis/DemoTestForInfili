package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Scraper {
    public static void main(String[] args) {

        // Set the EdgeDriver path
        System.setProperty("webdriver.edge.driver", "C:\\WebDriver\\msedgedriver.exe");

        // Create an instance of EdgeDriver
        WebDriver driver = new EdgeDriver();

        try {
            // Open website
            driver.get("https://www.okairos.gr/");

            // Create WebDriverWait object
            WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));

            // Wait for the pop-up to appear and click on the "Συναίνεση" button
            WebElement consentButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".fc-button.fc-cta-consent.fc-primary-button")));
            consentButton.click();

            //
            //Find the search field and type "Aθήνα"
            WebElement searchBox = driver.findElement(By.id("q")); // Χρησιμοποιούμε ID
            searchBox.sendKeys("Αθήνα");

            // Submit the search
            WebElement submitButton = driver.findElement(By.cssSelector("input[type='submit']"));
            submitButton.click();

            // Wait for the results to load
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("search-results")));

            // Find the link for Αθήνα
            WebElement athensLink = driver.findElement(By.cssSelector("#search-results ol li a[href='/αθήνα.html']"));
            athensLink.click(); // Κάντε κλικ στην επιλογή "Αθήνα"

            // Wait for the new page to load and find the temperature
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("td.weather.current")));

            // Find the element that shows the current temperature
            WebElement temperatureElement = driver.findElement(By.cssSelector("td.weather.current div"));
            String temperatureText = temperatureElement.getText();

            // Remove the Celsius symbol
            String temperatureValue = temperatureText.replace("º", "").trim();

            // If the temperature contains a comma, replace it with a period.
            //temperatureValue = temperatureValue.replace(",", ".");

            // convert to float
            float temperature = Float.parseFloat(temperatureValue);


            // get the current date and time
            LocalDateTime now = LocalDateTime.now();
            String formattedDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));


            sendTemperatureToApi(temperature, formattedDateTime);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Closing Browser
            driver.quit();
        }
    }

    private static void sendTemperatureToApi(float temperature, String dateTime) {
        try {
            // URL του API endpoint
            URL url = new URL("http://localhost:8080/temperature");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setup Post request
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);


            // Create JSON payload
            String jsonInputString = String.format("{\"temperature\": %.1f, \"datetime\": \"%s\"}", temperature, dateTime);
            System.out.println("JSON Payload: " + jsonInputString); // Εμφάνιση του JSON payload για έλεγχο

            // Send data
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Check response
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_CREATED) {
                System.out.println("Temperature and time data successfully saved to the database.");
            } else {
                System.out.println("Failed to save temperature and time data. HTTP error code: " + code);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }
                    System.out.println("Error Response: " + response.toString());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void getTemperature(long id) {
        try {
            // URL to API endpoint for GET request
            URL url = new URL("http://localhost:8080/temperature/" + id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setup get request
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            // Check response
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                //  If response is 200 ok
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }
                    // Print response
                    System.out.println("Response: " + response.toString());
                }
            } else {
                System.out.println("Failed to get temperature data. HTTP error code: " + code);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteTemperatureFromApi(int id) {
        try {
            // URL to API endpoint for delete
            URL url = new URL("http://localhost:8080/temperature/" + id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Accept", "application/json");

            // Check response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                System.out.println("Temperature with ID " + id + " successfully deleted.");
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                System.out.println("Temperature with ID " + id + " not found.");
            } else {
                System.out.println("Failed to delete temperature. HTTP error code: " + responseCode);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }
                    System.out.println("Error Response: " + response.toString());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}