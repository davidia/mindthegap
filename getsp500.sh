#!/bin/bash

awk  '{cmd = sprintf("./daily.sh %s",$1);system(cmd)}' sp500sym.txt