import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Naukrijobs {

    public static void main(String[] args) throws IOException, InterruptedException {
        String email = "swamymushini@gmail.com";
        String password = "niNEWSS3**";
        String cookieFile = "naukri_cookies.data";

        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            File file = new File(cookieFile);

            // Reuse cookies if available
            if (file.exists()) {
                driver.get("https://www.naukri.com/");
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    @SuppressWarnings("unchecked")
                    Set<Cookie> cookies = (Set<Cookie>) ois.readObject();

                    for (Cookie cookie : cookies) {
                        if ((cookie.getDomain().contains("naukri.com")) &&
                                (cookie.getExpiry() == null || cookie.getExpiry().after(new Date()))) {
                            try {
                                driver.manage().addCookie(cookie);
                            } catch (Exception e) {
                                System.out.println("Skipping invalid cookie: " + cookie.getName());
                            }
                        }
                    }

                    driver.navigate().refresh();
                    Thread.sleep(5000);
                    if (driver.getPageSource().contains("My Naukri")) {
                        System.out.println("Logged in using cookies.");
                        driver.quit();
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("Failed to load cookies, continuing with login...");
                }
            } else {
                // Fresh login
                driver.get("https://www.naukri.com/");
                driver.findElement(By.id("login_Layer")).click();

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='text']")));
                driver.findElement(By.cssSelector("input[type='text']")).sendKeys(email);
                driver.findElement(By.cssSelector("input[type='password']")).sendKeys(password);
                driver.findElement(By.cssSelector("button[type='submit']")).click();

                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".view-profile-wrapper")));

                // Save cookies
                Set<Cookie> cookies = driver.manage().getCookies();
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cookieFile))) {
                    oos.writeObject(new HashSet<>(cookies));
                }
                System.out.println("Login successful and cookies saved.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Navigate to job section and attempt to apply
        driver.findElement(By.cssSelector("a[class='view-all-link'] span")).click();
        WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait2.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//i[@class='dspIB naukicon naukicon-ot-checkbox'])[1]")));
        driver.findElement(By.xpath("(//i[@class='dspIB naukicon naukicon-ot-checkbox'])[1]")).click();
        driver.findElement(By.cssSelector(".multi-apply-button.typ-16Bold")).click();

        // Handle chatbot questions using Gemini
        Thread.sleep(5000);
        WebElement chatbox = driver.findElement(By.cssSelector(".chatbot_Drawer.chatbot_right"));
        wait2.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".chatbot_Drawer.chatbot_right")));
        do {
            if (chatbox.isDisplayed()) {
                wait2.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".botMsg.msg")));
                List<WebElement> questionLabels = driver.findElements(By.cssSelector(".botMsg.msg"));
               
                List<WebElement> answerInputs = driver.findElements(By.xpath("//div[@class='chatbot_InputContainer']"));

                if (questionLabels.size() == answerInputs.size() && questionLabels.size() > 0) {
                    for (int i = 0; i < questionLabels.size(); i++) {
                        String questionText = questionLabels.get(i).getText();
                        String answer = getAnswerFromGemini(questionText);

                        WebElement input = answerInputs.get(i);
                        input.sendKeys(answer);
                        driver.findElement(By.cssSelector(".sendMsg")).click();

                        System.out.println("Q: " + questionText);
                        System.out.println("A: " + answer);
                    }
                }
            } else {
                System.out.println("Chatbox is not displayed");
                break; // Exit the loop if chatbox is not visible
            }

            // Optionally add a wait here to prevent rapid loop execution
            Thread.sleep(1000);

        } while (chatbox.isDisplayed());

        driver.quit();
    }

    public static String getAnswerFromGemini(String userInput) throws IOException, InterruptedException {
        String apiKey = "YOUR_API_KEY_HERE";
        String modelId = "gemini-2.0-flash";
        String apiUrl = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:streamGenerateContent?key=%s",
                modelId, apiKey
        );

        String requestBody = """
        {
            "contents": [
                {
                    "role": "user",
                    "parts": [
                        {
                            "text": "%s"
                        }
                    ]
                }
            ]
        }
        """.formatted(userInput);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String responseBody = response.body();
        Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(responseBody);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            result.append(matcher.group(1));
        }

        return result.toString();
    }
}
