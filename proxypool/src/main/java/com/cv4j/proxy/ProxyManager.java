package com.cv4j.proxy;

import com.cv4j.proxy.domain.Proxy;
import com.cv4j.proxy.http.HttpManager;
import com.cv4j.proxy.task.ProxyPageCallable;
import com.safframework.tony.common.utils.Preconditions;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by tony on 2017/10/25.
 */
@Slf4j
public class ProxyManager {

    private ProxyManager() {
    }

    public static ProxyManager get() {
        return ProxyManager.Holder.MANAGER;
    }

    private static class Holder {
        private static final ProxyManager MANAGER = new ProxyManager();
    }

    /**
     * 抓取代理，成功的代理存放到ProxyPool中
     */
    public void start() {
        /// RxJava 2在这两种源之间引入了明显的区别-背压感知源现在使用专用类Flowable表示。
        // Obserable可观察到的源不支持背压。因此，我们应该将其用于仅消耗而无法影响的资源。
        // 另外，如果我们要处理大量元素，则取决于Observable的类型，可能会出现两种与背压有关的情况。
        // 如果使用所谓的“冷可观察 cold observable”，事件会延迟发送，因此我们可以避免观察者溢出。
        //但是，当使用“热可观察 hot observable”对象时，即使消费者无法跟上，它也会继续发出事件
        Flowable.fromIterable(ProxyPool.proxyMap.keySet())
                // 在相应的操作符上调用Flowable的parallel()就会返回ParallelFlowable。
                // 举例： ParallelFlowable parallelFlowable = Flowable.range(1,100).parallel();
                // ParallelFlowable的from()方法，通过Publisher并以循环的方式在多个“轨道”（CPU数）上消费它。
                .parallel(ProxyPool.proxyMap.size())
                .map(new Function<String, List<Proxy>>() {
                    @Override
                    public List<Proxy> apply(String s) throws Exception {

                        try {
                            return new ProxyPageCallable(s).call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return new ArrayList<Proxy>();
                    }
                })
                .flatMap(new Function<List<Proxy>, Publisher<Proxy>>() {
                    @Override
                    public Publisher<Proxy> apply(List<Proxy> proxies) throws Exception {
                        if (Preconditions.isNotBlank(proxies)) {
                            List<Proxy> result = proxies
                                    .stream()
                                    .parallel()
                                    .filter(new Predicate<Proxy>() {
                                        @Override
                                        public boolean test(Proxy proxy) {
                                            // 测试抓取的代理是否可用
                                            HttpHost httpHost = new HttpHost(proxy.getIp(), proxy.getPort(), proxy.getType());
                                            boolean result = HttpManager.get().checkProxy(httpHost);
                                            if(result) log.info("checkProxy " + proxy.getProxyStr() +", "+result);
                                            return result;
                                        }
                                    }).collect(Collectors.toList());

                            return Flowable.fromIterable(result);
                        }

                        return Flowable.empty();
                    }
                })
				// ParallelFlowable遵循与Flowable相同的异步原理，因此parallel()本身不引入顺序源的异步消耗，只准备并行流。
                // 但是可以通过runOn(Scheduler)操作符定义异步。
                // 这一点跟Flowable很大不同，Flowable是使用subscribeOn、observeOn操作符。
                .runOn(Schedulers.io())
                // 在最后，如果已经使用了必要的并行操作，您可以通过ParallelFlowable.sequential()操作符返回到顺序流。
                .sequential()
                .subscribe(new Consumer<Proxy>() {
                    @Override
                    public void accept(Proxy proxy) throws Exception {

                        if (proxy!=null) {
                            log.info("accept " + proxy.getProxyStr());
                            proxy.setLastSuccessfulTime(new Date().getTime());
                            ProxyPool.proxyList.add(proxy);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        log.error("ProxyManager is error: "+throwable.getMessage());
                    }
                });
    }
}
