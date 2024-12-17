package com.nicollasprado.envioArquivos.Test;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
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
        try(InputStream clientIS = clientSocket.getInputStream();
            OutputStream clientOS = clientSocket.getOutputStream()){

            while(clientSocket.isConnected()){
                if(checkDestinationConnectedRequests()){
                    byte[] destinationIp = new byte[4];
                    clientIS.read(destinationIp);
                    String destinationIpStr = new String(destinationIp);

                    if(processDestinationConnectedRequest(destinationIpStr)){
                        a
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private boolean checkDestinationConnectedRequests(){
        try(InputStream clientIS = clientSocket.getInputStream();
            OutputStream clientOS = clientSocket.getOutputStream();
            ){
            // Check if there's a "10" client signal
            byte[] requesterSignal = new byte[1];

            if(clientIS.read(requesterSignal) != -1 && requesterSignal[0] == (byte) 10){
                clientOS.write(2);
                return true;
            }else{
                clientOS.write(50);
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar sinal '10' do cliente" + e);
        }
    }

    private boolean processDestinationConnectedRequest(String destinationIp){
        try(OutputStream clientOS = clientSocket.getOutputStream()){
            InetAddress destinationInetAddress = InetAddress.getByName(destinationIp);
            if(clientsConnected.containsKey(destinationInetAddress)){
                clientOS.write(1);
                destinationSocket = clientsConnected.get(destinationInetAddress);
                return true;
            }else{
                clientOS.write(0);
                return false;
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException("Endereço ip de destino invalido: ", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean sendTransferRequestToDestination(){
        try(OutputStream destinationOS = destinationSocket.getOutputStream();
            InputStream destinationIS = destinationSocket.getInputStream()
        ){
            // Send to destination socket the server signal '3' and the requester ip
            byte[]
            destinationOS.write();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

}