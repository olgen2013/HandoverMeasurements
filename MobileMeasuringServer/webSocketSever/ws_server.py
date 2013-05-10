import json, string, ntplib, random, struct
from threading import Thread
from time import sleep
from StringIO import StringIO
from twisted.internet import reactor
from autobahn.websocket import WebSocketServerFactory, \
                               WebSocketServerProtocol, \
                               listenWS
 
 
class EchoServerProtocol(WebSocketServerProtocol):
 
   def onOpen(self):
	print "Server conneced to Client ... "   	
	self.sendMessage("manual server-Ack", binary=False)

   def onMessage(self, msg, binary):
#	self.calculateLatency(msg)
	#thread = Thread(target = self.sendMeasurments)
	#thread.start()
	#thread.join()
	#for i in range(10):
	#while True:
	self.sendMessage(self.randomByteString(500), binary=False)
	#sleep(5)


   def sendMeasurments(self):
	for i in range(10):
	#while True:
		self.sendMessage(self.randomByteString(500), binary=False)
		sleep(1)
	print "finished sending ... "

   def randomByteString(self, len):
	print "create random payload ..."
	return ''.join(random.choice(string.ascii_uppercase + string.digits) for x in range(len))
#   	return ''.join([struct.pack("!Q",random.getrandbits(64)) for x in xrange(0, len / 8 + int(len % 8 > 0))])[:len]

   # only usefull if server is endpoint
   def calculateLatency(self, msg):
	io = StringIO(msg)
	out = json.load(io)	
	if len(out) > 2:
		# convert unicode to string, string to dictionary
		data = (json.load(StringIO(out[2])))
		# access dict element		
		sendTime = data['timestamp_sender']
		
		# connect to NTP server 
		ntpClient = ntplib.NTPClient()
		ntpResponse = ntpClient.request('europe.pool.ntp.org', version=3)
		receiveTime = int(ntpResponse.tx_time*1000)
		latency = receiveTime-sendTime 
		print "latency: ", latency, "sendTime: ", sendTime, "receiveTime: ", receiveTime
 
if __name__ == '__main__':
   
   factory = WebSocketServerFactory("ws://141.62.65.108:443", debug = False)
   factory.protocol = EchoServerProtocol
   listenWS(factory)
   reactor.run()
