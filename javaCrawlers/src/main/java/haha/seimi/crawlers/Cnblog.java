package haha.seimi.crawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.core.Seimi;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.http.HttpMethod;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import haha.Util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Crawler(name = "cnblog", useCookie = true, delay = 1, httpTimeOut = 1)
public class Cnblog extends BaseSeimiCrawler {

@Override
public String[] startUrls() {
    return null;
}

@Override
public List<Request> startRequests() {
    return Arrays.asList(new Request("https://www.cnblogs.com/weidiao/default.html?page=2", "first", HttpMethod.GET, null, null));
}

public void first(Response response) {
    Document html = Jsoup.parse(response.getContent());
    String s = html.select("#homepage_top_pager").html();
    int totalPage = Integer.parseInt(Util.reGetsFirst("共(\\d+)页",
            s, 1));
    for (int i = 1; i <= totalPage; i += 1) {
        this.push(new Request("https://www.cnblogs.com/weidiao/default.html?page=" + i, "list"));
    }
}

public void list(Response response) {
    Document html = Jsoup.parse(response.getContent());
    Elements titles = html.select(".postTitle2");
    for (Element i : titles) {
        System.out.println(i.text());
    }
}

@Override
public void start(Response response) {
    System.out.println("haha start");
}

public static void main(String[] args) {
    Seimi seimi = new Seimi();
    seimi.goRun(false, "cnblog");
}

}
