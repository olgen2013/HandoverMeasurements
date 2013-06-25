'''
Created on 12.06.2013

@author: Johannes Goth 
@contact: jg079@hdm-stuttgart.de 


"tmp.csv" :
    1) müssen \" ersetzt werden mit " (bsp gedit)
    2) ack packet muss entfernt werden oder if clause einfügen, welcher diese ausnahme abfängt
    
    
änderungen zur alten version sieht git diff oder commit

 

'''

import json, string, numpy
from numpy import recfromcsv
from StringIO import StringIO

# set wireshark input file path
inputFile = 'tmp.csv'
# parse wireshark input
samples = recfromcsv(inputFile, delimiter='\t')

# parse app csv file 
appData = recfromcsv("moma2013-06-20-19-08-07-612.csv", delimiter=',')
#print appData["networktype"]


# initialization of variables 
range_limit = samples[0][0]
range_limit = int(range_limit) + 1 
results = []
sum_bytes = 0

# processing loop 
for packet in samples:
    #print packet[1]
    
    if packet[0] < range_limit:
        sum_bytes += packet[1]
        
    elif int(packet[0]) > range_limit:
        
        # get networktype to current timestamp
        networkType = ""
        if packet[2] != "":#len(packet) > 2:
            payloadString = packet[2]
            split = payloadString.split(",{")
            #print split
            payloadDict = json.loads(split[0])
            for ts in appData:
                if ts["timestamp_sender"] == payloadDict["timestamp"]:
                    networkType = ts["networktype"]
        
        print sum_bytes,",",networkType
        results.append(sum_bytes)
        sum_bytes = packet[1]
        diff = int(packet[0]) - range_limit
        for k in range(diff):
            print 0, ","
            results.append(0)
        range_limit = int(packet[0]) + 1
        
    elif int(packet[0]) == range_limit:    
        
        # get networktype to current timestamp
        networkType = ""
        if packet[2] != "": #len(packet) > 2:
            payloadString = packet[2]
            split = payloadString.split(",{")
            #print split
            payloadDict = json.loads(split[0])
            
            for ts in appData:
                if ts["timestamp_sender"] == payloadDict["timestamp"]:
                    networkType = ts["networktype"]
        
        print sum_bytes,",",networkType
        results.append(sum_bytes)
        range_limit = int(packet[0]) + 1
        sum_bytes = 0
        sum_bytes += packet[1]
        
    else:
        print "ERROR"
    
#for i in results:
#    print i
#print results
print "seconds: ", len(results)

