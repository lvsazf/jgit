package com.lzs;

import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;

public class Main {
	
	public static void main(String[] args) {
		
		Stream<String> stream = Stream.of("a","b","c","d");
		String[] strArr = {"a","b","c","d"};
		Stream<String> stream1 = Stream.of(strArr);
		stream.forEach(System.out::println);
	}
	
}
