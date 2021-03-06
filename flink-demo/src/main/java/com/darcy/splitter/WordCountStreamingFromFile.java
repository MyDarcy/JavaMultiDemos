package com.darcy.splitter;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements the "WordCount" program that computes a simple word occurrence histogram
 * over some sample data
 *
 * <p>
 * This example shows how to:
 * <ul>
 * <li>write a simple Flink program.
 * <li>use Tuple data types.
 * <li>write and use user-defined functions.
 * </ul>
 *
 */
public class WordCountStreamingFromFile {

	private static final Logger logger = LoggerFactory.getLogger(WordCountStreamingFromFile.class);

	public static void main(String[] args) throws Exception {
		// 固定 jobName 的方式仅为方便本地调试
		// 线上作业请使用 FlinkConf.getJobName(args) 作为 jobName！请不要自行指定！
		String jobName = "WordCountStreamingFromFile";
		// String jobName = FlinkConf.getJobName(args);

		// set up the execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// get input data from text file under resources/
		String fileName = "/Users/darcy/IdeaProjects/JavaMultiDemos/flink-demo/src/main/java/com/darcy/splitter/WordCountStreamingFromFile.java";
		DataStream<String> text = env.readTextFile(WordCountStreamingFromFile.class.getClassLoader().getResource(fileName).getPath())
				.setParallelism(1);

		DataStream<Tuple2<String, Integer>> counts =
		// split up the lines in pairs (2-tuples) containing: (word,1)
		text.flatMap(new LineSplitter()).setParallelism(1)
		// group by the tuple field "0" and sum up tuple field "1"
				.keyBy(0)
				.sum(1)
				.setParallelism(1);

		// execute and print result
		counts.print().setParallelism(1);
		
		env.execute(jobName);
	}

	/**
	 * Implements the string tokenizer that splits sentences into words as a user-defined
	 * FlatMapFunction. The function takes a line (String) and splits it into
	 * multiple pairs in the form of "(word,1)" (Tuple2<String, Integer>).
	 */
	public static final class LineSplitter implements FlatMapFunction<String, Tuple2<String, Integer>> {

		@Override
		public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
			// normalize and split the line
			String[] tokens = value.toLowerCase().split("\\W+");
			// emit the pairs
			for (String token : tokens) {
				if (token.length() > 0) {
					out.collect(new Tuple2<String, Integer>(token, 1));
				}
			}
			System.out.println("flatMap value:" + value);
		}
	}
}
