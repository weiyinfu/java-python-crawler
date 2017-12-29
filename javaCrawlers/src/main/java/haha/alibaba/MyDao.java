package haha.alibaba;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MyDao {
DruidDataSource source;

MyDao() {
    try {
        source = new DruidDataSource();
        Properties p = new Properties();
        p.load(MyDao.class.getResourceAsStream("/db.properties"));
        source.configFromPropety(p);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

public boolean rebuildTable() {
    try (Connection c = source.getConnection();
         Statement stmt = c.createStatement();
    ) {
        stmt.execute("drop table if EXISTS tao");
        stmt.execute(createTable());
        stmt.execute("drop table if EXISTS bad");
        stmt.execute("create table bad(url varchar(100))");
        stmt.execute("drop table  if EXISTS  dic");
        stmt.execute("create table dic(k varchar(30),v varchar(30))");
        createTable();
        return true;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}

String createTable() {
    StringBuilder builder = new StringBuilder("create table tao (");
    boolean first = true;
    for (String i : Company.fieldList) {
        if (first) first = false;
        else builder.append(",");
        builder.append(i + " varchar(100)");
    }
    builder.append(")");
    return builder.toString();
}

boolean insert(JsonObject it) {
    StringBuilder builder = new StringBuilder("insert into tao("
            + Company.fieldList.stream()
            .collect(Collectors.joining(",")) + ") values(");
    boolean first = true;
    for (String i : Company.fieldList) {
        if (first) first = false;
        else builder.append(",");
        JsonElement v = it.get(i);
        if (v == null)
            builder.append("null");
        else
            builder.append("'" + v.getAsString() + "'");
    }
    builder.append(")");
    String sql = builder.toString();
    try (DruidPooledConnection conn = source.getConnection();
         Statement stmt = conn.createStatement();) {
        stmt.execute(sql);
        return true;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}

boolean insertBad(String url) {
    try (DruidPooledConnection conn = source.getConnection();
         Statement stmt = conn.createStatement();) {
        stmt.execute("insert into bad values ('" + url + "')");
        return true;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}

boolean insertDic(Map<String, String> dic) {
    try (DruidPooledConnection conn = source.getConnection();
         Statement stmt = conn.createStatement();) {
        for (String i : dic.keySet()) {
            stmt.execute(String.format("insert INTO  dic values('%s','%s')", i, dic.get(i)));
        }
        return true;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}

JsonObject fakeData() {
    JsonObject json = new JsonObject();
    for (String i : Company.fieldList)
        json.addProperty(i, i);
    return json;
}

List<JSONObject> getBest() {
    try (DruidPooledConnection conn = source.getConnection();
         Statement stmt = conn.createStatement();
    ) {
        List<JSONObject> data = new ArrayList<>(50);
        ResultSet res = stmt.executeQuery("select * from tao order by cast(产品个数 as int ) desc limit 50");
        int cnt = 0;
        while (res.next()) {
            JSONObject it = new JSONObject();
            cnt += 1;
            it.put("排名", cnt);
            for (int i = 1; i <= res.getMetaData().getColumnCount(); i++) {
                it.put(res.getMetaData().getColumnName(i), res.getString(i));
            }
            data.add(it);
        }
        res.close();
        return data;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

void export() {
    MustacheFactory factory = new DefaultMustacheFactory();
    try (Reader cin = new InputStreamReader(UI.class.getResourceAsStream("/tem.html"), "utf8");
         DruidPooledConnection conn = source.getConnection();
         Statement stmt = conn.createStatement();
         BufferedWriter cout = Files.newBufferedWriter(Paths.get("index.html"), Charset.forName("utf8"), StandardOpenOption.CREATE);
    ) {
        Mustache m = factory.compile(cin, "nothing");
        Map<String, Object> scope = new HashMap<>();
        scope.put("title", "爬取结果");
        List<JSONObject> data = getBest();
        scope.put("data", data);
        ResultSet res = stmt.executeQuery("select * from dic");
        while (res.next()) {
            scope.put(res.getString(1), res.getString(2));
        }
        res.close();
        res = stmt.executeQuery("select * from bad");
        List<JSONObject> bad = new ArrayList<>();
        while (res.next()) {
            JSONObject it = new JSONObject();
            it.put("url", res.getString(1));
            bad.add(it);
        }
        res.close();
        scope.put("bad", bad);
        scope.put("badCount", bad.size());
        m.execute(cout, scope).flush();
        Desktop.getDesktop().open(Paths.get("index.html").toFile());
    } catch (Exception e) {
        e.printStackTrace();
    }
}

void test() {
    try (DruidPooledConnection conn = source.getConnection();
         Statement stmt = conn.createStatement();) {
        insert(fakeData());
        ResultSet res = stmt.executeQuery("select * from tao");
        while (res.next()) {
            for (int i = 1; i <= res.getMetaData().getColumnCount(); i++) {
                System.out.println(i + " " + res.getMetaData().getColumnName(i) + " " + res.getString(i));
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

public static void main(String[] args) throws Exception {
    MyDao myDao = new MyDao();
    myDao.export();
}
}
