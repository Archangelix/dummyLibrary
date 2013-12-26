package test

import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest.FlatSpec
import org.scalatest.selenium.HtmlUnit
import play.api.test.Helpers._
import org.scalatest.matchers.ShouldMatchers
import org.openqa.selenium.By
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.ie.InternetExplorerDriver
import java.io.File
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.support.ui.ExpectedConditions
import org.scalatest.Matchers
import org.openqa.selenium.support.ui.ExpectedCondition

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */ 
class FunctionalSpec extends FlatSpec with Matchers {
  
  val HOST = "http://localhost:9000/"
  val file = new File("C:/Program Files (x86)/Internet Explorer/IEDriverServer.exe");
  System.setProperty("webdriver.ie.driver", file.getAbsolutePath());

  def withDriver(f: FirefoxDriver => Unit) = { 
    val driver = new FirefoxDriver
    try {
      f(driver)
    } finally {
      driver.quit
    }
  }
  
  def loginAdmin(implicit driver: FirefoxDriver) = {
      driver.getTitle() should include("Dummy")

      driver.findElementById("username").sendKeys("admin")
      driver.findElementById("password").sendKeys("admin")
      driver.findElementById("password").submit()
  }
  
  def logout(implicit driver: FirefoxDriver) = {
      // find the element
      val anchor = driver.findElement(By.xpath("//*[@id='linkLogout']"))
      // anchor.click() Buggy. Use javascript instead temporarily.
      driver.executeScript("return document.getElementById('linkLogout').click();")
  }
  
  "The blog app home page" should "login and logout correctly" in withDriver { implicit driver => 
      driver get HOST

      loginAdmin
      driver.getPageSource() should include("admin")

      logout
      driver.getPageSource() should not include ("admin")
  }

  "The blog app home page" should "search and find the result correctly" in withDriver { implicit driver => 
    driver get HOST
    
    driver.findElement(By.id("txtSearch")).sendKeys("lord")
    driver.findElement(By.id("txtSearch")).submit()
    new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")))
    driver.getPageSource() should include("Lord")
    
//    driver.findElement(By.id("txtSearch")).sendKeys("thisShouldn'tAppear")
//    driver.findElement(By.id("txtSearch")).submit()
//    driver.getPageSource() should not include("thisShouldn'tAppear")
  }
  
  "The blog app home page" should "search and NOT find the result correctly" in withDriver { implicit driver => 
    driver get HOST
    
    driver.findElement(By.id("txtSearch")).sendKeys("thisShouldntAppear")
    driver.findElement(By.id("txtSearch")).submit()
    new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")))
    driver.getPageSource() should include("The catalogs are empty!")
  }
  
}