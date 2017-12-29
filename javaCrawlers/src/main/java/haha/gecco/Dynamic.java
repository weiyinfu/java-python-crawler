package haha.gecco;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.dynamic.DynamicGecco;

public class Dynamic {
public static void main(String[] args) {
    DynamicGecco.html()
            .gecco("https://github.com/{user}/{project}", "consolePipeline")
            .requestField("request").request().build()
            .stringField("user").requestParameter("user").build()
            .stringField("project").requestParameter().build()
            .stringField("title").csspath(".repository-meta-content").text(false).build()
            .intField("star").csspath(".pagehead-actions li:nth-child(2) .social-count").text(false).build()
            .intField("fork").csspath(".pagehead-actions li:nth-child(3) .social-count").text().build()
            .stringField("contributors").csspath("ul.numbers-summary > li:nth-child(4) > a").href().build()
            .register();

//开始抓取
    GeccoEngine.create().classpath("haha.gecco")
            .start("https://github.com/xtuhcy/gecco")
            .run();
}
}
