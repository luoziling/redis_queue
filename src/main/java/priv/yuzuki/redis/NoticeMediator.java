package priv.yuzuki.redis;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import priv.yuzuki.redis.factory.NoticeParameterFactory;
import priv.yuzuki.redis.notifier.Notifier;
import priv.yuzuki.redis.parameter.NoticeParameterStrategy;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @program: redis stream
 * @author: wangzibai01
 * @create: 2021-06-09 17:00
 * @description: 通知对外门面/中介
 **/
@Service
@Slf4j
public class NoticeMediator {
	@Resource
	private Notifier<String,String> notifier;

	@Resource
	private NoticeParameterFactory noticeParameterFactory;

	private NoticeParameterStrategy<String,String> noticeParameterStrategy;

	public void sendNotice(Map<String,String> messageBody){
		// 根据队列中存储的参数构建获取通知参数的策略对象
		String strategyEnumCode = messageBody.get(NoticeConstant.NOTICE_ACTION_STRATEGY);
		// 根据通知信息构建策略对象
		noticeParameterStrategy = noticeParameterFactory.getStringParameterStrategy(Integer.parseInt(strategyEnumCode));
		// 策略对象反序列化具体参数
		noticeParameterStrategy.init(messageBody.get(NoticeConstant.NOTICE_PARAMETER));
		String message = getMessage();
		List<String> informList = getInformList();
		// 通知人员列表去重，去空
		informList = informList.stream().distinct().filter(e -> Objects.nonNull(e) && !"".equals(e)).collect(Collectors.toList());
		if (CollUtil.isEmpty(informList)){
			// 若无人员需要被通知则略过此次通知并直接返回
			log.info("通知人员为空，无法继续发送，通知参数：{}",messageBody);
			return;
		}
		// 发送通知
		doSendNotice(message,informList);

	}

	private void doSendNotice(String message,List<String> informList){
		notifier.sendNotice(message,informList);
	}

	private String getMessage(){
		return noticeParameterStrategy.getMessage();
	}
	private List<String> getInformList(){
		return noticeParameterStrategy.listNotifiedPersons();
	}
}
