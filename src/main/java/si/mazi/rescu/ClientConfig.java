package si.mazi.rescu;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class ClientConfig {
    private final Map<Class<? extends Annotation>, Params> paramsMap = new HashMap<Class<? extends Annotation>, Params>();

    public ClientConfig add(Class<? extends Annotation> paramType, String paramName, Object paramValue) {
        Params params = paramsMap.get(paramType);
        if (params == null) {
            params = Params.of();
            paramsMap.put(paramType, params);
        }
        params.add(paramName, paramValue);
        return this;
    }

    public Map<Class<? extends Annotation>, Params> getParamsMap() {
        return paramsMap;
    }
}
