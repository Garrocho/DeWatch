package com.garrocho.dewserving;

import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Servidor {
    MainActivity activity;
    ServerSocket socketServidor;
    String mensagem = "";
    static final int socketServidorPORTA = 8080;
    public static Map<String, InetAddress> servicos;
    public static ArrayList<String> ipsAtivoRede;

    public Servidor(MainActivity activity) {
        this.activity = activity;
        this.servicos = new HashMap<String, InetAddress>();

        // Adiciona Servidor da Nuvem
        //this.ipsAtivoRede.add("192.168.0.103");

        // Iniciando Servicos do servidor de clientes
        Thread servirClientes = new Thread(new SocketServidorThread());
        servirClientes.start();

        // Iniciando servico de atualizacao do DNS de servicos
        Thread atualizaDNS = new Thread(new AtualizaServicosDNS());
        atualizaDNS.start();
    }

    public int getPort() {
        return socketServidorPORTA;
    }

    public void onDestroy() {
        if (socketServidor != null) {
            try {
                socketServidor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class SocketServidorThread extends Thread {

        @Override
        public void run() {

            try {
                socketServidor = new ServerSocket(socketServidorPORTA);

                while (true) {
                    Socket socket = socketServidor.accept();
                    TrataCliente trataCliente = new TrataCliente(socket);
                    trataCliente.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class TrataCliente extends Thread {

        public Cliente cliente;

        TrataCliente(Socket socket) {
            cliente = new Cliente(socket);
        }

        @Override
        public void run() {

            try {
                String msg = cliente.receberMensagem();

                ArrayList<String> mensagens = cliente.quebrarMensagem(msg, "(888)");

                if (mensagens.get(0).contains("CONSUMIR")) {

                    InetAddress ipServico = Servidor.servicos.get(mensagens.get(1));
                    Socket socketServico = new Socket(ipServico, 8081);

                    Cliente cliServico = new Cliente(socketServico);
                    cliServico.enviarMensagem(mensagens.get(1));
                    String resultado = cliServico.receberMensagem();

                    cliente.enviarMensagem(resultado);
                    mensagem += "Servico " + mensagens.get(1) + " prestado ao cliente " + cliente.socket.getInetAddress()  + "\n";
                    atualizaInterface(mensagem);
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void atualizaInterface(final String mensagem) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.msg.setText(mensagem);
            }
        });
    }

    public class AtualizaServicosDNS extends Thread {

        public Cliente cliente;

        @Override
        public void run() {

            while (true) {
                try {
                    checkIPNaRede();

                    for (String ip : Servidor.ipsAtivoRede) {

                        try {
                            Log.d("DEW_SERVER", "DNS: " + ip);
                            SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(ip), 8082);
                            Socket socket = new Socket();
                            socket.connect(socketAddress, 200);
                            cliente = new Cliente(socket);

                            cliente.enviarMensagem("LER_SER");

                            String mensagem = cliente.receberMensagem();

                            ArrayList<String> servicos = cliente.quebrarMensagem(mensagem, "(888)");
                            InetAddress ipServ = InetAddress.getByName(servicos.remove(0));

                            for (String servico : servicos) {
                                InetAddress existe = Servidor.servicos.get(servico);
                                if (existe == null)
                                    Servidor.servicos.put(servico, ipServ);
                            }
                            cliente.socket.close();
                        }catch (Exception erro) {
                            erro.printStackTrace();
                        }
                    }
                    //Thread.sleep(5000);
                }catch (Exception erro) {
                    erro.printStackTrace();
                }
            }
        }
    }

    public void checkIPNaRede() throws Exception {
        ipsAtivoRede = new ArrayList<String>();
        String ip = getIpAddress();
        String subnet = ip.substring(0, ip.lastIndexOf("."));
        int timeout=50;
        for (int i=2;i<255;i++){
            String host=subnet + "." + i;
            if (InetAddress.getByName(host).isReachable(timeout)){
                ipsAtivoRede.add(host);
            }
        }
    }

    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }
}