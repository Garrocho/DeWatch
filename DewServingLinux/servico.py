import socket
import time
import threading

def trata_cliente(soquete, info_cliente):
    global SERVICOS
    global QUEBRA
    solicitacao = soquete.recv(1024)
    solicitacao = solicitacao.split(QUEBRA)
    
    if solicitacao[0] in SERVICOS:
        if solicitacao[0] == 'CIN_INV':
            soquete.send(cin_inv())
        elif solicitacao[0] == 'PRO_IMG':
            soquete.send(pro_img())
    print 'Servico ' + solicitacao[0] + " executado"
    soquete.close()

def cin_inv():
    time.sleep(3)
    return "CIN_INV 3"

def pro_img():
    time.sleep(5)
    return "PRO_IMG 5"

def ser_dns():
    HOST_DNS = '192.168.0.103'
    PORT_DNS = 8082
    DESTINO = (HOST_DNS, PORT_DNS)
    tcp = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    tcp.connect(DESTINO)
    tcp.send('ADD_SER(888)CIN_INV(888)PRO_IMG')
    tcp.close()

SERVICOS = ['CIN_INV', 'PRO_IMG']
QUEBRA = '(888)'

print 'Identificando os Servicos ao DNS'
ser_dns()

HOST = '192.168.0.103'
PORT = 8081
ORIG = (HOST, PORT)
tcp = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
tcp.bind(ORIG)
tcp.listen(1)

print 'Aguardando Clientes'
while True:
    t = threading.Thread(target=trata_cliente,args=(tcp.accept()))
    t.start()
tcp.close()
