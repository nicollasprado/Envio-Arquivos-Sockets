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
            InputStream requesterReceivedMsg = clientSocket.getInputStream();
            OutputStream requesterSendMsg = clientSocket.getOutputStream();

            while(clientSocket.isConnected()){
                String destinationSocketIp = receiveDestinationIp(requesterReceivedMsg);
                // Send to the client 1 if found ip address and 0 if not
                byte[] foundIp = checkIfDestinationIpIsConnected(destinationSocketIp);
                if(foundIp[0] == (byte) 0){ // not found
                    requesterSendMsg.write(foundIp);
                    requesterSendMsg.flush(); // Garants that the signal will be received immediately and the internal socket buffer will be cleared
                }else{
                    destinationSocket = clientsConnected.get(InetAddress.getByName(destinationSocketIp));
                    InputStream destinationReceivedMsg = destinationSocket.getInputStream();
                    OutputStream destinationSendMsg = destinationSocket.getOutputStream();





                    // CONTINUAR -> ENVIO DE REQUISIÇAO PARA O DESTINATARIO





                    // Found ip
                    requesterSendMsg.write(foundIp);
                    requesterSendMsg.flush();

                    int sendFileToDestination = receiveFileAndSendToDestination(requesterReceivedMsg, destinationSendMsg);
                }
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeConnection() throws IOException {
        clientsConnected.remove(clientSocket.getInetAddress());
        clientSocket.close();
    }



    // Receives the file destination socket hostname
    private String receiveDestinationIp(InputStream msgReceived){
        byte[] buffer = new byte[4];

        try{
            msgReceived.read(buffer);
            InetAddress ipAdress = InetAddress.getByAddress(buffer);
            return ipAdress.getHostAddress();
        } catch (IOException e) {
            throw new RuntimeException("Endereço de destino invalido");
        }
    }


    private byte[] checkIfDestinationIpIsConnected(String destinationSocketIp){
        // Send 1 to client if found the destination IP adress and 0 if not found
        byte[] response = new byte[1];
        try {
            if (clientsConnected.containsKey(InetAddress.getByName(destinationSocketIp))) {
                response[0] = (byte) 1; // 1
            }
            // returns 0 by default
            return response;
        } catch (UnknownHostException e) {
            throw new RuntimeException("Endereço IP de destino invalido!");
        }
    }


    private int receiveFileAndSendToDestination(InputStream requesterReceivedMsg, OutputStream destinationSendMsg){
        byte[] receiveBuffer = new byte[4096];

        try {
            while (requesterReceivedMsg.read(receiveBuffer) != -1){
                destinationSendMsg.write(receiveBuffer);
            }
            return 1;
        }catch (IOException e){
            throw new RuntimeException("Falha ao receber arquivo: " + e);
        }
    }

}