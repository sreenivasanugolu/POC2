package base.ui.support;

import ru.qatools.properties.converters.Converter;

public class EnvironmentPropertyConverter implements Converter<EnvironmentsSetup.Environment> {
    @Override
    public  EnvironmentsSetup.Environment convert(String from) throws Exception {
        String[] envConfig = from.trim().split(regex: ":");
        String config = envConfig[0].trim();
        String env = envConfig[1].trim();
        EnvironmentsSetup envSetup = new EnvironmentsSetup().fromResource(config);
        return envSetup.getEnvironment(env);

    }
}
