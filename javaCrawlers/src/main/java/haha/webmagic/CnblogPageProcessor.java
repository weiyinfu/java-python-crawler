package haha.webmagic;

import haha.Util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

public class CnblogPageProcessor implements PageProcessor {

private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

void parsePage(Page page) {
    System.out.println("parsepage");
    String s = page.getRawText();
    page.addTargetRequests(Util.reGetsAll("http://www.cnblogs.com/weidiao/p/\\d+.html", s, 0));
}

void parseBlog(Page page) {
    System.out.println("parseblog");
    Document doc = Jsoup.parse(page.getRawText());
    Elements post = doc.select(".post");
    String title = post.select(".postTitle").text();
    String content = post.select("#cnblogs_post_body").html();
    page.putField("title", title);
    page.putField("content", content);
}

void firstPage(Page page) {
    String res = page.getHtml().$("#homepage_top_pager").get();
    int totalPage = Integer.parseInt(Util.reGetsFirst("共(\\d+)+页", res, 1));
    for (int i = 1; i <= totalPage; i++) {
        page.addTargetRequest("https://www.cnblogs.com/weidiao/default.html?page=" + i);
    }
}

boolean first = true;

public void process(Page page) {
    if (first && page.getRequest().getUrl().equals("https://www.cnblogs.com/weidiao/default.html?page=2")) {
        firstPage(page);
        first = false;
    } else if (page.getRequest().getUrl().matches("https://www\\.cnblogs\\.com/weidiao/default\\.html\\?page=\\d+")) {
        parsePage(page);
    } else if (page.getRequest().getUrl().matches("http://www\\.cnblogs\\.com/weidiao/p/\\d+\\.html")) {
        parseBlog(page);
    } else {
        System.out.println("baga");
        throw new RuntimeException("cannot find router");
    }
}

public Site getSite() {
    return site;
}

public static void main(String[] args) {
    Spider.create(new CnblogPageProcessor())
            .addUrl("https://www.cnblogs.com/weidiao/default.html?page=2")
            .addPipeline(new CnblogPipeline())
            .thread(5)
            .run();
}
}