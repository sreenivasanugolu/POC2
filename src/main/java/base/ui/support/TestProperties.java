package base.ui.support;


import javafx.application.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;
import ru.qatools.properties.Property;
import ru.qatools.properties.PropertyLoader;
import ru.qatools.properties.Resource;
import ru.qatools.properties.Use;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("FieldCanBeLocal")
@Resource.Classpath("test.properties")

public class TestProperties {
    private String reportVersion = "1.0.1";
    @Hide
    @Property("test.env")
    @Use(EnvironmentPropertyConverter.class)
    private EnvironmentSetup.Environment testEnvironment;
    @Hide
    @Property("report.results")
    private File resultsFolder = new File(pathname:"target/results");
    @Hide
    @Property("report.folder")
    private File reportFolder = new File(pathname:"target/report");
    @Property("report.port")
    private int reportPort = 8090;
    @Property("report.show")
    private boolean showReport;
    @Property("webdriver.remote.url")
    private String remoteURL;
    @Hide
    @Use(BrowserPlatformPropertyConverter.class)
    @Property("webdriver.browser.platform")
    private Platform platform;
    @Hide
    @Property("webdriver.browser.version")
    private String version;
    @Use(BrowserTypePropertyConverter.class)
    @Property("webdriver.browser.type")
    private WebDriverTypeEnum browserType = WebDriverTypeEnum.FIREFOX;
    @Hide
    @Property("webdriver.extra.capabilities")
    private String extraCapabilities;
    @Hide
    @Property("webdriver.http.proxy")
    private String httpProxy;
    @Hide
    @Property("webdriver.https.proxy")
    private String httpsProxy;
    @Hide
    @Property("webdriver.screen.size")
    private String screenSize;
    @Hide
    @Property("webdriver.accept.ssl.certs")
    private boolean acceptSSLCerts;
    @Property("timeout.page")
    private int page_timeout; //In milliseconds
    @Property("timeout.element")
    private int element_timeout; //In milliseconds
    @Property("test.suites")
    private String suites;
    @Hide
    @Property("user.agent")
    private String userAgent;
    @Property("test.parallel.threads")
    private Integer threadCount;
    @Property("test.default.retry")
    private int testDefaultRetry = 2;
    @Property("mail.server")
    private String mailServer;
    @Property("mail.timeout")
    private long emailTimeOut = 60;
    @Property("webdriver.install")
    private boolean installDrivers = true;
    @Hide
    @Property("report.tms.url")
    private String tmsUrlPattern = "https://jira.com"
    @Hide
    @Property("report.issue.url")
    private String issueUrlPattern = "https://jira.com"

    TestProperties() {
        populateEnvProp();
        PropertyLoader.newInstance().populate(bean:this);
    }

    public void setReportUrlPatterns() {
        System.setProperty("allure.issues.tracker.patttern", issueUrlPattern);
        System.setProperty("allure.tests.management.patttern", tmsUrlPattern);
    }

    public EnvironmentSetup.Environment getTestEnvironment() {
        return testEnvironment;
    }

    private void populateEnvProp() {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Property.class)) {
                String prop = field.getAnnotation(Property.class).value();
                String value = getEnvValue(prop.replace(target:".", replacement:"_"));
                if (value != null) {
                    System.setProperty(prop, value);
                }
            }
        }
    }

    private String getEnvValue(String prop) {
        for (String key : System.getenv().keySet()) {
            if (prop.equalsIgnoreCase(key)) {
                return System.getenv(key);
            }
        }
        return null;
    }

    public String getRemoteURL() {
        return remoteURL;
    }

    public Platform getBrowserPlatform() {
        return platform;
    }

    public String getBrowserVersion() {
        return version;
    }

    public WebDriverTypeEnum getBrowserType() {
        return browserType;
    }

    public int getPageTimeout() {
        return page_timeout;
    }

    public String getElementTimeout() {
        return element_timeout;
    }

    public List<String> getSuites() {
        List<String> suitesList = new ArrayList<>();
        if (this.suites != null) {
            String[] suites = this.suites.split(regex:",");
            for (String s : suites) {
                suitesList.add(s.trim().replace(target:"\"", replacement:"").replace(target:"\'", replacement:""));
            }
        }
        return suitesList;
    }

    public boolean isShowReport() {
        return showReport;
    }

    public File getReportFolder() {
        return reportFolder;
    }

    public File getResultsFolder() {
        return resultsFolder;
    }

    public int getReportPort() {
        return reportPort;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Property.class)) {
                String prop = field.getAnnotation(Property.class).value();
                String value = null;
                try {
                    value = field.get(this).toString();

                } catch (Exception e) {
                    //do nothing
                }
                str.append(prop).append(" = ").append(value).append("\n");

            }

        }
        return str.toString();
    }

    public List<Parameter> getAsParameters() {
        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter().withKey("test.env.name").withName("test.env.name").withValue(testEnvironment.getEnvironmentName()));
        params.add(new Parameter().withKey("test.env.url").withName("test.env.url").withValue(testEnvironment.getUrl()));
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Property.class) && !field.isAnnotationPresent(Hide.class)) {
                String property = field.getAnnotation(Property.class).value();
                String value;
                try {
                    value = field.get(this).toString();
                } catch (Exception e) {
                    value = "";
                }

            }

        }
        return params;
    }

    public Capabilities getExtraCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        if (extraCapabilities != null) {
            String params[] = extraCapabilities.split(regex:",");
            for (String param : params) {
                String values[] = param.split(regex:"=", limit:2);
                capabilities.setCapability(values[0].trim, values[1].trim());

            }

        }
        return capabilities;
    }

    public String getHttpProxy() {
        return httpProxy;
    }

    public String getHttpsProxy() {
        return httpsProxy;
    }

    public boolean getAcceptSSLCerts() {
        return acceptSSLCerts;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Integer getThreadCount() {
        return threadCount;
    }

    public int getTestDefaultRetry() {
        return testDefaultRetry;
    }

    public String getScreenSize() {
        return screenSize;
    }

    public String getReportVersion() {
        return reportVersion;
    }

    public String getMailServer() {
        return mailServer;
    }

    public long getEmailTimeOut() {
        return emailTimeOut;
    }

    public boolean isInstallDrivers() {
        return installDrivers;
    }
}
