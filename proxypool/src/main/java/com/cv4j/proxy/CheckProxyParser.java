package com.cv4j.proxy;

import com.cv4j.proxy.site.*;
import com.cv4j.proxy.site.xicidaili.XicidailiProxyListPageParser;

import java.util.HashMap;
import java.util.Map;

public class CheckProxyParser {

    public static void main(String[] args) {
        System.out.println("Start...");

        Map<String, Class> proxyMap = new HashMap<>();
        // 不可用
        //proxyMap.put("http://www.xicidaili.com/nn/1.html", XicidailiProxyListPageParser.class);
        // 网址可访问，没有可用的代理
        //proxyMap.put("https://proxy.mimvp.com/", MimvpProxyListPageParser.class);
        // http://www.mogumiao.com/web 已更改为http://www.moguproxy.com/
        //proxyMap.put("http://www.moguproxy.com/", MogumiaoProxyListPageParser.class);
        // 可用，但是需要修改table里的xpath
        proxyMap.put("http://www.goubanjia.com/", GoubanjiaProxyListPageParser.class);
        // 不可用
        //proxyMap.put("http://m.66ip.cn/2.html", M66ipProxyListPageParser.class);
        //  网址可访问，没有可用的代理
        //proxyMap.put("http://www.data5u.com/", Data4uProxyListPageParser.class);
        // 可用，但是需要修改table里的xpath  没有高匿代理
        proxyMap.put("https://list.proxylistplus.com/Fresh-HTTP-Proxy-List-1", ProxyListPlusProxyListPageParser.class);
        // 可用
        proxyMap.put("http://www.ip3366.net/", Ip3366ProxyListPageParser.class);//TODO gb2312如何处理？
        // 不可用
        //proxyMap.put("http://www.feilongip.com/", FeilongipProxyListPageParser.class);
        // 可用
        proxyMap.put("http://proxydb.net/", ProxyDbProxyListPageParser.class);
        // 不可用
        proxyMap.put("http://www.xiaohexia.cn/", XiaoHeXiaProxyListPageParser.class);

        ProxyPool.proxyMap = proxyMap;
        ProxyManager proxyManager = ProxyManager.get();
        proxyManager.start();
        System.out.println("大小为" + ProxyPool.proxyList.size());
        ProxyPool.proxyList.stream().forEach(System.out::println);
        System.out.println("End...");
    }
}
