#!/bin/bash

wget -O div/$1.csv "http://ichart.finance.yahoo.com/table.csv?s=$1&a=00&b=1&c=2011&d=03&e=29&f=2012&g=v&ignore=.csv"

#http://finance.yahoo.com/q/hp?s=$1&a=00&b=1&c=2011&d=03&e=29&f=2012&g=v