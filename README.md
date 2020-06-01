# websocket.server
websocket.server是基于netty实现的websocket协议的服务器软件。
## 实现功能
1 发布订阅消息

2 心跳检测
## 使用方法
  1 初始化主题
  ```javascript
  msg = {
    "type": "INIT_TOPIC",
    "topic": "topic1"
  }
  ```
  
  2 发送消息
  ```javascript
  msg = {
    "type": "SEND_MSG",
    "topic": "topic1",
    "body": {
      "title": :"hello world"
    }
  }
   ```
   
  3 订阅消息
  ```javascript
  msg = {
    "type": "SUB_TOPIC",
    "topic": "topic1"
  }
  ```
  
  4 取消订阅消息
  ```javascript
  msg = {
    "type": "CANCEL_SUB_TOPIC",
    "topic": "topic1"
  }
  ```
  
  5 心跳检测
  ```javascript
  msg = {
    "type": "HEAT_BEAT"
  }
  ```
