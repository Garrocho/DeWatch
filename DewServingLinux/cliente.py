import socket
import time

HOST    = '192.168.0.100'
PORT    = 8080
DESTINO = (HOST, PORT)
tcp = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
inicio = time.time()
tcp.connect(DESTINO)
tcp.send('CONSUMIR(888)PRO_IMG')
msg = tcp.recv(1024)
fim = time.time()
tcp.send('SAIR')
tcp.close()
tempo = fim - inicio
print "Servico CINEMATICA executado em " + str(tempo) + " ms, resultado: " + msg
