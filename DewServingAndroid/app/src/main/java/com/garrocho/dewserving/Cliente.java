package com.garrocho.dewserving;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

public class Cliente {

    public Socket socket;
    public DataInputStream in;
    public OutputStream out;

    public Cliente(Socket socket) {
        try {
            this.socket = socket;
            this.in = new DataInputStream(this.socket.getInputStream());
            this.out = this.socket.getOutputStream();
        }catch (Exception erro) {
            erro.printStackTrace();
        }
    }

    public String receberMensagem() throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        read = this.in.read(buffer);
        return new String(buffer, 0, read);
    }

    public void enviarMensagem(String mensagem) {
        PrintStream printStream = new PrintStream(this.out);
        printStream.print(mensagem);
    }

    public String removerCaracteres(String texto) {
        texto = texto.replace("(", "");
        texto = texto.replace(")", "");
        return texto;
    }

    public ArrayList<String> quebrarMensagem(String texto, String quebra) {
        String[] dados = texto.split(quebra);
        ArrayList<String> mensagens = new ArrayList<String>();
        for (String msg : dados) {
            mensagens.add(removerCaracteres(msg));
        }
        return mensagens;
    }
}
