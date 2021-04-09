package com.feng.springboot;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName SubjectTest
 * @Description TODO
 * @Author fengxiaoxiao
 * @Date 2021/2/24 13:59
 * @Version 1.0
 */
public class SubjectTest {
	// 主题同时是两个元素，一个订阅者和一个可观察者。作为订阅者，主题可以用于发布来自多个可观察对象的事件。
	//并且由于它也是可观察的，因此可以将来自多个订阅者的事件重新发布为观察它的任何人的事件。
	//在下一个示例中，我们将观察观察者如何看到订阅后发生的事件：
	Integer subscriber1 = 0;
	Integer subscriber2 = 0;

	Observer<Integer> getFirstObserver() {
		return new Observer<Integer>() {
			private AtomicInteger atomicInteger = new AtomicInteger(0);
			@Override
			public void onSubscribe(Disposable d) {

			}

			@Override
			public void onNext(Integer value) {
				System.out.println("invoke first...." + atomicInteger.incrementAndGet());
				subscriber1 += value;
			}

			@Override
			public void onError(Throwable e) {
				System.out.println("error");
			}

			@Override
			public void onComplete() {

			}

		};
	}

	Observer<Integer> getSecondObserver() {
		return new Observer<Integer>() {
			private AtomicInteger atomicInteger = new AtomicInteger(0);
			@Override
			public void onSubscribe(Disposable d) {

			}

			@Override
			public void onNext(Integer value) {
				System.out.println("invoke second...." + atomicInteger.incrementAndGet());
				subscriber2 += value;
			}

			@Override
			public void onError(Throwable e) {
				System.out.println("error");
			}

			@Override
			public void onComplete() {
				System.out.println("complete...");
			}


		};
	}


	@Test
	public void subjectTest() {
		PublishSubject<Integer> subject = PublishSubject.create();

		subject.subscribe(getFirstObserver());
		subject.onNext(1);
		subject.onNext(2);
		subject.onNext(3);
		subject.subscribe(getSecondObserver());
		subject.onNext(4);
		//subject.onCompleted();
		subject.onComplete();
		System.out.println(subscriber1);
		System.out.println(subscriber2);
	/*	assertTrue(subscriber1 + subscriber2 == 14)*/
	}


	@Test
	public void resourceManagement() {
		// using操作允许我们将资源（例如JDBC数据库连接，网络连接或打开的文件）关联到我们的可观察对象。
		//在这里，我们在评论中介绍实现此目标所需要做的步骤，以及实现示例：
		String[] result = {""};
		Observable<Character> values = Observable.using(
				() -> "MyResource",
				r -> {
					return Observable.create(o -> {
						for (Character c : r.toCharArray()) {
							o.onNext(c);
						}
						o.onComplete();
					});
				},
				r -> System.out.println("Disposed: " + r)
		);
		values.subscribe(
				v -> result[0] += v,
				e -> result[0] += e
		);
		System.out.println(result[0]);
		/*assertTrue(result[0].equals("MyResource"));*/
	}
	// 在本文中，我们讨论了如何使用RxJava库以及如何探索其最重要的功能。
	//可以在Github上找到该项目的完整源代码，包括此处使用的所有代码示例。
}
