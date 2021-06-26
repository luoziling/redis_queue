package priv.yuzuki.redis.notifier;

import java.util.List;

/**
 * @program: redis stream
 * @author: wangzibai01
 * @create: 2021-06-09 15:33
 * @description: 抽象通知者接口，执行发送通知的动作
 **/
public interface Notifier<T,S> {
	/**
	 * 核心通知方法，发送通知
	 * @param message 通知消息，兼容不同类型
	 * @param noticedPersons 被通知者列表
	 */
	public void sendNotice(T message, List<S> noticedPersons);
}
