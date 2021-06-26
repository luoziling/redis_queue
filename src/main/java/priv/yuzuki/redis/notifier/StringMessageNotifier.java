package priv.yuzuki.redis.notifier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: redis stream
 * @author: wangzibai01
 * @create: 2021-06-09 15:41
 * @description: 蓝信普通文字通知者
 **/
@Service
@Slf4j
public class StringMessageNotifier implements Notifier<String,String> {

	@Override
	public void sendNotice(String message, List<String> noticedPersons) {
		// todo 发送通知
		log.info("模拟给：{}发送通知：{}",noticedPersons,message);
	}
}
