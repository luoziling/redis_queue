package priv.yuzuki.redis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

import java.time.Duration;

/**
 * @program: redis
 * @author: wangzibai01
 * @create: 2021-04-12 15:43
 * @description: stream订阅
 **/
@Slf4j
@Component
public class StreamConsumerRunner implements ApplicationRunner, DisposableBean {
//	static final Logger LOGGER = LoggerFactory.getLogger(StreamConsumerRunner.class);

//	@Value("${redis.stream.consumer}")
//	private String consumer;

	@Autowired
	RedisConnectionFactory redisConnectionFactory;

	@Autowired
	ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Autowired
	StreamMessageListener streamMessageListener;

	@Autowired
	StringRedisTemplate stringRedisTemplate;

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
					public void handleError(Throwable t) {
						// throw new RuntimeException(t);
						t.printStackTrace();
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

		// 使用监听容器对象开始监听消费（使用的是手动确认方式）
		streamMessageListenerContainer.receive(Consumer.from("group-1", "consumer-1"),
				StreamOffset.create("mystream", ReadOffset.lastConsumed()), this.streamMessageListener);

		this.streamMessageListenerContainer = streamMessageListenerContainer;
		// 启动监听
		this.streamMessageListenerContainer.start();

	}

	@Override
	public void destroy() throws Exception {
		this.streamMessageListenerContainer.stop();
	}
}
