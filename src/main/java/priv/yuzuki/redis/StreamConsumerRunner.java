package priv.yuzuki.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ErrorHandler;
import priv.yuzuki.redis.config.RedisStreamConfig;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;

/**
 * @program: redis
 * @author: wangzibai01
 * @create: 2021-04-12 15:43
 * @description: stream订阅
 **/
@Slf4j
@Component
public class StreamConsumerRunner implements ApplicationRunner, DisposableBean {

	@Resource(name = "factory1")
	RedisConnectionFactory redisConnectionFactory;

	@Resource
	ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Resource
	StreamMessageListener streamMessageListener;

	@Resource(name = "noticeStringRedisTemplate")
	StringRedisTemplate stringRedisTemplate;

	@Resource
	RedisStreamConfig redisStreamConfig;

	private StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer;

	@Override
	public void run(ApplicationArguments args) throws Exception {

		// 创建配置对象
		StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> streamMessageListenerContainerOptions = StreamMessageListenerContainerOptions
				.builder()
				// 一次性最多拉取多少条消息
				.batchSize(10)
				// 执行消息轮询的执行器
				.executor(this.threadPoolTaskExecutor)
				// 消息消费异常的handler
				.errorHandler(new ErrorHandler() {
					@Override
					public void handleError(Throwable e) {
						// throw new RuntimeException(t);
						// 日志 + 报警
						log.error("消费特殊异常：{}",e.getMessage());
						// todo 可发送邮件报警
					}
				})
				// 超时时间，设置为0，表示不超时（超时后会抛出异常）
				.pollTimeout(Duration.ZERO)
				// 序列化器
				.serializer(new StringRedisSerializer())
				.build();

		// 根据配置对象创建监听容器对象
		StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer = StreamMessageListenerContainer
				.create(this.redisConnectionFactory, streamMessageListenerContainerOptions);

		// stream及consumer group 初始化
		prepareChannelAndGroup(stringRedisTemplate.opsForStream(), redisStreamConfig.getStream(), redisStreamConfig.getConsumerGroup());

		// 使用监听容器对象开始监听消费（使用的是手动确认方式），指定偏移为从上次消费过的位置开始继续消费
		// 开启多个消费者
		List<String> consumers = redisStreamConfig.getConsumers();
		for (String consumer : consumers) {
			streamMessageListenerContainer.receive(Consumer.from(redisStreamConfig.getConsumerGroup(), consumer),
					StreamOffset.create(redisStreamConfig.getStream(), ReadOffset.lastConsumed()), this.streamMessageListener);
		}

		this.streamMessageListenerContainer = streamMessageListenerContainer;
		// 启动监听
		this.streamMessageListenerContainer.start();

	}

	/**
	 * 初始化stream以及group
	 * @param ops redis操作对象
	 * @param channel stream name
	 * @param group consumer group name
	 */
	private void prepareChannelAndGroup(StreamOperations<String, ?, ?> ops, String channel, String group) {
		String status = "OK";
		try {
			StreamInfo.XInfoGroups groups = ops.groups(channel);
			if (groups.stream().noneMatch(xInfoGroup -> group.equals(xInfoGroup.groupName()))) {
				status = ops.createGroup(channel, group);
			}
		} catch (Exception exception) {
			RecordId initialRecord = ops.add(ObjectRecord.create(channel, "Initial Record"));
			Assert.notNull(initialRecord, "Cannot initialize stream with key '" + channel + "'");
			status = ops.createGroup(channel, ReadOffset.from(initialRecord), group);
		} finally {
			Assert.isTrue("OK".equals(status), "Cannot create group with name '" + group + "'");
		}
	}

	@Override
	public void destroy() throws Exception {
		this.streamMessageListenerContainer.stop();
	}
}
