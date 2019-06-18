package com.darcy.window.session;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.MapFunction;

public class FlowIpRangeParseMap implements MapFunction<String, FlowIpRangeInputData> {

  @Override
  public FlowIpRangeInputData map(String value) throws Exception {
    JSONObject jsonObject = JSONObject.parseObject(value);
    String group_key = jsonObject.getString("groupKey");
    String proxy_ip = jsonObject.getString("proxyIp");
    String domain = jsonObject.getString("domain");
    String instance_id = jsonObject.getString("instanceId");
    long request_time = jsonObject.getLong("requestTime");

    return new FlowIpRangeInputData(group_key, proxy_ip, domain, instance_id, request_time);
  }
}