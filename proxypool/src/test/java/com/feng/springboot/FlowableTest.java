package com.feng.springboot;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.exceptions.MissingBackpressureException;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @ClassName FlowableTest
 * @Description TODO
 * @Author fengxiaoxiao
 * @Date 2021/2/24 10:16
 * @Version 1.0
 */
// 在本教程中，我们介绍了RxJava 2中引入的名为Flowable的新类。
//要查找有关Flowable本身及其API的更多信息，请参考文档。
//与往常一样，所有代码示例都可以在GitHub上找到。
public class FlowableTest {

	List testList = IntStream.range(0, 100000)
			.boxed()
			.collect(Collectors.toList());

	@Test
	public void create() {
		//我们可以像使用Observable一样使用just（）方法创建Flowable：
		// 尽管使用just（）非常简单，但是从静态数据创建Flowable并不是很常见，并且用于测试目的。
		Flowable<Integer> integerFlowable = Flowable.just(1, 2, 3, 4);

		// 当我们有一个Observable时，可以使用toFlowable（）方法轻松地将其转换为Flowable：
		Observable<Integer> observable = Observable.just(1, 2, 3, 5);
		// 注意，要执行转换，我们需要使用BackpressureStrategy丰富Observable。我们将在下一节中介绍可用的策略。
		Flowable<Integer> integerFlowable1 = observable.toFlowable(BackpressureStrategy.BUFFER);
		// RxJava 2引入了一个功能接口FlowableOnSubscribe，该接口表示一个Flowable，在使用者订阅它之后，它开始发出事件。
		// 因此，所有客户端将接收相同的事件集，这使FlowableOnSubscribe背压安全。
		//当我们有了FlowableOnSubscribe时，我们可以使用它来创建Flowable：
		FlowableOnSubscribe<Integer> flowableOnSubscribe
				= flowable -> flowable.onNext(1);
		Flowable<Integer> integerFlowable2 = Flowable
				.create(flowableOnSubscribe, BackpressureStrategy.BUFFER);
	}

	@Test
	public void flowablePressureStrategy() {
		// 诸如toFlowable（）或create（）之类的某些方法采用BackpressureStrategy作为参数。
		// BackpressureStrategy是一个枚举，它定义了我们将应用于Flowable的背压行为。
		//它可以缓存或删除事件，或者根本不执行任何行为，在最后一种情况下，我们将负责使用反压运算符定义它。
		// BackpressureStrategy与以前版本的RxJava中存在的BackpressureMode相似。
		//RxJava 2提供了五种不同的策略。
	}

	@Test
	public void pressureStrategyBuffer() {
		// 如果我们使用BackpressureStrategy.BUFFER，则源将缓冲所有事件，直到订阅者可以使用它们

		Observable observable = Observable.fromIterable(testList);
		TestSubscriber<Integer> testSubscriber = observable
				.toFlowable(BackpressureStrategy.BUFFER)
				.observeOn(Schedulers.computation()).test();

		testSubscriber.awaitTerminalEvent();

		List<Integer> receivedInts = testSubscriber.getEvents()
				.get(0)
				.stream()
				.mapToInt(object -> (int) object)
				.boxed()
				.collect(Collectors.toList());

		assertEquals(testList, receivedInts);
		// 这类似于在Flowable上调用onBackpressureBuffer（）方法，但是它不允许显式定义缓冲区大小或onOverflow操作。
	}

	@Test
	public void pressureStrategyDrop() {
		// 我们可以使用BackpressureStrategy.DROP丢弃无法消耗的事件，而不是对其进行缓冲。
		Observable observable = Observable.fromIterable(testList);
		TestSubscriber<Integer> testSubscriber = observable
				.toFlowable(BackpressureStrategy.DROP)
				.observeOn(Schedulers.computation())
				.test();
		testSubscriber.awaitTerminalEvent();
		List<Integer> receivedInts = testSubscriber.getEvents()
				.get(0)
				.stream()
				.mapToInt(object -> (int) object)
				.boxed()
				.collect(Collectors.toList());

/*		assertThat(receivedInts.size() < testList.size());
		assertThat(!receivedInts.contains(100000));*/
	}

	@Test
	public void pressureStrategyLatest() {
		// 使用BackpressureStrategy.LATEST将强制源仅保留最新事件，从而在使用者无法跟上时覆盖所有先前值：
		Observable observable = Observable.fromIterable(testList);
		TestSubscriber<Integer> testSubscriber = observable
				.toFlowable(BackpressureStrategy.LATEST)
				.observeOn(Schedulers.computation())
				.test();

		testSubscriber.awaitTerminalEvent();
		List<Integer> receivedInts = testSubscriber.getEvents()
				.get(0)
				.stream()
				.mapToInt(object -> (int) object)
				.boxed()
				.collect(Collectors.toList());

	/*	assertThat(receivedInts.size() < testList.size());
		assertThat(receivedInts.contains(100000));*/
		// 当我们查看代码时，BackpressureStrategy.LATEST和BackpressureStrategy.DROP看起来非常相似。
		//但是，BackpressureStrategy.LATEST将覆盖我们的订户无法处理的元素，并仅保留最新的元素，因此命名为。
		//另一方面，BackpressureStrategy.DROP将丢弃无法处理的元素。这意味着不一定会发出最新元素。
	}

	@Test
	public void pressureStrategyError() {
		// 当我们使用BackpressureStrategy.ERROR时，我们只是在说我们不希望发生背压。
		// 因此，如果使用者跟不上源，就应该抛出MissingBackpressureException：
		Observable observable = Observable.range(1, 100000);
		TestSubscriber subscriber = observable
				.toFlowable(BackpressureStrategy.ERROR)
				.observeOn(Schedulers.computation())
				.test();

		subscriber.awaitTerminalEvent();
		subscriber.assertError(MissingBackpressureException.class);
	}

	@Test
	public void pressureStrategyMissing() {
		// 如果我们使用BackpressureStrategy.MISSING，则源将推送元素而不会丢弃或缓冲。
		//在这种情况下，下游将必须处理溢出：
		Observable observable = Observable.range(1, 100000);
		TestSubscriber subscriber = observable
				.toFlowable(BackpressureStrategy.MISSING)
				.observeOn(Schedulers.computation())
				.test();
		subscriber.awaitTerminalEvent();
		subscriber.assertError(MissingBackpressureException.class);
		// 在我们的测试中，我们同时针对ERROR和MISSING策略都缺少MissingbackpressureException。因为当源的内部缓冲区溢出时，它们都将引发此类异常。
		//但是，值得注意的是，两者都有不同的目的。
		// 当我们完全不希望背压时，应该使用前一种，并且我们希望源抛出异常以防万一。
		//如果我们不想在创建Flowable时指定默认行为，则可以使用后一种方法。我们稍后将使用反压运算符对其进行定义。
	}


}
