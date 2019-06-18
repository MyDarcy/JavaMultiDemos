package com.darcy.window.session;

import org.apache.flink.api.common.functions.AggregateFunction;

public class FlowIpRangeAggregate implements AggregateFunction<FlowIpRangeInputData, FlowIpRangeAccumulator, FlowIpRangeOutputData> {

  private final long interval = 60000L;

  @Override
  public FlowIpRangeAccumulator createAccumulator() {
    return new FlowIpRangeAccumulator(true);
  }

  @Override
  public FlowIpRangeAccumulator add(FlowIpRangeInputData value, FlowIpRangeAccumulator accumulator) {

    long cur_timestamp = value.getRequest_time();
    long start = cur_timestamp - (cur_timestamp % interval);

    if(accumulator.isFirst_element()){
      accumulator.setStart_date(cur_timestamp);
      accumulator.setEnd_date(cur_timestamp);
      accumulator.setCurrent_date(start);
      accumulator.setCurrent_request_count(1L);
      accumulator.setPre_element(true);
      accumulator.setNext_element(false);
      accumulator.setFirst_element(false);
    } else {
      long pre_timestamp = accumulator.getEnd_date();
      long pre_start = pre_timestamp - (pre_timestamp % interval);
      long next_end = pre_start + interval - 1L;
      if(pre_start == start){
        accumulator.setEnd_date(cur_timestamp);
        accumulator.setCurrent_date(start);
        accumulator.setCurrent_request_count(accumulator.getCurrent_request_count() + 1L);
      }else {
        accumulator.setEnd_date(next_end);
        accumulator.setCurrent_date(pre_start);
        accumulator.setPre_element(false);
        accumulator.setNext_element(true);
        accumulator.setNext_date(cur_timestamp);
        accumulator.setNext_request_count(1L);
      }
    }

    return accumulator;
  }

  @Override
  public FlowIpRangeOutputData getResult(FlowIpRangeAccumulator accumulator) {
    FlowIpRangeOutputData result = new FlowIpRangeOutputData();
    result.setRequest_date(accumulator.getCurrent_date());
    result.setStart_date(accumulator.getStart_date());
    result.setEnd_date(accumulator.getEnd_date());
    result.setRequest_count(accumulator.getCurrent_request_count());
    result.setLast_element(accumulator.isPre_element());
    result.setNext_element(accumulator.isNext_element());

    if(accumulator.isPre_element()){
      accumulator.setPre_element(false);
    }

    if(accumulator.isNext_element()){
      long next_timestamp = accumulator.getNext_date();
      long next_start = next_timestamp - (next_timestamp % interval);
      accumulator.setCurrent_date(next_start);
      accumulator.setStart_date(next_start);
      accumulator.setEnd_date(next_timestamp);
      accumulator.setCurrent_request_count(accumulator.getNext_request_count());
      accumulator.setPre_element(true);
      accumulator.setNext_element(false);
      accumulator.setNext_date(0L);
      accumulator.setNext_request_count(0L);
    }

    return result;
  }

  @Override
  public FlowIpRangeAccumulator merge(FlowIpRangeAccumulator a, FlowIpRangeAccumulator b) {
    return null;
  }
}
