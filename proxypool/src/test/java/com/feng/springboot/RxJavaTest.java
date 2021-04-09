package com.feng.springboot;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertTrue;

/**
 * @ClassName RxJavaTest
 * @Description TODO
 * @Author fengxiaoxiao
 * @Date 2021/2/24 10:57
 * @Version 1.0
 */
public class RxJavaTest {

	private String result = "";

	private String[] letters = {"a", "b", "c", "d", "e", "f", "g"};

	private String[] titleList = {"t1", "t2"};

	List<Integer> numbers  = (List<Integer>) IntStream.rangeClosed(0,10).mapToObj(x -> x).collect(toList());

	@Test
	public void observable() {
		// 基本运算符just产生一个Observable，
		// 它在完成之前发出单个通用实例，即字符串“ Hello”。
		// 当我们想从Observable中获取信息时，我们实现一个观察者接口，然后在所需的Observable上调用subscription：
		Observable<String> observable = Observable.just("Hello");
		observable.subscribe(s -> System.out.println(s));

		//assertTrue(result.equals("Hello"));
	}

	@Test
	public void onNextOnErrorOnComplete() {

		Observable<String> observable = Observable.fromArray(letters);
		observable.subscribe(
				i -> result += i,  //OnNext
				Throwable::printStackTrace, //OnError
				() -> result += "_Completed" //OnCompleted
		);
		//assertTrue(result.equals("abcdefg_Completed"));
		System.out.println(result);
	}

	@Test
	public void map() {
		// map运算符通过对每个项目应用函数来转换Observable发出的项目。
		//假设有一个声明的字符串数组，其中包含字母表中的一些字母，并且我们要以大写方式打印它们：
		Observable<String> observable = Observable.fromArray(letters);
		observable.map(x -> x.toUpperCase())
				.subscribe(x -> result += x);

		System.out.println(result);
		// 每当我们得到嵌套的Observable时，均可使用flatMap来展平Observable。
		//有关map和flatMap之间差异的更多详细信息，请参见此处。
		//假设我们有一个从字符串列表中返回Observable <String>的方法。现在，我们将根据订阅者看到的内容，为新的Observable中的每个字符串打印标题列表：
		result = "";
		Observable.just("book1", "book2")
				.flatMap(s -> getTitle())
				.subscribe(l -> result += l);
		System.out.println(result);

	}

	Observable<String> getTitle() {
		return Observable.fromArray(titleList);
	}

	@Test
	public void scan() {
		//scan运算符将一个函数应用于Observable顺序发出的每个项目，并发出每个连续的值。
		String[] letters = {"a", "b"};
		// 它使我们能够在事件之间传递状态：
		Observable.fromArray(letters)
				.scan(new StringBuilder(), StringBuilder::append)
				.subscribe(total -> result += total.toString());
		// 三次  第一次 a  第二次ab  第三次abc
		System.out.println(result);
		//assertTrue(result.equals("aababc"));
	}


	@Test
	public void groupBy() {
		// group by 运算符可以使我们将输入Observable中的事件分类为输出类别。
		//假设我们创建了一个从0到10的整数数组，然后应用group by将其分为偶数和奇数类别：
		StringBuilder enev = new StringBuilder();
		StringBuilder odd = new StringBuilder();
		List<Integer> numbers  = (List<Integer>) IntStream.rangeClosed(0,10).mapToObj(x -> x).collect(toList());
		Observable.fromIterable(numbers)
				.groupBy(i -> 0 == (i % 2) ? "EVEN" : "ODD")
				.subscribe(group ->
						group.subscribe((number) -> {
							if (group.getKey().toString().equals("EVEN")) {
								enev.append(number);
							} else {
								odd.append(number);
							}
						})
				);

		System.out.println(enev);
		System.out.println(odd);
/*		assertTrue(EVEN[0].equals("0246810"));
		assertTrue(ODD[0].equals("13579"));*/
	}

	@Test
	public void filter() {
		// 运算符过滤器仅从可观察对象中通过谓词测试的那些项发出。
		//因此，让我们在整数数组中过滤奇数：
		Observable.fromIterable(numbers)
				.filter(x -> (x % 2) ==1)
				.subscribe(System.out::println);
	}

	@Test
	public void conditionalOperator() {
		// DefaultIfEmpty从源Observable发射项目，如果源Observable为空，则发射默认项目：
		Observable.empty()
				.defaultIfEmpty("default is empty")
				.subscribe(System.out::println);

		// 下面的代码发出字母“ a”的第一个字母，因为数组字母不为空，这是它在第一个位置中包含的内容：
		Observable.fromArray(letters)
				.defaultIfEmpty("haha")
				.firstElement()
				.subscribe(System.out::println);

		//
		Observable.fromIterable(numbers)
				.takeWhile(x -> x <= 5)
				.subscribe(System.out::println);
		// 当然，还有更多其他运算符可以满足我们的需求，例如Contain，SkipWhile，SkipUntil，TakeUntil等。
	}

	@Test
	public void connnectObservable() throws InterruptedException {
		// ConnectableObservable与普通的Observable相似，除了它在订阅时不会开始发出项目，
		// 而仅在将connect运算符应用于它时才开始发出项目。
		//这样，我们可以在Observable开始发出项目之前等待所有预期的观察者订阅Observable：
		String[] result = {""};
		ConnectableObservable<Long> connectable
				= Observable.interval(200, TimeUnit.MILLISECONDS).publish();
		connectable.subscribe(i -> result[0] += i);
	/*	assertFalse(result[0].equals("01"));*/
		System.out.println(result[0]);
		connectable.connect();
		Thread.sleep(700);
		System.out.println(result[0]);
		/*assertTrue(result[0].equals("01"));*/
	}

	@Test
	public void single() {
		// Single就像是一个Observable，而不是发出一系列值，而是发出一个值或错误通知。
		//使用此数据源，我们只能使用两种方法进行订阅：
		//OnSuccess返回一个Single，该Single也调用我们指定的方法
		//OnError还返回一个Single，可立即将错误通知订户
		String[] result = {""};
		Single<String> single = Observable.just("Hello")
				.singleOrError()
				.doOnSuccess(i -> result[0] += i)
				.doOnError(error -> {
					throw new RuntimeException(error.getMessage());
				});
		single.subscribe();
		System.out.println(result[0]);
	/*	assertTrue(result[0].equals("Hello"));*/
	}
}
