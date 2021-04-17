# redis_queue
redis 5.0后stream新特性用做 队列 测试demo
消息队列初始化
```shell script
XADD noticeStream * init init
XGROUP CREATE noticeStream noticeGroup $

