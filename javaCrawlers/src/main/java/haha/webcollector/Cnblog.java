package haha.webcollector;

import cn.edu.hfut.dmic.webcollector.conf.Configuration;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import haha.Util;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Cnblog extends BreadthCrawler {

public Cnblog(String crawlPath, boolean autoParse) {
    super(crawlPath, autoParse);
    this.addSeed("https://www.cnblogs.com/weidiao/default.html?page=2", "first");
}

@Override
public void visit(Page page, CrawlDatums crawlDatums) {
    if (page.matchType("first")) {
        int totalpage = Integer.parseInt(Util.reGetsFirst("共(\\d+)页", page.select("#homepage_top_pager").html(), 1));
        totalpage = Math.min(totalpage, 3);
        for (int i = 1; i <= totalpage; i++) {
            crawlDatums.add("https://www.cnblogs.com/weidiao/default.html?page=" + i, "list");
        }
    } else if (page.matchType("list")) {
        Elements titles = page.select(".postTitle2");
        for (Element title : titles) {
            crawlDatums.add(title.select("a").attr("href"), "blog");
        }
    } else if (page.matchType("blog")) {
        String title = page.selectText(".postTitle2");
        System.out.println(title);
    } else {
        System.out.println(page.url());
        throw new RuntimeException("unhandled result");
    }
}

public static void main(String[] args) throws Exception {
    //autoparse表示是否让引擎来控制url的解析
    Cnblog blog = new Cnblog("webcollector", false);
    Configuration conf = blog.getConf();
    conf.setConnectTimeout(3000);
    blog.start(4);
}
}
