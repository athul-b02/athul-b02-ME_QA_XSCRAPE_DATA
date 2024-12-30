package demo.wrappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class Wrappers {
    /*
     * Write your selenium wrappers here
     */
    public static void navigatetoUrl(ChromeDriver driver, String url){
        driver.get(url);
    }

    public static void clickElement(ChromeDriver driver, By locator){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    public static ArrayList<List<Object>> scrapeHocekyTableDate(ChromeDriver driver, int noOfPages,int winPercentage){
        ArrayList<List<Object>> dataList = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
            "(//div[@class='row pagination-area']//ul//a)[1]"
        ))).click();
        int currentPageIndex = 1;
        while(true){                 

            WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table")));
            WebElement currentPage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='row pagination-area']//a[strong]")
            ));
            currentPageIndex = Integer.parseInt(currentPage.getText());
            System.out.println("Scraping data from page "+currentPageIndex);
        
            List<WebElement> rowData = table.findElements(By.xpath(".//tbody/tr[@class='team']"));
            int rowsScraped = 0;           
            for(WebElement row : rowData){
                double currentWinRate = Double.parseDouble(row.findElement(By.xpath(".//td[6]")).getText());
                if(currentWinRate<(double)winPercentage/100){
                    dataList.add(Arrays.asList(
                        String.valueOf(Instant.now().getEpochSecond()),
                        row.findElement(By.xpath(".//td[1]")).getText(),
                        row.findElement(By.xpath(".//td[2]")).getText(),
                        row.findElement(By.xpath(".//td[6]")).getText()
                    ));
                    rowsScraped++;
                }               
            }
            System.out.println("Number of rows Scraped from page "+currentPageIndex
            +" = "+rowsScraped);

            //condition to check if the required no of Pages has been clicked through
            if(currentPageIndex<noOfPages){
                WebElement nextPage = currentPage.findElement(By.xpath(
                    ".//parent::li/following-sibling::li/a"
                ));
                System.out.println("next page = "+nextPage.getText());

                nextPage.click();
                // adding wait statement to make sure previous is table is removed from the DOM
                wait.until(ExpectedConditions.stalenessOf(table));
            }
            else{
                int nofRows = dataList.size();
                System.out.println("Total number of rows scraped = "+nofRows);
                break;
            }
        }
      
        return dataList;  
    }

    public static ArrayList<List<Object>> scrapeOscarList(ChromeDriver driver){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//a[@class='year-link']")
        ));
        List<WebElement> yearList = driver.findElements(By.xpath(
            "//a[@class='year-link']"
        ));
        ArrayList<List<Object>> dataList = new ArrayList<>();
        for(WebElement year: yearList){
            System.out.println("Scraping values for year: "+year.getText()+".....");
            //click on the year
            year.click();
            //wait till the table is loaded
            WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table")));
            List<WebElement> rowData = new ArrayList<>();
            //finding top 5 movies for each year
            for(int i =1;i<=5;i++){
                rowData.add(table.findElement(By.xpath("(//table/tbody/tr)["
                +i+"]")));
            }

            for(WebElement row: rowData){
                ArrayList<Object> dataValues = new ArrayList<>();
                dataValues.add(String.valueOf(Instant.now().getEpochSecond()));
                dataValues.add(year.getText());
                boolean isWinner = false;
                List<WebElement> rowValues = row.findElements(By.xpath(".//td"));
                for(WebElement value : rowValues){
                    //checking the presence of flag for the best picture winner 
                    if(value.getAttribute("class").equals("film-best-picture")){
                        if(value.findElements(By.xpath(".//*[contains(@class,flag)]")).size()!=0){
                            isWinner = true;
                            dataValues.add(isWinner);   
                        }
                        else{
                            isWinner = false;
                            dataValues.add(isWinner);  
                        }
                    }
                    else{
                        dataValues.add(value.getText());
                    }
                }
                dataList.add(dataValues);

            }

        }
        return dataList;
    }

    public static void createJsonFile(ArrayList<List<Object>> dataList, String fileName,String context){
  
            if(dataList.size()==0)
                return;

            ObjectMapper mapper = new ObjectMapper();
            Map<String,Map<String,Object>> map = new LinkedHashMap<>();  
            int index = 1;
            if(context.equalsIgnoreCase("hockey")){
                System.out.println("Mapping Hockey Data to Json file.....");
                for(List<Object> dataRow : dataList){
                    Map<String,Object> valuesMap = new LinkedHashMap<>();
                    if(dataRow.size()==4){
                        valuesMap.put("Epoch time of Scraping",dataRow.get(0));
                        valuesMap.put("Team Name", dataRow.get(1));
                        valuesMap.put("Year",dataRow.get(2));
                        valuesMap.put("Win %", dataRow.get(3));
                    }else{  
                        System.out.println("incorrect number of values for the entry for mapping Hockey Data. SKIPPED-->"
                        +dataRow);
                    }
                map.put("# "+(index++)+".", valuesMap);
                }
            }       
            else if (context.equalsIgnoreCase("oscar")){
                System.out.println("Mapping Oscar Data to Json file.....");
                for(List<Object> dataRow : dataList){
                    Map<String,Object> valuesMap = new LinkedHashMap<>();
                    if(dataRow.size()==6){
                        valuesMap.put("Epoch time of Scraping",dataRow.get(0));
                        valuesMap.put("Year", dataRow.get(1));
                        valuesMap.put("Title",dataRow.get(2));
                        valuesMap.put("Nomination", dataRow.get(3));
                        valuesMap.put("Awards", dataRow.get(4));
                        valuesMap.put("isWinner", dataRow.get(5));
                    }else{  
                        System.out.println("incorrect number of values for the entry for mapping Oscar Data. SKIPPED-->"
                        +dataRow);
                    }
                    map.put("# "+(index++)+".", valuesMap);
            }
        }
        try {

            String dataValues = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
            System.out.println(dataValues);

        } catch (JsonProcessingException e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File( System.getProperty("user.dir")+"\\src\\test\\resources\\"+fileName+".json"), map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
