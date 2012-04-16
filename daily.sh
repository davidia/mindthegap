#!/bin/bash

wget -O daily/$1.daily.csv "http://ichart.finance.yahoo.com/table.csv?s=$1&a=00&b=1&c=2011&d=03&e=8&f=2012&g=d&ignore=.csv"
