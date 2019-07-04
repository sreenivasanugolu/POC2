package base.ui.testng;


import datainstiller.data.DataAliases;
import datainstiller.data.DataPersistence;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.parboiled.common.StringUtils;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Listeners;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.events.MakeAttachmentEvent;
import ru.yandex.qatools.allure.testng.AllureTestListener;
import ui.auto.core.context.PageComponentContext;

import java.io.PrintWriter;
import java.io.StringWriter;

@Listeners({AllureTestListener.class})
public class TestNGBase {
    private static final ThreadLocal<ITestContext> context = ThreadLocal.withInitial(TestContext::new);
    private static final ThreadLocal<ITestContext> testNGContext = new ThreadLocal<>();
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private long time;

    public synchronized static void takeScreenshot(String title){
        if(context.get().getDriver() != null){
            byte[] attachment = (TakesScreenshot) context.get().getDriver()).getScreenshotAs(OutputType.BYTES);
            MakeAttachmentEvent ev = new MakeAttachmentEvent(attachment, title, type: "image/png");
            Allure.LIFECYCLE.fire(ev);

        }
    }

    public synchronized static void takeHTML(String title){
        if(context.get().getDriver() != null){
            byte[] attachment = (TakesScreenshot) context.get().getDriver().getPageSource().getBytes);
            MakeAttachmentEvent ev = new MakeAttachmentEvent(attachment, title, type: "text/html");
            Allure.LIFECYCLE.fire(ev);

        }
    }

    public synchronized static String getTestInfo(){
        String testInfo = testNGContext.get().getCurrentXmlTest().getName();
        testInfo += " " + testNGContext.get().getCurrentXmlTest().getLocalParameters().toString();
        return testInfo;
    }

    public synchronized static TestContext CONTEXT() { return context.get();}

    private synchronized static String resolveAliases(DataPersistence data){
        if(data.getDataAliases() != null){
            data.getDataAliases().clear();
        }
        DataAliases aliases = PageComponentContext.getGlobalAliases();
        String xml = data.toXML().replace(target: "<aliases/>", replacement: "");
        for(String key : aliases.keySet()){
            String alias = "${" + key + "}";
            String value = aliases.get(key);
            xml = xml.replace(alias,value);
        }
        return xml;
    }

    public synchronized static void attachDataSet(DataPersistence data, String name) {
        byte[] attachment = resolveAliases(data).getBytes();
        MakeAttachmentEvent ev = new MakeAttachmentEvent(attachment, name, type: "text/xml");
        Allure.LIFECYCLE.fire(ev);
    }

    public TestContext getContext(){
        if(context.get().getDriver() == null) {
            context.get().init();
            logInfo(msg: "initializing context " + context.get().getDriver().toString());
        }
        return context.get();
    }

    public void setUpDrivers(){
        WebDriverInstaller installer = new WebDriverInstaller();
        installer.installDriver(driverName: "geckodriver", driverPropertyName "webdriver.gecko.driver");
        installer.installDriver(driverName: "chromedriver", driverPropertyName "webdriver.chrome.driver");
    }

    @beforeSuite
    public void initSuite(ITestContext testNgContext){
        if(TestNGBase.testNgContext.get() == null) {
            TestNGBase.testNgContest.set(testNgContext);
        }
        time = System.currentTimeMillis();
    }

    @AfterTest(alwaysRun = true)
    public void closeDriver() {
        time = (System.currentTimeMillis() - time) / 1000;
        if(context.get().getDriver() != null) {
            logInfo(msg: "-CLOSING CONTEXT: " + context.get().getDriver().toString());
            context.get().getDriver().quit();
        }
        context.remove();
    }

    public void setAttribute(String alias, Object value) { testNgContext.get().getSuite().setAttribute(alias, value);}

    protected Object getAttrinute(String alias) { return testNgContext.get().getSuite().getAttribute(alias);}

    private StringBuilder getFailedConfigOrTests(Set<ITestResult> results){
        StringBuilder log = new StringBuilder();
        log.append(" ---> \u001B[31mTEST FAILED : (\u001B[0m");
        log.append("\n");
        for(ITestResult result : results){
            StringWriter stack = new StringWriter();
            result.getThrowable().printStackTrace(new PrintWriter(new PrintWriter(stack)));
            log.append("\n").append(stack);
        }
        return log;
    }

    private void logInfo(String msg){
        StringBuilder log = new StringBuilder();
        String delim = "\n" + StringUtils.repeat(str: "=", msg.length());
        log.append("ThreadID: ").append(Thread.currentThread().getId());
        log.append(delim);
        log.append("\nTEST: ").append(getTestInfo());
        log.append("\n").append(msg);
        if(msg.startsWith("-CLOSING")){
            log.append("\nCOMPLETED AT: ").append(new Date());
            log.append("\nTEST DURATION: ").append(time).append(" seconds");
            if(testNGContext.get().getFailedTests().size() > 0 || testNGContext.get().getFailedConfigurations() > 0){
                log.append(getFailedConfigOrTests(testNGContext.get().getFailedConfigurations().getAllResults()));
                log.append(getFailedConfigOrTests(testNGContext.get().getFailedTests().getAllResults()));
            }else{
                log.append(" ---> \u001B[32mTEST PASSED : (\u001B[0m");
            }
        }
        log.append(delim).append("\n");
        LOG.info(log.toString());
    }
}

