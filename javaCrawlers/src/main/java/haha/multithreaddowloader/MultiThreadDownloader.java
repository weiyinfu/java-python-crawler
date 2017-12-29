package haha.multithreaddowloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadDownloader {
class Worker extends Thread {
    Worker(int beg, int end, Path path, boolean isLast) {
        this.beg = beg;
        this.end = end;
        this.path = path;
        this.isLast = false;
    }

    int beg;
    int end;
    Path path;
    boolean isLast;

    @Override
    public void run() {
        try (OutputStream cout = Files.newOutputStream(path)) {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("Range", "bytes=" + beg + "-" + end);
            conn.setDoInput(true);
            conn.connect();
            InputStream cin = conn.getInputStream();
            byte[] buf = new byte[2048];
            while (true) {
                int cnt = cin.read(buf);
                if (cnt <= 0) break;
                cout.write(buf, 0, cnt);
            }
            cout.flush();
            if (workingWorkerCount.decrementAndGet() == 0) {
                merge(workers);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

String url = "http://mp4.vjshi.com/2017-12-18/422ded2944a95d6ca09752e04f687dd6.mp4";
final int MAX_THREAD__COUNT = 10;
final int MIN_THREAD_SIZE = 2048;
final String targetFile = "monkey.mp4";
final String tempDir = "dow";
AtomicInteger workingWorkerCount;
List<Worker> workers;
long begTime, endTime;

int getBlockSize(int sz) {
    int blockSize = sz / MAX_THREAD__COUNT;
    if (blockSize < MIN_THREAD_SIZE) blockSize = MIN_THREAD_SIZE;
    return blockSize;
}

int getResourceSize(String url) throws IOException {
    URLConnection conn = new URL(url).openConnection();
    conn.connect();
    int sz = conn.getContentLength();
    return sz;
}

MultiThreadDownloader(String url) throws IOException {
    begTime = System.currentTimeMillis();
    if (url != null)
        this.url = url;
    if (Files.notExists(Paths.get(tempDir))) {
        Files.createDirectory(Paths.get(tempDir));
    }
    int sz = getResourceSize(this.url);
    int blockSize = getBlockSize(sz);
    System.out.println("文件大小" + sz + " " + blockSize);
    List<Worker> a = new ArrayList<>((int) Math.ceil(sz / blockSize));
    for (int i = 0; i < sz; ) {
        int nowSize = blockSize;
        if (sz - i - blockSize < MIN_THREAD_SIZE) {
            nowSize = sz - i;
        }
        Worker worker = new Worker(i, i + nowSize, Paths.get(tempDir).resolve("" + i), i + nowSize == sz);
        a.add(worker);
        worker.start();
        i += nowSize;
    }
    workers = a;
    workingWorkerCount = new AtomicInteger(a.size());
}

void merge(List<Worker> workers) {
    try (OutputStream cout = Files.newOutputStream(Paths.get(targetFile))) {
        for (Worker w : workers) {
            byte[] content = Files.readAllBytes(w.path);
            cout.write(content, 0, w.isLast ? content.length : content.length - 1);
        }
        endTime = System.currentTimeMillis();
        System.out.println("总计用时" + (endTime - begTime));
    } catch (Exception e) {
        e.printStackTrace();
    }
}

public static void main(String[] args) throws IOException {
    new MultiThreadDownloader(null);
}
}
