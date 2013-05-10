import socket
import sys
s=socket.socket(socket.AF_INET,socket.SOCK_STREAM)

host= "141.62.65.108"
port=int(443)
s.bind((host,port))
s.listen(10000000)

conn,addr =s.accept()

data=conn.recv(100000)
print data

conn.close()
s.close()

