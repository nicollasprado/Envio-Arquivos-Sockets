package com.nicollasprado.envioArquivos.Version2;

public class run {
    public static void main(String[] args) {
        Server server = new Server();

        server.start(6000,"127.0.0.1", 10);
    }
}
