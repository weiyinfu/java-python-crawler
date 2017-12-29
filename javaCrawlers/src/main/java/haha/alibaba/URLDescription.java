package haha.alibaba;

import java.util.HashMap;
import java.util.Map;

public class URLDescription {
String url;
Map<String, String> params;

public URLDescription() {
    params = new HashMap<>(2);
}

public URLDescription(String url) {
    this();
    this.url = url;
}

public String getUrl() {
    return url;
}

public void setUrl(String url) {
    this.url = url;
}

public Map<String, String> getParams() {
    return params;
}

public void setParams(Map<String, String> params) {
    this.params = params;
}

public String tos() {
    StringBuilder builder = new StringBuilder();
    builder.append(url).append("?");
    boolean first = true;
    for (String k : params.keySet()) {
        if (first) {
            first = false;
        } else {
            builder.append("&");
        }
        builder.append(k);
        String v = params.get(k);
        if (v.length() > 0) {
            builder.append("=" + v);
        }
    }
    return builder.toString();
}

public URLDescription put(String k, String v) {
    this.params.put(k, v);
    return this;
}

@Override
public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(url + "\n");
    for (String k : params.keySet()) {
        builder.append("\t" + k + ":" + params.get(k) + "\n");
    }
    builder.append("\n");
    return builder.toString();
}
}
