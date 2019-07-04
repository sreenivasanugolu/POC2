package base.ui.support;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import ui.auto.core.context.PageComponentContext;

public class TestContext extends PageComponentContext {
    private static ThreadLocal<TestProperties> props = ThreadLocal.withInitial(TestProperties::new);

    public TestContext(WebDriver driver) {
        super(driver);
        setTimeouts();
    }

    public TestContext() { this(driver: null); }

    public static TestProperties getTestProperties() { return props.get();}

    public void init(){
        driver = getTestProperties().getBrowserType().getNewWebDriver();
        String res = getTestProperties().getScreenSize();
        if(res != null){
            String resWH = res.toLowerCase().split(regex: "x");
            int width = Integer.parseInt(resWH[0].trim());
            int height = Integer.parseInt(resWH[1].trim());
            Dimension dim = new Dimension(width, height);
            driver.manage().window().setSize(dim);
        }
    }

    public String getAlias(String key) { return GlobalAliases().get(key);}

    public void setAlias(String key, String value) { getGlobalAliases().put(key, value);}

    private void setTimeouts(){
        if(getTestProperties().getElementTimeout() > 0){
            setAjaxTimeOut(getTestProperties().getElementTimeout());
        }
        if(getTestProperties().getPageTimeout() > 0){
            setWaitForUrlTimeOut(getTestProperties().getPageTimeout() * 1000);
        }
    }
}
