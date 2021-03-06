#!/usr/bin python2.7

import sys

from twisted.python import log
from twisted.internet import reactor
from twisted.web.server import Site
from twisted.web.static import File

from autobahn.websocket import listenWS
from autobahn.wamp import WampServerFactory, WampServerProtocol

class PubSubServer1(WampServerProtocol):
    
    
    def onSessionOpen(self):
        ## register a single, fixed URI as PubSub topic
        ##self.registerForPubSub("http://example.com/event")
        ## register a URI and all URIs having the string as prefix as PubSub topic
        ##self.registerForPubSub("http://example.com/simple#", True)
        # self.registerForPubSub("http://example.com/event")
        
        self.registerForPubSub("moma/latencyAndThroughtput")

if __name__ == '__main__':

    log.startLogging(sys.stdout)
    debug = len(sys.argv) > 1 and sys.argv[1] == 'debug'
    
    factory = WampServerFactory("ws://141.62.65.108:443", debugWamp = debug)
    factory.protocol = PubSubServer1
    factory.setProtocolOptions(allowHixie76 = True)
    listenWS(factory)
    
    #webdir = File(".")
    #web = Site(webdir)
    #reactor.listenTCP(8080, web) #@UndefinedVariable
    
    reactor.run() #@UndefinedVariable
