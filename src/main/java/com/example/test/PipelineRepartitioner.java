package com.example.test;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;

import java.util.concurrent.ThreadLocalRandom;

public class PipelineRepartitioner<T> {
    public static class AddArbitraryKey<T> extends DoFn<T, KV<Integer, T>> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            c.output(KV.of(ThreadLocalRandom.current().nextInt(), c.element()));
        }
    }

    public static class RemoveArbitraryKey<T> extends DoFn<KV<Integer, Iterable<T>>, T> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            for (T s : c.element().getValue()) {
                c.output(s);
            }
        }
    }
}