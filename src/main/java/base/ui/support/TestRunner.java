package base.ui.support;

import org.apache.commons.io.FileUtils;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.reporters.Files;
import ru.yandex.qatools.allure.AllureMain;
import ru.yandex.qatools.allure.config.AllureConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TestRunner {
    private String resultsFolder;
    private String reportFolder;

    public TestRunner() {
        TestProperties props = TestContext.getTestProperties();
        System.setProperty("allure.results.directory", props.getResultsFolder().getAbsolutePath());
        System.setProperty("allure.testng.parameters.enabled", "false");
        AllureConfig config = new AllureConfig();
        resultsFolder = config.getResultsDirectory().getAbsolutePath();
        reportFolder = props.getReportFolder().getAbsolutePath();
        props.setReportUrlPatterns();
    }

    public int runTests(List<String> suites) throws IOException{
        if(suites.isEmpty()){
            throw new RuntimeException("Please provide suite file name Ex -Dtest.suites=<SUITE FILE PATH>");

        }
        for (String suite : suites) {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(suite);
            File file = new File(suite);
            if(is != null){
                FileUtils.copyInputStreamToFile(is, file);
            }else if(! file.exists()){
                throw new RuntimeException("suite file '" + suite + "' does not exist in the file system")

            }
        }
        TestNG testNG = new TestNG(b: false);
        testNG.addListener((ITestListener) new TestParameterValidator());
        testNG.setTestSuites(suites);
        testNG.setSuiteThreadPoolSize(1);
        testNG.run();
        return testNG.getStatus();
    }

    public void generateReport() throws IOException {
        String[] arguments = {resultsFolder, reportFolder};
        AllureMain.main(arguments);
        brandingMod();
    }

    private void brandingMod() throws IOException{
        String logo = "/61aae920ab2f8fe604ba57b135aa9919.png";
        String index = "/index.html";
        InputStream logoStr = Thread.currentThread().getContextClassLoader().getResourceAsStream(name: "rep" + logo);
        InputStream indexStr = Thread.currentThread().getContextClassLoader().getResourceAsStream(name: "rep" + index);
        Files.copyFile(logoStr, new File(pathname: reportFolder + logo));
        Files.copyFile(indexStr, new File(pathname: reportFolder + index));
        File report = new File(pathname: reportFolder + "/app.js");
        String content = Files.readFile(report);
        content = content.replace(target: "Allure", replacement: "<br/></br></br>Test Report");
        content = content.replace(target: "Latest", TestContext.getTestProperties().getReportVersion());
        Files.writeFile(content, report);
    }

    private void saveEnvironment(){
        TestProperties prop = TestContext.getTestProperties();
        Environment environment = new Environment().withName("Environment");
        environment.withParameter(prop.getAsParameters());
        XStream xStream = new XStream();
        xStream.addImplicitArray(Environment.class, fieldName: "parameter", itemName: "parameter");
        String xml=xStream.toXML(environment);
        xml = xml.replace(target: "<ru.yandex.qatools.commons.model.Environment>", replacement: "<qa:environment xmlns:qa=\"urn:model.commons.qatools.yandex.ru\">");
        xml = xml.replace(target: "</ru.yandex.qatools.commons.model.Environment>", replacement: "</qa:environment>");
        xml = xml.replace(target: " class=\"ru.yandex.qatools.commons.model.Parameter\"", replacement: "");
        File file = new File(pathname: resultsfolder + "/environment.xml");
        try{
            Files.writeFile(xml, file);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public  void deleteResultsFolder() throws IOException {
        File resFolder = new File(resultsFolder);
        if(resFolder.exists()){
            FileUtils.forceDelete(resFolder);
        }
    }

    public void deleteReportFolder() throws IOException{
        File resFolder = new File(reportFolder);
        if(resFolder.exists()){
            FileUtils.forceDelete(resFolder);
        }
    }
}
