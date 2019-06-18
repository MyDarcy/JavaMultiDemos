package com.darcy.window.session;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.common.typeutils.base.LongSerializer;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.Window;

@PublicEvolving
public class ContinuousEventTimeTrigger<W extends Window> extends Trigger<Object, W> {
  private static final long serialVersionUID = 1L;

  private final long interval;

  private final ReducingStateDescriptor<Long> stateDesc =
      new ReducingStateDescriptor<>("fire-time", new ContinuousEventTimeTrigger.Min(), LongSerializer.INSTANCE);

  private ContinuousEventTimeTrigger(long interval) {
    this.interval = interval;
  }

  @Override
  public TriggerResult onElement(Object element, long timestamp, W window, TriggerContext ctx) throws Exception {

    if (window.maxTimestamp() <= ctx.getCurrentWatermark()) {
      return TriggerResult.FIRE;
    } else {
      ctx.registerEventTimeTimer(window.maxTimestamp());
    }

    ReducingState<Long> fireTimestamp = ctx.getPartitionedState(stateDesc);

    // 如果第一次执行，则将元素的timestamp进行floor操作，取整后加上传入的实例变量interval，得到下一次触发时间并注册，添加到状态中
    if (fireTimestamp.get() == null) {
      long start = timestamp - (timestamp % interval);
      long nextFireTimestamp = start + interval;
      ctx.registerEventTimeTimer(nextFireTimestamp);
      fireTimestamp.add(nextFireTimestamp);
    }else {
      // 检测下一个元素所在分钟，如果存在间隙，重新注册触发时间
      long start = timestamp - (timestamp % interval);
      if(fireTimestamp.get() < start){
        clear(window, ctx);
        fireTimestamp.add(start);
        ctx.registerEventTimeTimer(start);
      }
    }

    return TriggerResult.CONTINUE;
  }

  @Override
  public TriggerResult onEventTime(long time, W window, TriggerContext ctx) throws Exception {

    if (time == window.maxTimestamp()){
      return TriggerResult.FIRE;
    }

    ReducingState<Long> fireTimestampState = ctx.getPartitionedState(stateDesc);

    Long fireTimestamp = fireTimestampState.get();

    // 如果状态中的值等于注册的时间，则删除此定时器时间戳，并注册下一个interval的时间，触发计算
    if (fireTimestamp != null && fireTimestamp == time) {
      fireTimestampState.clear();
      fireTimestampState.add(time + interval);
      ctx.registerEventTimeTimer(time + interval);
      return TriggerResult.FIRE;
    }

    return TriggerResult.CONTINUE;
  }

  @Override
  public TriggerResult onProcessingTime(long time, W window, TriggerContext ctx) throws Exception {
    return TriggerResult.CONTINUE;
  }

  @Override
  public void clear(W window, TriggerContext ctx) throws Exception {
    ReducingState<Long> fireTimestamp = ctx.getPartitionedState(stateDesc);
    Long timestamp = fireTimestamp.get();
    if (timestamp != null) {
      ctx.deleteEventTimeTimer(timestamp);
      fireTimestamp.clear();
    }
  }

  @Override
  public boolean canMerge() {
    return true;
  }

  @Override
  public void onMerge(W window, OnMergeContext ctx) throws Exception {
    ctx.mergePartitionedState(stateDesc);
    Long nextFireTimestamp = ctx.getPartitionedState(stateDesc).get();
    if (nextFireTimestamp != null) {
      ctx.registerEventTimeTimer(nextFireTimestamp);
    }
  }

  @Override
  public String toString() {
    return "ContinuousEventTimeTrigger(" + interval + ")";
  }

  @VisibleForTesting
  public long getInterval() {
    return interval;
  }

  public static <W extends Window> ContinuousEventTimeTrigger<W> of(Time interval) {
    return new ContinuousEventTimeTrigger<>(interval.toMilliseconds()); // 60 * 1000ms
  }

  private static class Min implements ReduceFunction<Long> {
    private static final long serialVersionUID = 1L;

    @Override
    public Long reduce(Long value1, Long value2) throws Exception {
      return Math.min(value1, value2);
    }
  }
}
