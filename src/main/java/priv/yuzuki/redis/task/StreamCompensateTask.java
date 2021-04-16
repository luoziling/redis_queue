package priv.yuzuki.redis.task;


import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import priv.yuzuki.redis.config.RedisStreamConfig;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: redis
 * @author: wangzibai01
 * @create: 2021-04-15 18:17
 * @description: stream 处理pending list的补偿任务
 **/
@Component
@Slf4j
public class StreamCompensateTask {
	@Resource
	StringRedisTemplate stringRedisTemplate;
	@Resource
	RedisStreamConfig redisStreamConfig;
	public static int keyCount = 0;
	public static int valueCount = 0;
	public static final int LIMIT_SIZE = 3;
	public static int count = 0;

	@Scheduled(cron = "0/5 * * * * ?")
	@Async
	public void produceMessage(){
		Map<String,String> mockMap = new HashMap<>(1);
		mockMap.put("message" + keyCount++,"value" + valueCount++);
		log.info("produce mockMap = {}",mockMap);
		stringRedisTemplate.opsForStream().add(
				redisStreamConfig.getStream(),
				mockMap
		);
	}

	@Scheduled(cron = "0 0/1 * * * ?")
	@Async
	public void pendingHandler(){
		Consumer notifier = Consumer.from(redisStreamConfig.getConsumerGroup(), redisStreamConfig.getConsumer());
		List<PendingMessage> pendingMessageList = getPendingMessages(notifier);
		for (int i = 0; i < LIMIT_SIZE; i++) {
			if (!CollUtil.isEmpty(pendingMessageList)){
				for (PendingMessage pendingMessage : pendingMessageList) {
					try {
						// todo 消息消费
						log.info("pendingMessage = " + pendingMessage);
						RecordId messageId = pendingMessage.getId();
						Range<String> range = Range
								.from(Range.Bound.inclusive(messageId.toString()))
								.to(Range.Bound.inclusive(messageId.toString()));
						List<MapRecord<String, Object, Object>> mapRecordList = stringRedisTemplate.opsForStream().range(redisStreamConfig.getStream(), range);
						if (CollUtil.isEmpty(mapRecordList)){
							continue;
						}
						Map<Object, Object> map = mapRecordList.get(0).getValue();
						System.out.println("MessageMap = " + map);
						// 模拟失败
						if (count++ %5 == 0){
							throw new RuntimeException("pending handler 出错啦");
						}
						// 消费
						stringRedisTemplate.opsForStream().acknowledge(redisStreamConfig.getStream(),redisStreamConfig.getConsumerGroup(),pendingMessage.getId());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			// 重新获取pending list
			pendingMessageList = getPendingMessages(notifier);
		}
		if (CollUtil.isNotEmpty(pendingMessageList)){
			log.info("wrongList = " + pendingMessageList);
			throw new RuntimeException("pendingList 弥补出错 value:" + pendingMessageList);
		}

	}

	private List<PendingMessage> getPendingMessages(Consumer notifier) {
		PendingMessages pendingMessages = stringRedisTemplate.opsForStream().pending(redisStreamConfig.getStream(), notifier);
		List<PendingMessage> pendingMessageList = new ArrayList<>();
		pendingMessages.forEach(pendingMessageList::add);
		return pendingMessageList;
	}
}
