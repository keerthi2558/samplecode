
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Naukrijobs2 {

	public static void main(String[] args) throws InterruptedException {
		String userid = "swamymushini@gmail.com";
		String password = "niNEWSS3**";

		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		driver.manage().window().maximize();

		driver.get("https://www.naukri.com/"); // Ensure domain is loaded first

		driver.findElement(By.id("login_Layer")).click();

		driver.findElement(By.xpath("//input[@placeholder='Enter your active Email ID / Username']")).sendKeys(userid);
		driver.findElement(By.xpath("//input[@placeholder='Enter your password']")).sendKeys(password);
		driver.findElement(By.cssSelector("button[type='submit']")).click();
		driver.findElement(By.xpath("//span[text()='Jobs']")).click();

		driver.findElement(By.cssSelector("a[class='view-all-link'] span")).click();

		int minExp = 3;
		int maxExp = 6; 
		Set<String> skillsSet = Set.of("Java", "Selenium", "Spring");

		try {
			if (minExp <= 5 && maxExp >= 5 && skillsSet.contains("Java")) {

				// Find all checkboxes (update the locator as needed)
				List<WebElement> checkboxes = driver
						.findElements(By.xpath("(//i[@class='dspIB naukicon naukicon-ot-checkbox'])"));
				WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(10));
				wait2.until(ExpectedConditions
						.visibilityOfElementLocated(By.xpath("(//i[@class='dspIB naukicon naukicon-ot-checkbox'])")));

				int selectedCount = 0;

				for (WebElement checkbox : checkboxes) {
					if (selectedCount >= 5)
						break;

					if (!checkbox.isSelected() && checkbox.isDisplayed()) {
						checkbox.click();
						selectedCount++;
					}
				}

				// Click the apply button
				WebElement applyBtn = driver.findElement(By.cssSelector(".multi-apply-button.typ-16Bold")); 
				applyBtn.click();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
			try {
				WebElement chatbox = driver.findElement(By.cssSelector(".chatbot_Drawer.chatbot_right")); 
																											
				if (chatbox.isDisplayed()) {
					System.out.println("Chatbox is displayed");

					WebElement textbox = driver.findElement(By.xpath("//div[@class='textArea']")); 
																									
					WebElement sendButton = driver.findElement(By.xpath("//div[@class='sendMsg']"));

					if (textbox.isDisplayed() && sendButton.isDisplayed()) {
						textbox.sendKeys("Gopal");
						sendButton.click();
					}

					Thread.sleep(2000); // Wait to avoid rapid sending
				} else {
					break; // Exit if chatbox is not displayed
				}
			} catch (NoSuchElementException e) {
				System.out.println("Chatbox not found, exiting loop.");
				break;
			}
		}

	}
}
