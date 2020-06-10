package com.ateqi.entity;

import com.alibaba.fastjson.JSONObject;

public class Message {
    private String type;
    private String topic;
    private Object body;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
