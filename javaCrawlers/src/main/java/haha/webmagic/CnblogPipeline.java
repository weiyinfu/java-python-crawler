package haha.webmagic;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CnblogPipeline implements Pipeline {
Path folder = Paths.get("cnblog");

public CnblogPipeline() {
    if (Files.notExists(folder)) {
        try {
            Files.createDirectory(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public void process(ResultItems resultItems, Task task) {
    System.out.println(resultItems.getRequest().getUrl());
    if (resultItems.getRequest().getUrl().startsWith("http://www.cnblogs.com/weidiao/p/")) {
        String title = resultItems.get("title");
        System.out.println(title);
        String content = resultItems.get("content");
        BufferedWriter cout = null;
        try {
            cout = Files.newBufferedWriter(folder.resolve(title+".html"));
            cout.write(content);
            cout.close();
        } catch (IOException e) {
            System.out.println("baga");
            e.printStackTrace();
        }
    }
}

}
