package com.alibaba.fastjson2.benchmark.eishay;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class EishayWrite {
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayWrite.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(2)
                .build();
        new Runner(options).run();
    }
}
