import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
static WebDriver webDriver;
static Path targetFolder = Paths.get(System.getProperty("user.home")).resolve("desktop/tianyancha-html");
static BufferedWriter log;

static void sleep(int seconds) {
    try {
        Thread.sleep(1000L * seconds);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}


static List<String> companyNames;

static {
    try {
        companyNames = Files.readAllLines(Paths.get("company.txt"), Charset.forName("utf8"));
        if (Files.notExists(targetFolder)) {
            Files.createDirectory(targetFolder);
        }
        log = Files.newBufferedWriter(Paths.get("log.txt"));
    } catch (IOException e) {
        e.printStackTrace();
    }
}

static WebElement findElement(By by) {
    try {
        return webDriver.findElement(by);
    } catch (Exception e) {
        info("没有找到元素", by);
        e.printStackTrace();
    }
    return null;
}

static void info(Object... args) {
    StringBuilder builder = new StringBuilder();
    for (Object i : args) {
        builder.append(i.toString() + " ");
    }
    System.out.println(builder.toString());
    try {
        log.write(builder.toString() + "\n");
        log.flush();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

static void handle(String companyName) {
    webDriver.get("https://www.tianyancha.com/");
    String windowHandle = webDriver.getWindowHandle();
    sleep(5);//等待一段时间，等页面加载完全
    WebElement inputBox = findElement(By.id("home-main-search"));
    inputBox.sendKeys(companyName);
    sleep(5);
    inputBox.submit();//点击搜索
    sleep(5);
    WebElement firstCompany = findElement(By.cssSelector(".sv-search-company"));
    if (firstCompany == null) {
        info("没有找到", companyName, "的公司信息");
        return;
    }
    firstCompany.click();//点击第一个公司的链接
    sleep(10);//等待页面加载3s

    for (String i : webDriver.getWindowHandles()) {
        if (i.equals(windowHandle)) continue;
        webDriver.switchTo().window(i);
    }
    String s = webDriver.getPageSource();
    try {
        BufferedWriter cout = Files.newBufferedWriter(targetFolder.resolve(companyName + ".html"));
        cout.write(s);
        cout.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
    webDriver.close();//关闭公司主页窗口
    webDriver.switchTo().window(windowHandle);
}

public static void main(String[] args) throws IOException {
    webDriver = new FirefoxDriver();
    try {
        webDriver.manage().window().maximize();
        for (String companyName : companyNames) {
            companyName = companyName.trim();
            if (companyNames.size() == 0) continue;
            if (Files.exists(targetFolder.resolve(companyName + ".html"))) {
                info(companyName, "已经存在了，无需爬取");
                continue;
            }
            info("即将处理：", companyName);
            handle(companyName);
            info("处理", companyName, "结束");
            sleep(5);
        }
        info("程序运行结束");
        sleep(10);
    } catch (Exception e) {
        e.printStackTrace();
    }
    webDriver.close();
}
}
