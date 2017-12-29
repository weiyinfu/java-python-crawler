package haha.alibaba;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Company {
public static List<String> fieldList = Arrays.asList(
        "名称", "主页", "主营产品", "所在地", "员工人数", "经营模式", "工艺类型",
        "加工方式", "产房面积", "产品个数", "累计成交数", "累计买家数", "重复采购率",
        "页码"
);
public static HashSet<String> fields = new HashSet<>(fieldList);

public static void main(String[] args) {
    System.out.println(fields.contains("累计买家数"));
}
}
