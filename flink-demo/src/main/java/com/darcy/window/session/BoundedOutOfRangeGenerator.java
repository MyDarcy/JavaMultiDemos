package com.darcy.window.session;

import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

import javax.annotation.Nullable;

public class BoundedOutOfRangeGenerator implements AssignerWithPeriodicWatermarks<FlowIpRangeInputData> {

  private final long maxOutOfRange = 10000; // 10 seconds

  private long currentMaxTimestamp;

  @Nullable
  @Override
  public Watermark getCurrentWatermark() {
    // return the watermark as current highest timestamp minus the out-of-range bound
    return new Watermark(currentMaxTimestamp - maxOutOfRange);
  }

  @Override
  public long extractTimestamp(FlowIpRangeInputData value, long previousElementTimestamp) {
    // 将数据中的时间戳字段（long 类型，精确到毫秒）赋给 timestamp 变量，此处是 FlowIpRangeInputData 的 timestamp 字段
    long timestamp = value.getRequest_time();
    currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp);
    return timestamp;
  }
}