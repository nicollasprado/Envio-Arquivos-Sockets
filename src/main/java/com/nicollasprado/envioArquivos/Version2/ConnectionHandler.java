package com.nicollasprado.envioArquivos.Version2;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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

                        if(sendTransferRequestToDestination()){
                            processFileTransfer();
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    // Constantly checks if there's a client '10' signal -> to request a destination ip connected check
    private boolean checkDestinationConnectedRequests(){
        try(InputStream clientIS = clientSocket.getInputStream();
            OutputStream clientOS = clientSocket.getOutputStream();
            ){
            byte[] requesterSignal = new byte[1];

            if(clientIS.read(requesterSignal) > -1 && requesterSignal[0] == (byte) 10){
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


    // Checks if the destination ip is connected to the server
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


    // Sends to the destination ip the file transfer request
    private boolean sendTransferRequestToDestination(){
        try(OutputStream destinationOS = destinationSocket.getOutputStream();
            InputStream destinationIS = destinationSocket.getInputStream()
        ){
            // Send to destination socket the server signal '3' and the requester ip
            byte[] requesterIp = clientSocket.getInetAddress().getHostAddress().getBytes();
            destinationOS.write(3);
            destinationOS.write(requesterIp);

            byte[] clientSignal = new byte[1];
            // Loops while don't receive an answer
            // I did this because the client can delay to answer the request
            do{
                if(destinationIS.read(clientSignal) > -1){
                    if(clientSignal[0] == (byte) 11){
                        // Accepted request
                        return true;
                    }else if(clientSignal[0] != (byte) 11){
                        return false;
                    }
                }
            }while(destinationIS.read(clientSignal) == -1);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Erro ao processar tentativa de envio");
    }


    private boolean processFileTransfer(){
        try(InputStream requesterIS = clientSocket.getInputStream();
            OutputStream destinationOS = destinationSocket.getOutputStream();
        ){
            // chamar as funcoes e enviar os dados!!
        }catch (IOException e){
            throw new RuntimeException("Erro ao processar envio do arquivo");
        }
    }

}