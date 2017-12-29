package haha.html2swing;

import haha.Util;
import org.apache.tools.ant.taskdefs.Exit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main extends JFrame {
WebDriver driver;
final int pad = 10;
final Map<String, Component> ids = new HashMap<>();

Component findElementById(String id) {
    return ids.get(id);
}

void initWebDriver() {
    if (System.getProperty("os.arch").equals("amd64")) {
        System.setProperty("webdriver.gecko.driver", "./drivers/geckodriver64.exe");
    } else {
        System.setProperty("webdriver.gecko.driver", "./drivers/geckodriver32.exe");
    }
    FirefoxOptions options = new FirefoxOptions();
    options.setHeadless(true);
    driver = new FirefoxDriver(options);
    driver.manage().window().maximize();
}

Rectangle conv(org.openqa.selenium.Rectangle rec) {
    return new Rectangle(rec.x, rec.y, rec.width, rec.height);
}

void inputHandler() {
    for (WebElement i : driver.findElements(By.tagName("input"))) {
        if (i.getAttribute("type").equals("text") == false) continue;
        JTextField textField = new JTextField(i.getAttribute("value"));
        if (Util.valid(i.getAttribute("id"))) {
            ids.put(i.getAttribute("id"), textField);
        }
        textField.setBounds(conv(i.getRect()));
        textField.setFont(new Font(i.getCssValue("font-name"), Font.BOLD, 18));
        this.add(textField);
    }
}

void labelHandler() {
    for (WebElement i : driver.findElements(By.className("label"))) {
        JLabel label = new JLabel(i.getText());
        label.setBounds(conv(i.getRect()));
        this.add(label);
    }
}

void sliderHandler() {
    for (WebElement i : driver.findElements(By.cssSelector("input"))) {
        if (i.getAttribute("type").equals("range") == false) continue;
        JSlider slider = new JSlider(
                Integer.parseInt(i.getAttribute("min")),
                Integer.parseInt(i.getAttribute("max")),
                Integer.parseInt(i.getAttribute("value")));
        slider.setBounds(conv(i.getRect()));
        this.add(slider);
    }
}

void progressHandler() {
    for (WebElement i : driver.findElements(By.cssSelector(".progressBar"))) {
        JProgressBar bar = new JProgressBar();
        bar.setBounds(conv(i.getRect()));
        this.add(bar);
    }
}

void buttonHandler() {
    for (WebElement i : driver.findElements(By.cssSelector("button"))) {
        JButton button = new JButton(i.getText());
        button.setBounds(conv(i.getRect()));
        this.add(button);
    }
}

Main(String url) {
    initWebDriver();
    this.setLayout(null);
    driver.get(url);
    inputHandler();
    labelHandler();
    sliderHandler();
    progressHandler();
    buttonHandler();
    driver.close();
    int w = 0, h = 0;
    for (Component i : this.getContentPane().getComponents()) {
        w = Math.max(w, i.getWidth() + i.getX());
        h = Math.max(h, i.getHeight() + i.getY());
    }
    this.setSize(w + pad, h + pad);
    this.setResizable(false);
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
}

public static void main(String[] args) throws IOException {
    Main frame = new Main(Main.class.getResource("/html2swing/two.html").toString());
    frame.setVisible(true);
}
}
