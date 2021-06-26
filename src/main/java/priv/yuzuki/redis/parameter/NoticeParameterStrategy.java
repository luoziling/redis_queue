package priv.yuzuki.redis.parameter;

import java.util.List;

/**
 * @program: redis stream
 * @author: wangzibai01
 * @create: 2021-06-09 15:49
 * @description: 通知参数抽象接口
 **/
public interface NoticeParameterStrategy<T,S> {
	/**
	 * 初始化，主要用于消息体反序列化
	 * @param body 参数消息体
	 */
	public void init(String body);

	/**
	 * 获取通知的消息体
	 * @return
	 */
	public T getMessage();

	/**
	 * 获取被通知者列表
	 * @return
	 */
	public List<S> listNotifiedPersons();
}
