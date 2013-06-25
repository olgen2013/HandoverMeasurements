#!/bin/sh

EXPECTED_ARGS=1
E_BADARGS=65

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` {input file}"
  exit $E_BADARGS
fi

# biderectional communication between server and client
#tshark -r $1 -T fields -e frame.time_relative -e frame.cap_len -R "ip.dst == 141.62.65.108 || ip.src == 141.62.65.108"> $1_filtered.csv

# unidirectional communication (server -> client)
tshark -r $1 -T fields -e frame.time_relative -e frame.cap_len -R "ip.src == 141.62.65.108"> $1_filtered.csv

# unidirectional communication (server -> client) with webSocket Payload 
#tshark -r $1 -T fields -e frame.time_relative -e frame.cap_len -e websocket.payload.text -R "ip.src == 141.62.65.108"> $1_filtered.csv



 
