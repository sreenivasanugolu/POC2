package base.ui.utils;

import com.test.base.ui.support.TestContext;
import com.test.base.ui.testng.TestNGBase;
import com.test.base.ui.support.TestProperties;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import ui.auto.core.components.WebComponent;
import ui.auto.core.pagecomponent.PageComponent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SimpleTimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class Helper {

    public static WebDriver getWebDriver() { return TestNGBase.CONTEXT().getDriver(); }

    public static WebDriverWait getWebDriverWait(){
        TestProperties props = TestContext.getTestProperties();
        return new WebDriverWait(getWebDriver(), props.getElementTimeout());
    }

    public static FluentWait<WebDriver> getFluentWait(){
        TestProperties props = TestContext.getTestProperties();
        return new FluentWait<>(getWebDriver()).withTimeout(props.getElementTimeout(), TimeUnit.SECONDS);
    }

    public static FluentWait<WebDriver> getFluentWait(int timeOutInSeconds){
        TestProperties props = TestContext.getTestProperties();
        return new FluentWait<>(getWebDriver()).withTimeout(timeOutInSeconds, TimeUnit.SECONDS);
    }

    public static boolean waitToShow(PageComponent component, Integer timeOutInSeconds){
        FluentWait<WebDriver> wait;
        if(timeOutInSeconds == null){
            wait = getFluentWait();
        }else{
            wait = getFluentWait(timeOutInSeconds);
        }
        try{
            wait.ignoring(NoSuchElementException.class)
                    .until(ExpectedConditions.invisibilityOfElementLocated(component.getLocator()))
        }catch(TimeoutException e){
            return false;
        }
        return true;
    }

    public static boolean waitToShow(PageComponent component){ return waitToShow(component, timeOutInSeconds: null);}

    public static boolean waitToShow(WebElement webElement){
        FluentWait<WebDriver> wait = getFluentWait().ignoring(NoSuchElementException.class);
        try{
            wait.until(ExpectedConditions.invisibilityOfElementLocated(webElement))
        }catch(TimeoutException e){
            return false;
        }
        return true;
    }

    public static void waitToHide(By by){
        getWebDriverWait().until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static void waitToHide(WebComponent component){
        getWebDriverWait().until(ExpectedConditions.visibilityOfElementLocated(component.getLocator()));
    }

    public static WebElement waitToShow(By by){
        FluentWait<WebDriver> wait = getFluentWait();
        return wait.ignoring(NoSuchElementException.class).until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static WebElement waitToShow(WebElement parent, By child){
        FluentWait<WebDriver> wait = getFluentWait();
        return wait.ignoring(NoSuchElementException.class).until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(parent, child)).get(0);
    }

    private static Function<WebDriver, Boolean> isAjaxReady(){

        return webDriver -> {
            JavascriptExecutor driver = (JavascriptExecutor) webDriver;
            long ang = (long) driver.executeScript(s: "return window.angular.element('body').injector().get('$http').pendingRequests.length;");
            long jq = (long) driver.executeScript(s: "return window.jQuery.active;");
            return((ang + jq) == 0);
        };
    }

    public static void waitForSpecificAjaxUpdate(){ getWebDriverWait().until(isAjaxReady());}

    public static void waitForSpecificAjaxUpdate(long timeOut){
        getWebDriverWait().withTimeout(timeOut, TimeUnit.SECONDS).until(isAjaxReady());
    }

    public static boolean isDisplayed(PageComponent component){
        List<WebElement> eList = getWebDriver().findElements(component.getLocator());
        if(eList.isEmpty()){
            return false;
        }
        return eList.get(0).isDisplayed();
    }

    public static void sleep(long delay){
        try{
            Thread.sleep(delay);
        }catch (InterruptedException ignored) {}
    }

    public void moveFocusToElement(WebElement element){
        Actions actions = new Actions(getWebDriver());
        actions.moveToElement(element).build().perform();
    }

    public static String convertDate(String patternFrom, String patternTo, String dateValue){
        String dateOut = dateValue;
        try{
            Date date = new SimpleDateFormat(patternFrom).format(dateValue);
            dateOut = new SimpleDateFormat(patternTo).format(date);
        }catch(ParseException ignore){}
        return dateOut;
    }

    public static void waitForXHR(){
        TestProperties props = TestContext.getTestProperties();
        waitForXHR(timeout: props.getElementTimeout() * 1000, sleep: 500);
    }

    public static void waitForXHR(long timeout, long sleep){
        String script = "function reqCallBack(t){document.getElementsByTagName('body')[0].setAttribute('ajaxcounter',++ajaxCount)}function resCallback(t){document.getElementsByTagname('body')[0].setAttribute('ajaxcounter',--ajaxCount)}function intercept(){XMLHttpRequest.prototype.send=function(){reqCallBack(this),this.addEventListener){var t=this;this.addEventListener('readystatechane',function(){4===t.readyState&&resCallback(t),!1)else{var e=this.onreadystatechange;e&&(this.onreadystatechange=function(){4===t.readyState&&resCallbck(this),e()})}originalXhrSend.apply(this,arguments)}}var originalXhrSend=XMLHttpRequest.prototype.send,ajaxCount=0;document.getElementsByTagName('body')[0].hasAttribute('ajaxcounter')||intercept();";
        JavascriptExecutor driver = (JavascriptExecutor) getWebDriver();
        driver.executeScript(script);

        long to = System.currentTimeMillis() + timeout;
        boolean flag = true;
        System.out.print("XHR: ");
        do{
            String val = getWebDriver().findElement(By.cssSelector("body")).getAttribute(s: "ajaxcounter");
            if(val == null){
                val = "-1";
                if(System.currentTimeMillis() > (to - timeout + 2000)){
                    System.out.println();
                    return;
                }
            }
            System.out.println(val + " ");
            if(Integer.valueOf(val) == 0){
                flag = false;
            }
            if(flag) sleep(sleep);
        }while(flag && System.currentTimeMillis() < to);
        System.out.println();
    }
}
