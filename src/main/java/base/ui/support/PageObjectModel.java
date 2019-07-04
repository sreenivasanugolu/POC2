package base.ui.support;

import com.sun.corba.se.impl.orb.ParserTable;
import datainstiller.data.DataAliases;
import datainstiller.data.DataPersistence;
import org.apache.commons.jexl3.JexlContext;
import org.testng.ITestContext;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.events.MakeAttachmentEvent;
import ru.yandex.qatools.allure.events.TestCaseEvent;
import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.DescriptionType;
import ru.yandex.qatools.allure.model.LabelName;
import ui.auto.core.context.PageComponentContext;
import ui.auto.core.pagecomponent.PageObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageObjectModel extends PageObject {

    private void resolveGlobalAliasses() {
        DataAliases global = PageComponentContext.getGlobalAliases();
        for (String aliasKey: global.keySet()) {
            String aliasValue = global.get(aliasKey);
            if(aliasValue != null && aliasValue.matches(regex: ".*\\$\\{[\\w-]+}.*")) {
                Pattern pat = Pattern.compile("\\$\\{[\\w-]+}");
                Matcher mat = pat.matcher(aliasValue);
                while(((Matcher) mat).find()){
                    String alias = mat.group();
                    String key = alias.replace(target: "${", replacement: "").replace(target:"}", replacement: "");
                    if(global.containsKey(key)){
                        String value = global.get(key);
                        if(value != null){
                            aliasValue = aliasValue.replace(alias, value);
                        }
                    }
                }
                global.put(aliasKey, aliasValue);

            }

        }
    }

    @Override
    protected  void initJexlcontext(JexlContext jexlContext){
        jexlContext.set("minutes", DateTimeFormatter.ofPattern("mm"));;
        jexlContext.set("hours", DateTimeFormatter.ofPattern("hh"));
        jexlContext.set("ampm", DateTimeFormatter.ofPattern("a"));
        jexlContext.set("day_month_year", DateTimeFormatter.ofPattern("d-M-yyyy"));
        jexlContext.set("time_stamp", DateTimeFormatter.ofPattern("yyyyMMddkkmmss-S"));
        String timeStampThreadId = DateTimeFormatter.ofPattern("yyMMddkkmmss").format(LocalDateTime.now()) + Thread.currentThread().getId();
        jexlContext.set("time_stamp_thread", timeStampThreadId);

        EnvironmentsSetup.Environment env = TestContext.getTestProperties().getTestEnvironment();
        if(env != null){
            jexlContext.set("env", env);
        }
    }

    private List<Label> getLables(DataPersistence data, String labelAlias, LabelName labelName){
        DataAliases aliases = data.getDataAliases();
        String label = aliases.get(labelAlias);
        List<label> labels = new ArrayList<>();
        if(label != null){
            for(String value: label.split(regex: ",")){
                ((ArrayList) labels).add(new Label().withName(labelName.value().withValue(value.trim())));
            }
        }
        return labels;
    }

    private void overwriteTestParameters(DataPersistence data){
        DataAliases aliases = data.getDataAliases();
        if(aliases == null) return;
        String name = aliases.get("test-name");
        String description = aliases.get("test-description");

        List<Label> labels = new ArrayList<>();
        labels.addAll(getLables(data, "test-features", LabelName.FEATURE));
        labels.addAll(getLables(data, "test-stories", LabelName.STORY));
        labels.addAll(getLables(data, "test-issues", LabelName.ISSUE));
        labels.addAll(getLables(data, "test-IDs", LabelName.TEST_ID));
        labels.addAll(getLables(data, "test-severity", LabelName.SEVERITY));

        Allure.LIFECYCLE.fire((TestCaseEvent) context -> {
            context.getLabels().addAll(labels);
            if(name != null){
                context.setName(name);
            }
            if(description != null){
                context.setDescription(new Description().withType(DescriptionType.MARKDOWN).withValue(description));
            }
        });

    }

    @Override
    public<T extends  DataPersistence> T fromResource(String resourceFilePath, boolean resolveAliases){
        T data = super.fromResource(resourceFilePath, resolveAliases);
        resolveGlobalAliasses();
        overwriteTestParameters(data);
        TestNGBase.attachDataSet(data, resourceFilePath);

        String aliases = getXstream().toXML(ITestContext.getGlobalAliases());
        MakeAttachmentEvent ev = new MakeAttachmentEvent(aliases.getBytes(), title: "Global Aliases : " + resourceFilePath, type: "text/xml");
        Allure.LIFECYCLE.fire(ev);
        return data;
    }

}
