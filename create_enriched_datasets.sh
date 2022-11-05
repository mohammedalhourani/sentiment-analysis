#!/bin/bash

if [ "$#" -ne 7 ]; then
    echo "Usage: ./create_datasets.sh Analyzer-Class dataset-name table-name bucket-name year-filter month-filter max-num-workers"
    exit
fi

PROJECT=$DEVSHELL_PROJECT_ID
ANALYZER=$1
DATASETNAME=$2
TABLENAME=$3
BUCKET=$4
YEAR=$5
MONTH=$6
MAX_NUM_WORKERS=$7


gsutil -m rm -rf gs://$BUCKET/staging

export GOOGLE_APPLICATION_CREDENTIALS="/home/$USER/analysis/json-key.json"

mvn compile exec:java \
 -Dexec.mainClass=com.example.test.${ANALYZER} \
 -Dexec.args="--project=$PROJECT --region=us-central1 --bucket=$BUCKET --dataset=$DATASETNAME --table=$TABLENAME --year=$YEAR --month=$MONTH --experiments=use_runner_v2 --autoscalingAlgorithm=THROUGHPUT_BASED --dataflowServiceOptions=enable_prime --numWorkers=$MAX_NUM_WORKERS "

# --workerMachineType=n1-highmem-8"
