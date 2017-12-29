package haha;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
static public String reGetsFirst(String pattern, String s, int group) {
    Pattern p = Pattern.compile(pattern);
    Matcher match = p.matcher(s);
    boolean res = match.find();
    if (res)
        return match.group(group);
    else return null;
}

static public List<String> reGetsAll(String pattern, String s, int group) {
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(s);
    int i = 0;
    List<String> ans = new ArrayList<String>();
    while (true) {
        boolean find = m.find(i);
        if (!find) break;
        ans.add(m.group(group));
        i = m.end();
    }
    return ans;
}

static public boolean valid(String s) {
    if (s == null) return false;
    if (s.trim().length() == 0) return false;
    return true;
}

public static String getResourcePath(String res) {
    String s = Util.class.getResource(res).getPath();
    if (s.charAt(2) == ':') {
        return s.substring(1);
    }
    return s;
}

public static void main(String[] args) {
    Properties p = System.getProperties();
    p.forEach((x, y) -> {
        System.out.println(x + "=" + y);
    });
}
}
