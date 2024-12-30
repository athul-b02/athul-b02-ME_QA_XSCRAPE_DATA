package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
// import io.github.bonigarcia.wdm.WebDriverManager;
import demo.wrappers.Wrappers;

public class TestCases {
    ChromeDriver driver;

    /*
     * TODO: Write your tests here with testng @Test annotation. 
     * Follow `testCase01` `testCase02`... format or what is provided in instructions
     */

     
    /*
     * Do not change the provided methods unless necessary, they will help in automation and assessment
     */
    @Test
    public void testCase01(){
        Wrappers.navigatetoUrl(driver, "https://www.scrapethissite.com/pages/");
        Wrappers.clickElement(driver, By.xpath("//a[contains(text(),'Hockey')]"));
        ArrayList<List<Object>> dataList = Wrappers.scrapeHocekyTableDate(driver, 4,40);
        Wrappers.createJsonFile(dataList, "hockey-team-data", "hockey");
    }
    @Test
    public void testCase02(){
        Wrappers.navigatetoUrl(driver, "https://www.scrapethissite.com/pages/");
        Wrappers.clickElement(driver, By.xpath("//a[contains(text(),'Oscar')]"));
        ArrayList<List<Object>> dataList = Wrappers.scrapeOscarList(driver);
        Wrappers.createJsonFile(dataList, "oscar-winner-data", "oscar");

        String filePath = System.getProperty("user.dir")+"\\src\\test\\resources\\oscar-winner-data.json";
        File file = new File(filePath);
        Assert.assertTrue(file.exists(), "json File for oscar data does not exist");
        Assert.assertTrue(file.length()>0, "json File is empty");
    }   


    @BeforeTest
    public void startBrowser()
    {
        System.setProperty("java.util.logging.config.file", "logging.properties");

        // NOT NEEDED FOR SELENIUM MANAGER
        // WebDriverManager.chromedriver().timeout(30).setup();

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logs = new LoggingPreferences();

        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        options.addArguments("--remote-allow-origins=*");

        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log"); 

        driver = new ChromeDriver(options);

        driver.manage().window().maximize();
    }

    @AfterTest
    public void endTest()
    {
        driver.close();
        driver.quit();

    }
}