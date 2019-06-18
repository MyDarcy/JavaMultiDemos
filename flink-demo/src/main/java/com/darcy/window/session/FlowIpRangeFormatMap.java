package com.darcy.window.session;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.MapFunction;

public class FlowIpRangeFormatMap implements MapFunction<FlowIpRangeOutputFormat, JSONObject> {

  @Override
  public JSONObject map(FlowIpRangeOutputFormat value) throws Exception {
    String json = JSON.toJSONString(value);
    return JSONObject.parseObject(json);
  }
}
