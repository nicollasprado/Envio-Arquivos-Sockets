package com.nicollasprado.envioArquivos.Server;

import lombok.Getter;

import java.awt.font.OpenType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

@Getter
class ConnectionHandler extends Thread{
    Socket clientSocket;
    Socket destinationSocket;
    ConcurrentHashMap<InetAddress, Socket> clientsConnected;

    public ConnectionHandler(Socket clientSocket, ConcurrentHashMap<InetAddress, Socket> clientsConnected){
        this.clientSocket = clientSocket;
        this.clientsConnected = clientsConnected;
    }

    @Override
    public void run() {
        try{
            InputStream msgReceived = clientSocket.getInputStream();
            OutputStream msgSend = clientSocket.getOutputStream();
            byte[] ipBuffer = new byte[512];
            StringBuilder destinationSocketIp = new StringBuilder();

            while(clientSocket.isConnected()){
                // Receives the file destination socket hostname
                while(msgReceived.read(ipBuffer) != -1){
                    destinationSocketIp.append(new String(ipBuffer, StandardCharsets.UTF_8));
                }
                // Send to the client 1 if found ip address and 0 if not
                msgSend.write(checkIfDestinationIpIsConnected(destinationSocketIp.toString()));
                msgSend.flush(); // Garants that the signal will be received immediately and the internal socket buffer will be cleared

                String downloadPath = "";
                try{
                    File downloadFile = new File(downloadPath);
                }catch (NullPointerException e){
                    throw new RuntimeException("Destino de arquivo invalido");
                }

                byte[] dataBuffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = msgReceived.read(dataBuffer)) != -1){
                    Files.write(Path.of(downloadPath), dataBuffer, StandardOpenOption.TRUNCATE_EXISTING);
                    System.out.println("Recebendo dados...");
                }
                System.out.println("Arquivo recebido com sucesso!");
                closeConnection();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] checkIfDestinationIpIsConnected(String destinationSocketIp){
        // Send 1 to client if found the destination IP adress and 0 if not found
        byte[] response = new byte[3];
        try {
            if (clientsConnected.containsKey(InetAddress.getByName(destinationSocketIp))) {
                response[2] = 1; // 001
            }
            // returns 000 by default
            return response;
        } catch (UnknownHostException e) {
            throw new RuntimeException("Endereço IP de destino invalido!");
        }
    }

    public void closeConnection() throws IOException {
        clientsConnected.remove(clientSocket.getInetAddress());
        clientSocket.close();
    }
}