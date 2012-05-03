#!/bin/bash


startTime=085900
endTime=110000

start=$2$startTime
end=$2$endTime

wget -O ../sp500/$1.$2.csv "http://localhost:5000/barData?symbol=$1&historyType=0&intradayMinutes=1&beginTime=$start&endTime=$end"
