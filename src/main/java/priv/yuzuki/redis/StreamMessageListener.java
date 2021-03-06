package priv.yuzuki.redis;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;
import priv.yuzuki.redis.config.RedisStreamConfig;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @program: redis
 * @author: wangzibai01
 * @create: 2021-04-12 15:47
 * @description:
 **/
@Component
@Slf4j
public class StreamMessageListener implements StreamListener<String, MapRecord<String, String, String>> {
//	static final Logger LOGGER = LoggerFactory.getLogger(StreamMessageListener.class);

	@Resource
	RedisStreamConfig redisStreamConfig;

	@Resource
	StringRedisTemplate stringRedisTemplate;

	@Resource
	NoticeMediator noticeMediator;

	public static int count = 1;

	@Override
	public void onMessage(MapRecord<String, String, String> message) {

		// 消息ID
		RecordId messageId = message.getId();

		// 消息的key和value
		Map<String, String> body = message.getValue();

		log.info("stream message。messageId={}, stream={}, body={}", messageId, message.getStream(), body);
		try {
			// 模拟失败
//			if (count++ %5 == 0){
//				log.info("message = " + message);
//				throw new RuntimeException("出错啦");
//			}
			noticeMediator.sendNotice(body);
			this.stringRedisTemplate.opsForStream().acknowledge(redisStreamConfig.getConsumerGroup(), message);
		} catch (RuntimeException e) {
			// todo 可发送邮件通知
			e.printStackTrace();
		}
	}
}
