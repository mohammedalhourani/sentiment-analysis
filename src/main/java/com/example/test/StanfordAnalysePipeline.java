package com.example.test;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class StanfordAnalysePipeline {
    @DefaultCoder(AvroCoder.class)
    static class RecordData {
        final String body;
        final long created_utc;

        public RecordData() {
            this.body = "";
            this.created_utc = 0;
        }

        public RecordData(String body, long created_utc) {
            this.body = body;
            this.created_utc = created_utc;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(StanfordAnalysePipeline.class);

    public static interface MyOptions extends DataflowPipelineOptions {
        @Description("Bucket")
        @Default.String("sentiment-analysis-bucket")
        String getBucket();

        void setBucket(String s);

        @Description("dataset")
        @Default.String("sentiment_ds")
        String getDataset();

        void setDataset(String s);

        @Description("table")
        @Default.String("reddit_comments_languages")
        String getTable();

        void setTable(String s);

        @Description("year filter")
        @Default.Integer(2019)
        Integer getYear();

        void setYear(Integer s);

        @Description("month filter")
        @Default.Integer(1)
        Integer getMonth();

        void setMonth(Integer s);

    }

    private static String getTempLocation(MyOptions opts) {
        return "gs://BUCKET/staging".replace("BUCKET", opts.getBucket());
    }

    public static void main(String[] args) throws IOException {
        TableSchema schema = new TableSchema()
                .setFields(
                        ImmutableList.of(
                                new TableFieldSchema()
                                        .setName("body")
                                        .setType("STRING")
                                        .setMode("REQUIRED"),
                                new TableFieldSchema()
                                        .setName("created_utc")
                                        .setType("INTEGER")
                                        .setMode("REQUIRED"),
                                new TableFieldSchema()
                                        .setName("language")
                                        .setType("STRING")
                                        .setMode("REQUIRED"),
                                new TableFieldSchema()
                                        .setName("magnitude")
                                        .setType("BIGNUMERIC")
                                        .setMode("REQUIRED"),
                                new TableFieldSchema()
                                        .setName("sentiment")
                                        .setType("BIGNUMERIC")
                                        .setMode("REQUIRED")));

        MyOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(MyOptions.class);
        // options.setStreaming(true);
        OptionHelper.setYear(options.getYear());
        OptionHelper.setMonth(options.getMonth());
        options.setRunner(DataflowRunner.class);
        options.setTempLocation(getTempLocation(options));
        Pipeline pipeline = Pipeline.create(options);

        PCollection<TableRow> input =
                pipeline
                        .apply(
                                "Read from BigQuery reddit comments",
                                BigQueryIO.readTableRows()
                                        .from(String.format("%s:%s.%s", options.getProject(), options.getDataset(), options.getTable()))
                        );


        PCollection<TableRow> output = input
                .apply("enrich comments with sentiment and magnitude ", ParDo.of(new DoFn<TableRow, TableRow>() {
                    @ProcessElement
                    public void processElement(ProcessContext c) throws IOException, InterruptedException {
                        TableRow e = c.element();
                        long created_utc = Long.parseLong(e.get("created_utc").toString());
                        String created_date = (String)e.get("created_date");
                        String body = (String) e.get("body");
                        String lang = (String) e.get("language");

                        if (lang.equals("eng") && body.length() < 1000) {

                            try {

                                SentimentResult sentimentResult = StanfordSentimentAnalyzerProcessor.populateOverallSentimentIndexScore(body);

                                double sentimentValue = (sentimentResult.getOverallSentimentScore() == 0 ? 0.0d : (sentimentResult.getOverallSentimentClassIndexScore() < 0 ? -1 : 1) * sentimentResult.getOverallSentimentScore());
                                double magnitudeValue = (sentimentResult.getOverallSentimentScore() == 0 ? 0.0d : (Math.abs(sentimentResult.getOverallSentimentClassIndexScore())));
                                TableRow row = new TableRow()
                                        .set("body", body)
                                        .set("created_utc", created_utc)
                                        .set("language", lang)
                                        .set("magnitude", magnitudeValue)
                                        .set("sentiment", sentimentValue)
                                        .set("created_date", created_date);
                                c.output(row);
                            } catch (Exception ex) {
                                TableRow row = new TableRow()
                                        .set("body", body)
                                        .set("created_utc", created_utc)
                                        .set("language", lang)
                                        .set("magnitude", 0)
                                        .set("sentiment", 0)
                                        .set("created_date", created_date);
                                c.output(row);

                            }
                        } else {
                            TableRow row = new TableRow()
                                    .set("body", body)
                                    .set("created_utc", created_utc)
                                    .set("language", lang)
                                    .set("magnitude", 0)
                                    .set("sentiment", 0);
                            c.output(row);
                        }
                    }
                }));

        output.apply(
                "Write to BigQuery with new table",
                BigQueryIO.writeTableRows()
                        .to(String.format("%s:%s.%s", options.getProject(), options.getDataset(), options.getTable() + "_enriched"))
                        .withSchema(schema)
                        // For CreateDisposition:
                        // - CREATE_IF_NEEDED (default): creates the table if it doesn't exist, a schema is
                        // required
                        // - CREATE_NEVER: raises an error if the table doesn't exist, a schema is not needed
                        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                        // For WriteDisposition:
                        // - WRITE_EMPTY (default): raises an error if the table is not empty
                        // - WRITE_APPEND: appends new rows to existing rows
                        // - WRITE_TRUNCATE: deletes the existing rows before writing
                        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE));
        pipeline.run();
    }

}
