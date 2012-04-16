#!/bin/bash

awk 'BEGIN{FS=","}{ system(sprintf("./getOpen.sh %s %s",$1,$2)) }' gaps.csv
