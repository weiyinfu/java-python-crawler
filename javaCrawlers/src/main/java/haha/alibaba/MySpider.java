package haha.alibaba;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequest;
import cn.edu.hfut.dmic.webcollector.plugin.ram.RamCrawler;
import com.google.gson.JsonObject;
import haha.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class MySpider extends RamCrawler {
String keyword;
String companyUrl;
String goodsUrl;
MyDao myDao;
long begTime;
long endTime;
/*********爬取进度********/
int totalRequestCount = 0;
int finishedRequestCount = 0;

/*****************/

public MySpider(String keyword,
                String companyUrl,
                String goodsUrl,
                MyDao myDao,
                int delay) {
    super(false);
    myDao.rebuildTable();
    this.keyword = keyword;
    this.companyUrl = companyUrl;
    this.goodsUrl = goodsUrl;
    if (Util.valid(companyUrl))
        addSeed(formListURL(1), "first");
    if (Util.valid(goodsUrl))
        addSeed(formGoodsListURL(1, 0), "goodsListFirst");
    //当使用selenium时，这些参数全部失效了
    conf.setConnectTimeout(8000);
    conf.setReadTimeout(8000);
    conf.setMaxRedirect(2);
    //这个参数表示等待线程运行时间
    //conf.setExecuteInterval(10000);
    //这个参数表示爬虫爬取时间间隔
    conf.setExecuteInterval(delay);
}

@Override
public void start(int depth) throws Exception {
    begTime = System.currentTimeMillis();
    super.start(depth);
}

String formListURL(int begPage) {
    return URLUtil.parse(companyUrl)
            .put("beginPage", begPage + "")
            .tos();
}

String formGoodsListURL(int begPage, int startIndex) {
    if (startIndex == 0) return URLUtil.parse(goodsUrl).put("beginPage", begPage + "").tos();
    else if (startIndex == 20)
        return URLUtil.parse("https://s.1688.com/selloffer/rpc_async_render.jsonp?keywords=GPU&button_click=top&earseDirect=false&n=y&beginPage=3&uniqfield=pic_tag_id&templateConfigName=marketOfferresult&offset=0&pageSize=60&asyncCount=20&startIndex=20&async=true&enableAsync=true&rpcflag=new&_pageName_=market&callback=jQuery183023711953996449164_1514246954334&_=1514246957381")
                .put("beginPage", begPage + "")
                .put("startIndex", "20").tos();
    else if (startIndex == 40)
        return URLUtil.parse("https://s.1688.com/selloffer/rpc_async_render.jsonp?keywords=GPU&button_click=top&earseDirect=false&n=y&beginPage=3&uniqfield=pic_tag_id&templateConfigName=marketOfferresult&offset=0&pageSize=60&asyncCount=20&startIndex=20&async=true&enableAsync=true&rpcflag=new&_pageName_=market&callback=jQuery183023711953996449164_1514246954334&_=1514246957381")
                .put("beginPage", begPage + "")
                .put("startIndex", "40").tos();
    throw new RuntimeException("cannot decide the page");
}

String formCompanyURL(CrawlDatum it) {
    try {
        return new URLDescription(it.meta("主页") + "/page/offerlist.htm")
                .put("keywords", URLEncoder.encode(keyword, "gbk"))
                .tos();
    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    }
    return null;
}

void list(Page page, CrawlDatums next) {
    Document doc = page.doc();
    CrawlDatum parent = page.crawlDatum();
    Elements companyItems = doc.select(".company-list-item");
    for (Element co : companyItems) {
        CrawlDatum it = new CrawlDatum();
        Element a = co.select(".list-item-title .list-item-title-text").get(0);
        it.meta("名称", a.text());
        it.meta("页码", parent.meta("页码"));
        String href = a.attr("href");
        if (href.contains("?")) {
            it.meta("主页", href.substring(0, href.indexOf('?')));
        }
        it.meta("主页", href);
        for (Element i : co.select(".list-item-detail .detail-field-name")) {
            String value = i.nextElementSibling().text().trim();
            String key = i.text().replace(':', ' ').trim();
            if (Company.fields.contains(key)) {
                it.meta(key, value);
            }
        }
        if (it.meta("主页").startsWith("https://www.1688.com/company/")) {
            //特殊公司,会发生多次重定向
            myDao.insertBad(it.meta("主页"));
            continue;
        }
        it.url(formCompanyURL(it));
        it.type("company");
        it.meta("Referer", page.url());
        next.add(it);
    }
}

void goodslist(Page page, CrawlDatums next) {
    Document doc = page.doc();
    CrawlDatum parent = page.crawlDatum();
    Elements a = doc.select("#sm-offer-list .sm-offer-companyName");
    for (Element i : a) {
        CrawlDatum it = new CrawlDatum();
        it.meta("名称", i.text());
        String href = i.attr("href");
        if (href.contains("?")) {
            it.meta("主页", href.substring(0, href.indexOf('?')));
        }
        it.meta("主页", href);
        it.meta("页码", parent.meta("页码"));
        if (it.meta("页码") == null) {
            System.out.println(parent.meta());
            System.exit(-1);
        }
        if (it.meta("主页").startsWith("https://www.1688.com/company/")) {
            //特殊公司,会发生多次重定向
            myDao.insertBad(it.meta("主页"));
            continue;
        }
        it.url(formCompanyURL(it));
        it.type("company");
        it.meta("Referer", page.url());
        next.add(it);
    }
}

void pipeline(JsonObject it) {
    System.out.println(it.toString());
    myDao.insert(it);
}

@Override
public void visit(Page page, CrawlDatums next) {
    finishedRequestCount++;
    if (page.matchType("first")) {
        String res = Util.reGetsFirst("共找到(\\d+)条符", page.html(), 1);
        int totalCount = res == null ? 0 : Integer.parseInt(res);
        totalCount = Math.min(3000, totalCount);
        int totalPage = (int) Math.ceil(totalCount / 30.0);
        totalRequestCount += totalCount + totalPage;
        for (int i = 1; i <= totalPage; i++) {
            CrawlDatum ne = new CrawlDatum(formListURL(i), "list");
            ne.meta("页码", "公司<" + i + ">");
            ne.meta("Referer", page.url());
            next.add(ne);
        }
        finishedRequestCount++;
        page.crawlDatum().meta("页码", "公司<1>");
        list(page, next);
    } else if (page.matchType("goodsListFirst")) {
        String res = Util.reGetsFirst("共<em>(\\d+)</em>件相关产品", page.html(), 1);
        int goodsCount = res == null ? 1 : Integer.parseInt(res);
        goodsCount = Math.min(goodsCount, 100 * 60);
        int pageCount = (int) Math.ceil(goodsCount / 60.0);
        totalRequestCount += goodsCount + pageCount;
        for (int i = 1; i <= pageCount; i++) {
            CrawlDatum ne = new CrawlDatum(formGoodsListURL(i, 0), "goodslist");
            ne.meta("页码", "商品<" + i + ">");
            ne.meta("Referer", page.url());
            next.add(ne);
            if (goodsCount > i * 60 + 20) {
                ne = new CrawlDatum(formGoodsListURL(i, 20), "goodslist");
                ne.meta("页码", "goods" + i + "(20~40)");
                ne.meta("Referer", page.url());
                next.add(ne);
            }
            if (goodsCount > i * 60 + 40) {
                ne = new CrawlDatum(formGoodsListURL(i, 40), "goodslist");
                ne.meta("页码", "goods" + i + "(40~60)");
                ne.meta("Referer", page.url());
                next.add(ne);
            }
        }
        page.crawlDatum().meta("页码", "商品<1>");
        goodslist(page, next);
        finishedRequestCount++;
    } else if (page.matchType("company")) {
        String res = Util.reGetsFirst("共搜索到<em>\\s*(\\d+)\\s*</em>个符合条件的产品", page.html(), 1);
        int totalCount = res == null ? 0 : Integer.parseInt(res);
        CrawlDatum it = page.crawlDatum();
        it.meta("产品个数", totalCount);
        pipeline(it.meta());
    } else if (page.matchType("list")) {
        list(page, next);
    } else if (page.matchType("goodslist")) {
        goodslist(page, next);
    } else {
        System.err.println("unhandled page type" + page.crawlDatum().type());
        throw new RuntimeException("unhandled page type " + page.crawlDatum().type());
    }
}

@Override
public void afterStop() {
    Map<String, String> ma = new HashMap<>();
    ma.put("companyURL", companyUrl);
    ma.put("goodsURL", goodsUrl);
    ma.put("keyword", keyword);
    ma.put("totalRequestCount", totalRequestCount + "");
    ma.put("now", LocalDateTime.now().toString());
    myDao.insertDic(ma);
    endTime = System.currentTimeMillis();
}

@Override
public Page getResponse(CrawlDatum crawlDatum) throws Exception {
    try {
        System.out.println(crawlDatum.url());
        HttpRequest request = new HttpRequest(crawlDatum);
        request.setHeader("Accept", "*/*");
        request.setHeader("Accept-Encoding", "gzip, deflate, br");
        request.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        request.setHeader("Cache-Control", "no-cache");
        request.setHeader("Connection", "keep-alive");
        request.setHeader("Host", "arms-retcode.aliyuncs.com");
        request.setHeader("Pragma", "no-cache");
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("61.155.164.111", 3128));
//        request.setProxy(proxy);
        if (crawlDatum.meta().has("Referer"))
            request.setHeader("Referer", crawlDatum.meta("Referer"));
        request.setHeader("upgrade-insecure-requests", "1");
        request.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36");
        Page page = request.responsePage();
        return page;
    } catch (Exception e) {
        System.out.println(crawlDatum.url());
        e.printStackTrace();
    }
    return null;
}

@Override
public void execute(CrawlDatum datum, CrawlDatums next) throws Exception {
    Page page = requester.getResponse(datum);
    if (page == null) {
        myDao.insertBad(datum.url());
        return;
    }
    visitor.visit(page, next);
    if (autoParse && !regexRule.isEmpty()) {
        parseLink(page, next);
    }
    afterParse(page, next);
}

public static void main(String[] args) throws Exception {
    MyDao mydao = new MyDao();
    MySpider ali = new MySpider("GPU",
            "",
            "https://s.1688.com/selloffer/-475055.html?spm=b26110380.sw1688.0.0.Xx0ox0&featurePair=287%3A17354&cps=1&earseDirect=false&button_click=filtbar&n=y&filt=y&uniqfield=pic_tag_id",
            mydao, 0);
    ali.start(3);
}

}
