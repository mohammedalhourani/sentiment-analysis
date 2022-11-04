## Sentiment Analyzer

To run the code, first we need to open a cloud console, then pull the code repo using this command:

>git clone https://github.com/mohammedalhourani/sentiment-analysis.git analysis

this will crete a analysis folder in your console, go into this directory:
>cd analysis

after that run this command to give execute permessions to scripts:
>chmod 755 create_*

to run the pipeline to detect the language you need to run this command:
>./create_with_languages_datasets.sh sentiment_ds reddit_comments sentiment-analysis-bucket-test 64

Before running the analyser pipline we need to create an optimized version of the language tagged table:

```
CREATE TABLE  slalom-ai-ml.sentiment_ds.reddit_comments_languages_optimized (body String,language String,created_utc Integer, created_date TIMESTAMP)
PARTITION BY  TIMESTAMP_TRUNC(created_date, DAY)
as
SELECT body,language,created_utc,TIMESTAMP_TRUNC(TIMESTAMP_SECONDS(created_utc), DAY) as created_date
FROM `slalom-ai-ml.sentiment_ds.reddit_comments_languages` as ll where ll.language ='eng'
```

Run Stanford analyser:
>./create_enriched_datasets.sh StanfordAnalysePipeline sentiment_ds reddit_comments_languages_optimized sentiment-analysis-bucket-test **2018** **1** 64

to get more infomation you can look inside both script files to see which Apache Beam class is used for each pipeline and how the methods are passed.

## Security and service account
For this pipeline to work under gcp project we needed to create a service account with a json key, the file is included (not best practice)
>json-key.json

To use this file in the shell scripts we included this environmnet variable to point to the json file:
>export GOOGLE_APPLICATION_CREDENTIALS="/home/$USER/analysis/json-key.json"

this will allow for the Apache beam pipeline to work under the correct service account in DataFlow


## Using OpenNLP language detection model

To use the detection model we downloaded a trained model from [OpenNLP Page](https://opennlp.apache.org/models.html)
This model is saved in the resources folder , file name is: **langdetect-183.bin** , it can detect 103 languages 

## Developing on Windows machine
To execute the test locally, the gcloud commands and libraries needs to work, this will allow to make it work if you are getting and error like this:
"exception in thread com.google.api.gax.rpc.permission denied exception: io.grpc.status runtime exception: permission_denied: cloud natural language api has not been used in project"

This will solve it by executing this command on Windows powershell:
>Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned

if this doesn't work you can use the unrestricted policy (not recommended in general):
>Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy Unrestricted




