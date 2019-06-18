package com.darcy.window.session;

import com.google.common.collect.Lists;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.util.List;

public class FlowIpRangeWindow implements WindowFunction<FlowIpRangeOutputData, FlowIpRangeOutputFormat, Tuple, TimeWindow>{

  private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  @Override
  public void apply(Tuple tuple, TimeWindow window, Iterable<FlowIpRangeOutputData> input, Collector<FlowIpRangeOutputFormat> out) throws Exception {
    List<FlowIpRangeOutputData> list = Lists.newArrayList(input);
    for (FlowIpRangeOutputData data : list) {
      String group_key = tuple.getField(0);
      String proxy_ip = tuple.getField(1);
      String domain = tuple.getField(2);
      String instance_id = tuple.getField(3);

      String request_date = format.format(data.getRequest_date());
      String start_time = format.format(data.getStart_date());
      String end_time = format.format(data.getEnd_date());
      long request_count = data.getRequest_count();

      String window_start_time = format.format(window.getStart());
      String window_end_time = format.format(window.getEnd());

      boolean last_element = data.isLast_element();
      boolean next_element = data.isNext_element();

      if(data.isNext_element() || data.isLast_element()){
        out.collect(new FlowIpRangeOutputFormat(
            group_key, proxy_ip, domain, instance_id,
            request_date, start_time, end_time, request_count,
            window_start_time, window_end_time,
            last_element, next_element));
      }
    }

  }
}