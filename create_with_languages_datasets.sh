#!/bin/bash

if [ "$#" -ne 4 ]; then
    echo "Usage: ./create_datasets.sh  dataset-name table-name bucket-name max-num-workers"
    exit
fi

PROJECT=$DEVSHELL_PROJECT_ID
DATASETNAME=$1
TABLENAME=$2
BUCKET=$3
MAX_NUM_WORKERS=$4

gsutil -m rm -rf gs://$BUCKET/staging

export GOOGLE_APPLICATION_CREDENTIALS="/home/$USER/analysis/json-key.json"

mvn compile exec:java \
 -Dexec.mainClass=com.example.test.IdentifyLanguagePipeline \
 -Dexec.args="--project=$PROJECT --region=us-central1 --bucket=$BUCKET --dataset=$DATASETNAME --table=$TABLENAME --maxNumWorkers=$MAX_NUM_WORKERS --autoscalingAlgorithm=THROUGHPUT_BASED --workerMachineType=c2-standard-16"

