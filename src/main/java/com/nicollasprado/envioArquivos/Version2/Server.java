package com.nicollasprado.envioArquivos.Version2;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


@Getter
@NoArgsConstructor
public class Server {
    private ServerSocket serverSocket;
    private ConcurrentHashMap<Integer, Socket> clientsById;
    private ConcurrentHashMap<InetAddress, Socket> clientsByHost;
    private Integer clientId = 0;

    public void start(int port, String host, int maxWaitingConnections) {
        try {
            serverSocket = new ServerSocket(port, maxWaitingConnections, InetAddress.getByName(host));
            while (true) {
                Socket client = serverSocket.accept();
                addClientToMaps(client);
                new ConnectionHandler(client, clientsByHost).start(); // Thread creation
                System.out.println(client.getInetAddress().getHostAddress() + " Conectado com sucesso ao servidor!");

                Scanner terminalInput = new Scanner(System.in);
                String terminalEntry = terminalInput.next();
                if(terminalEntry.equals("!exit")){
                    stop();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop(){
        if(serverSocket != null) {
            try{
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("O servidor nao foi iniciado!");
        }
    }

    private void addClientToMaps(Socket clientSocket){
        if(clientsById != null && clientsByHost != null){
            clientsById.put(clientId, clientSocket);
            clientsByHost.put(clientSocket.getInetAddress(), clientSocket);
            clientId++;
        }else{
            clientsById = new ConcurrentHashMap<Integer, Socket>();
            clientsByHost = new ConcurrentHashMap<InetAddress, Socket>();

            clientsById.put(clientId, clientSocket);
            clientsByHost.put(clientSocket.getInetAddress(), clientSocket);
            clientId++;
        }
    }
}