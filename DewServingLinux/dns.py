import socket
import time
import threading

def trata_cliente(soquete, info_cliente):
    global SERVICOS
    global QUEBRA
    solicitacao = soquete.recv(1024)
    print info_cliente[0], solicitacao
    
    if 'LER_SER' == solicitacao:
        soquete.send(SERVICOS)
        print 'Servicos enviados ao cliente ' + info_cliente[0]
    elif 'ADD_SER' in solicitacao:
        servs = solicitacao.split(QUEBRA)[1:]
        for serv in servs:
            SERVICOS += info_cliente[0] + QUEBRA + serv + QUEBRA
            print 'Servico ' + serv + ' de ' + info_cliente[0] + ' adicionado'
    soquete.close()

SERVICOS = ''
QUEBRA = '(888)'
HOST = '192.168.0.103'
PORT = 8082
ORIG = (HOST, PORT)
tcp = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
tcp.bind(ORIG)
tcp.listen(1)
print 'Aguardando clientes'
while True:
    t = threading.Thread(target=trata_cliente,args=(tcp.accept()))
    t.start()
tcp.close()
