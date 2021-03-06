import json, string, ntplib, random, threading, time
from threading import Thread
from StringIO import StringIO
from twisted.internet import reactor
from autobahn.websocket import WebSocketServerFactory, \
                               WebSocketServerProtocol, \
                               listenWS
 
 
class EchoServerProtocol(WebSocketServerProtocol):

   # init methode
   def __init__(self):
	self.threadActive = False
	self.measurmentsActive = False
   # on connection opened
   def onOpen(self):
	print "Server conneced to Client ... "
	ack = {"ack": "connection_open_ack"}
	self.sendMessage(json.dumps(ack), binary=False)

   # on messages received from client
   def onMessage(self, msg, binary):
	#self.calculateLatency(msg)

	# get remote configuration 
	io = StringIO(msg)
	out = json.load(io)
	transmissionInterval = float((json.load(StringIO(out['transmissionInterval']))))
	playloadSize = (json.load(StringIO(out['playloadSize'])))

	# start sending measuring packets to client
	if self.threadActive == False:
		self.stop_event = threading.Event()		
		self.measurmentsActive = True
		thread = threading.Thread(target = self.sendMeasurments, args=(transmissionInterval/1000.0,playloadSize, ))
		thread.start()
		self.threadActive = True

	# stop sending measuring packets to client
	else:
		self.threadActive = False
		self.measurmentsActive = False	
		self.stop_event.set()

   # method for threading issues 	
   def sendMeasurments(self, interval, size):
	print "transmissionInterval (sec): ", interval, " generated playloadSize (byte): ", size
	while (self.measurmentsActive == True):
		# connect to NTP server 
		ntpClient = ntplib.NTPClient()
		ntpResponse = ntpClient.request('ntp1.belwue.de', version=3)
		sendTime = int(ntpResponse.tx_time*1000)

		payload = {"timestamp":  sendTime, "data" : self.randomByteString(size)}
		self.sendMessage(json.dumps(payload), binary=False)
		time.sleep(interval)
		pass

	print "finished sending ... "

   # returns random upper cases string of len bytes
   def randomByteString(self, len):
	print "create random payload ..."
	return ''.join(random.choice(string.ascii_uppercase + string.digits) for x in range(len))

   # NOTE: only usefull if server is endpoint
   # converts input stream to dict, to access json elements
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
