package base.ui.support;

import ru.qatools.properties.converters.Converter;

public class BrowserTypePropertyConverter  implements Converter<WebDriverTypeEnum> {
    @Override
    public WebDriverTypeEnum convert(String from) throws Exception{
        return WebDriverTypeEnum.valueOf(from);
    }
}
