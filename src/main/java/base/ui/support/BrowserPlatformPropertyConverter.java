package base.ui.support;

import org.openqa.selenium.Platform;
import ru.qatools.properties.converters.Converter;

public class BrowserPlatformPropertyConverter implements Converter<Platform> {
    @Override
    public Platform convert(String from) throws Exception{
        return Platform.valueOf(from);
    }
}
