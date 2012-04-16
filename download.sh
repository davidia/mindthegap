#!/bin/bash

function download {

wget -O $1 "http://localhost:5000/barData?symbol=$1&historyType=0&intradayMinutes=1&beginTime=20120101093000&endTime=20120403160000"
awk  'BEGIN {FS=",";OFS="\t"} {print $1,$5}' $1 > $1.temp

}

cd data

download $1
download $2
# dt=$3
# l1=`wc -l < $1.temp`
# l2=`wc -l < $2.temp`
# if [ $l1 -gt $l2 ]; then
# 	a=$1.temp
# 	b=$2.temp
# 	echo $1 longer than $2
# else
# 	a=$2.temp
# 	b=$1.temp
# 	echo $2 longer than $1
# fi

# paste $a $b > $1-vs-$2.csv
# rm $a
# rm $b
# cd ..

