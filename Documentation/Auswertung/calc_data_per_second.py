'''
Created on 12.06.2013

@author: Johannes Goth 
@contact: jg079@hdm-stuttgart.de 

'''


from numpy import recfromcsv

# set input file path
inputFile = 'out.csv'
samples = recfromcsv(inputFile, delimiter='\t')

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
        print sum_bytes
        results.append(sum_bytes)
        sum_bytes = packet[1]
        diff = int(packet[0]) - range_limit
        for k in range(diff):
            print 0
            results.append(0)
        range_limit = int(packet[0]) + 1
        
    elif int(packet[0]) == range_limit:        
        print sum_bytes
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

