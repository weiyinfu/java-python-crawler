package haha.crawler4j;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import haha.Util;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.RequestUserAgent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

public class Cnblog extends WebCrawler {

/**
 * 禁用掉一切shouldVisit，只使用visit中的getMyController.addSeed进行扩展
 */
@Override
public boolean shouldVisit(Page referringPage, WebURL url) {
    return false;
}

boolean first = true;

void first(Page page) throws UnsupportedEncodingException {
    System.out.println(page.getContentEncoding() + " encoding");
    System.out.println(page.getContentCharset() + " charset");
    System.out.println(page.getContentType() + " type");

    String content = new String(page.getContentData(), page.getContentCharset());
    Document doc = Jsoup.parse(content);
    String s = doc.select("#homepage_top_pager").html();
    int totalPage = Integer.parseInt(Util.reGetsFirst("共(\\d+)页", s, 1));
    totalPage = 3;
    for (int i = 1; i <= totalPage; i++)
        getMyController().addSeed("https://www.cnblogs.com/weidiao/default.html?page=" + i);
}

void list(Page page) throws UnsupportedEncodingException {
    String s = new String(page.getContentData(), page.getContentCharset());
    Document doc = Jsoup.parse(s);
    Elements res = doc.select(".postTitle2");
    for (Element i : res) {
        System.out.println(i.text());
    }
}

@Override
public void visit(Page page) {
    try {
        if (first) {
            first(page);
            first = false;
        } else {
            list(page);
        }
    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    }
}

public static void main(String[] args) throws Exception {
    //CrawlConfig是爬虫的全局配置
    CrawlConfig crawlConfig = new CrawlConfig();
    crawlConfig.setConnectionTimeout(3000);
    crawlConfig.setDefaultHeaders(Arrays.asList(
            new BasicHeader("one", "1"),
            new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6"),
            new BasicHeader("Connection", "keep-alive")));
    crawlConfig.setFollowRedirects(false);
    crawlConfig.setCrawlStorageFolder("crawl4j");
    crawlConfig.setMaxTotalConnections(4);
    //PageFetcher是页面下载器，WebController通过多个PageFetcher进行下载
    PageFetcher pageFetcher = new PageFetcher(crawlConfig);
    //RobotstxtConfig
    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
    robotstxtConfig.setUserAgentName("Baiduspider+(+http://www.baidu.com/search/spider.htm)");
    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
    CrawlController controller = new CrawlController(crawlConfig, pageFetcher, robotstxtServer);
    controller.addSeed("https://www.cnblogs.com/weidiao/default.html?page=2");
    controller.start(Cnblog.class, 2);
}
}
