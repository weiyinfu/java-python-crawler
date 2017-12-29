package haha.alibaba;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.Requester;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SeleniumRequester implements Requester {
WebDriver driver;


void chromeInit() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--start-maximized");
//    System.setProperty("webdriver.chrome.driver","./drivers/chromedriver.exe");
    options.setBinary("./drivers/chromedriver.exe");

    driver = new ChromeDriver(options);
}

void firefoxInit() {
    if (System.getProperty("os.arch").equals("amd64")) {
        System.setProperty("webdriver.gecko.driver", "./drivers/geckodriver64.exe");
    } else {
        System.setProperty("webdriver.gecko.driver", "./drivers/geckodriver32.exe");
    }
    FirefoxOptions options = new FirefoxOptions();
    //加上这句话会导致程序很快退出
    //options.setPageLoadStrategy(PageLoadStrategy.NONE);
//    options.addArguments("-profile", Paths.get("./myprofile").toAbsolutePath().toString());
    options.setAcceptInsecureCerts(true);
    options.addArguments("-connect-existing");
    driver = new FirefoxDriver(options);
    driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
    driver.manage().window().maximize();
}

SeleniumRequester() {
    firefoxInit();
}

@Override
public Page getResponse(CrawlDatum crawlDatum) throws Exception {
    try {
        driver.navigate().to(crawlDatum.url());
        Page ans = new Page(crawlDatum, 200, "text/html", null);
        ans.html(driver.getPageSource());
        return ans;
    } catch (TimeoutException e) {
        e.printStackTrace();
        Page ans = new Page(crawlDatum, 200, "text/html", null);
        ans.html(driver.getPageSource());
        return ans;
    } catch (RuntimeException e) {
        e.printStackTrace();
    }
    return null;
}


List<String> getURLs() {
    List<String> urls = driver.getWindowHandles()
            .stream()
            .map(x -> driver.switchTo().window(x).getCurrentUrl().trim())
            .collect(Collectors.toList());
    return urls;
}

void close() {
    if (driver != null) {
        driver.close();
    }
}

public static void main(String[] args) throws InterruptedException {
    SeleniumRequester sel = new SeleniumRequester();
//    sel.driver.get("https://sq2016.1688.com/page/offerlist.htm?keywords=GPU");
    sel.driver.get("https://login.1688.com/member/signin.htm");
    Thread.sleep(20000);
    sel.getURLs().forEach(System.out::println);
}
}
