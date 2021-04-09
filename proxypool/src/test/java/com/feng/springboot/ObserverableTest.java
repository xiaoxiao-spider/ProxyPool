package com.feng.springboot;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @ClassName ObserverableTest
 * @Description TODO
 * @Author fengxiaoxiao
 * @Date 2021/2/25 8:55
 * @Version 1.0
 */
@Slf4j
public class ObserverableTest {

	@Test
	public void threadChange() {
		/*log.info("debug", Thread.currentThread().getName());
		Observable.empty()
				.doOnComplete(new Action0() {
					@Override
					public void call() {
						Log.i("debug", Thread.currentThread().getName());
					}
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doOnCompleted(new Action0() {
					@Override
					public void call() {
						log.info("debug", Thread.currentThread().getName());
					}
				})
				.subscribe();
	*/}
}
