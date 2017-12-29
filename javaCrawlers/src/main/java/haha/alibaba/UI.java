package haha.alibaba;

import haha.Util;
import org.openqa.selenium.TimeoutException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

public class UI extends JFrame {

JTextField providerUrlField, goodsUrlField, keywordField;
MySpider spider;
Timer timer;
JProgressBar progressBar;
JLabel progressInfoLabel;
JLabel infoLabel;
JButton startButton;
JSlider delaySlider;
java.util.List<JTextField> inputEntrys;
Thread crawlThread;
MyDao myDao = new MyDao();
boolean useSelenium = true;
SeleniumRequester seleniumRequester;

public UI() {
    //全局初始化
    Container container = getContentPane();
    container.setLayout(null);
    Font font = new Font("微软雅黑", Font.BOLD, 20);
    this.setFont(font);
    container.setSize(600, 300);
    this.setSize(600, 400);
    setTitle("阿里巴巴供应商产品个数");

    //label输入框
    JLabel label = new JLabel("供应商URL");
    label.setBounds(10, 10, 200, 50);
    label.setFont(font);
    container.add(label);

    JTextField field = new JTextField();
    providerUrlField = field;
    field.setFont(font);
    field.setBounds(200, 10, 350, 50);
    container.add(field);

    label = new JLabel("商品URL");
    label.setFont(font);
    label.setBounds(10, 60, 200, 50);
    container.add(label);

    field = new JTextField();
    goodsUrlField = field;
    field.setBounds(200, 60, 350, 50);
    field.setFont(font);
    container.add(field);

    //设置爬虫爬取时间间隔
    label = new JLabel("爬虫休息间隔(秒)");
    label.setBounds(10, 110, 200, 50);
    label.setFont(font);
    container.add(label);

    delaySlider = new JSlider(0, 60);
    delaySlider.setValue(5);
    delaySlider.setPaintLabels(false);
    delaySlider.setMajorTickSpacing(1);
    delaySlider.setPaintTicks(true);
    delaySlider.setPaintTrack(true);
    delaySlider.setValueIsAdjusting(true);
    delaySlider.setMajorTickSpacing(10);
    delaySlider.setPaintLabels(true);
    delaySlider.setBounds(200, 110, 350, 50);
    container.add(delaySlider);

    label = new JLabel("关键词");
    label.setFont(font);
    label.setBounds(10, 160, 200, 50);
    container.add(label);

    field = new JTextField();
    field.setFont(font);
    keywordField = field;
    field.setBounds(200, 160, 350, 50);
    container.add(field);


    label = new JLabel();
    infoLabel = label;
    label.setForeground(Color.red);
    label.setFont(new Font("楷体", Font.ITALIC, 18));
    label.setBounds(10, 240, 290, 50);
    container.add(label);

    //启动爬虫按钮
    JButton button = new JButton("启动爬虫");
    startButton = button;
    button.setBounds(300, 240, 250, 50);
    button.setFont(new Font("隶书", Font.BOLD, 20));
    button.addActionListener(listenStart);
    container.add(button);

    //进度条
    progressBar = new JProgressBar(0, 100);
    progressBar.setBounds(10, 300, 550, 30);
    progressBar.setStringPainted(true);
    container.add(progressBar);

    progressInfoLabel = new JLabel();
    progressInfoLabel.setBounds(50, 350, 400, 20);
    container.add(progressInfoLabel);

    //进度条定时更新
    timer = new Timer(100, updateProgress);


    inputEntrys = Arrays.asList(providerUrlField, keywordField, goodsUrlField);

    inputEntrys.forEach(i -> i.addKeyListener(new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent e) {
            super.keyTyped(e);
            infoLabel.setText("");
        }
    }));

    Dimension sz = Toolkit.getDefaultToolkit().getScreenSize();
    this.setLocation((sz.width - this.getWidth()) / 2, (sz.height - this.getHeight()) / 2);
    addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            if (useSelenium) {
                seleniumRequester.close();
            }
        }
    });
    this.setResizable(false);


    if (useSelenium) {
        seleniumRequester = new SeleniumRequester();
        try {
            seleniumRequester.driver.get("https://login.1688.com/member/signin.htm");
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}

void disableInput() {
    inputEntrys.forEach(i -> i.setEnabled(false));
    startButton.setEnabled(false);
    delaySlider.setEnabled(false);
}

void enableInput() {
    inputEntrys.forEach(i -> i.setEnabled(true));
    startButton.setText("启动爬虫");
    progressInfoLabel.setText("");
    startButton.setEnabled(true);
    delaySlider.setEnabled(true);
}

private final ActionListener listenStart = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        String providerUrl = providerUrlField.getText();
        String goodsUrl = goodsUrlField.getText();
        String keyword = keywordField.getText();
        int delay = delaySlider.getValue();
        if (useSelenium) {
            java.util.List<String> urls = seleniumRequester.getURLs();
            for (String url : urls) {
                if (url.startsWith("https://s.1688.com/selloffer")) {
                    goodsUrl = url;
                    goodsUrlField.setText(goodsUrl);
                } else if (url.startsWith("https://s.1688.com/company")) {
                    providerUrl = url;
                    providerUrlField.setText(providerUrl);
                }
            }
        }
        if (!Util.valid(keyword)) {
            infoLabel.setText("必须输入关键字");
            return;
        }
        if (!(Util.valid(providerUrl) || Util.valid(goodsUrl))) {
            infoLabel.setText("必须输入公司URL或者商品URL");
            return;
        }
        disableInput();
        spider = new MySpider(keyword, providerUrl, goodsUrl, myDao, delay * 1000);
        if (useSelenium) {
            spider.setThreads(1);
            spider.setRequester(seleniumRequester);
        }
        crawlThread = new Thread(() -> {
            try {
                timer.start();
                spider.start(3);
                progressBar.setValue(progressBar.getMaximum());
                myDao.export();
                timer.stop();
                enableInput();
                System.exit(0);
            } catch (Exception e1) {
                timer.stop();
                e1.printStackTrace();
            }
        });
        crawlThread.start();
        timer = new Timer(500, updateProgress);
    }
};

String format(long milliSeconds) {
    long second = milliSeconds / 1000 % 60;
    long minute = (milliSeconds / 1000 / 60) % 60;
    long hour = milliSeconds / 1000 / 60 / 60;
    StringBuilder builder = new StringBuilder(10);
    if (hour > 0) builder.append(hour).append("小时");
    if (minute > 0) builder.append(minute).append("分");
    return builder.append(second).append("秒").toString();
}

ActionListener updateProgress = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (spider.finishedRequestCount > 2) {
            long used = System.currentTimeMillis() - spider.begTime;
            long left = (long) (used * (spider.totalRequestCount - spider.finishedRequestCount) * 1.0 / spider.finishedRequestCount);
            progressInfoLabel.setText(String.format("%d/%d 已用时间%s 还需%s", spider.finishedRequestCount, spider.totalRequestCount, format(used), format(left)));
        }
        progressBar.setMaximum(spider.totalRequestCount);
        progressBar.setValue(spider.finishedRequestCount);
    }
};

public static void main(String[] args) {
    UI ui = new UI();
    ui.setVisible(true);
}
}
