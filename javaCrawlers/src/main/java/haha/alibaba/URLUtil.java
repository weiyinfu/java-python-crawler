package haha.alibaba;

public class URLUtil {
public static URLDescription parse(String url) {
    url = url.trim();
    URLDescription des = new URLDescription();
    if (url.contains("?")) {
        String[] res = url.split("\\?");
        des.url = res[0];
        if (res[1].length() > 1) {
            String[] p = res[1].split("&");
            for (String kv : p) {
                if (kv.contains("=") && kv.indexOf('=') != kv.length() - 1) {
                    String[] kvs = kv.split("=");
                    des.params.put(kvs[0], kvs[1]);
                } else {
                    if (kv.endsWith("=")) {
                        kv = kv.substring(0, kv.length() - 1);
                    }
                    des.params.put(kv, "");
                }
            }
        }
    }
    return des;
}

public static void main(String[] args) {
    String[] test = {
            "http://www.baidu.com/?q=234&baga&one=&q=23"
    };
    for (String i : test) {
        System.out.println(parse(i).tos());
    }
    System.out.println(new URLDescription("haha.com").put("oen", "1")
            .put("two", "2").tos());
}
}
