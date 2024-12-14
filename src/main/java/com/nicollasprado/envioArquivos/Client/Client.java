package com.nicollasprado.envioArquivos.Client;

import lombok.NoArgsConstructor;

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


@NoArgsConstructor
public class Client {
    private Socket clientSocket;
    private static final InetAddress host;
    private static final int port = 6000;

    static {
        try {
            host = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void connectToServer() {
        try {
            clientSocket = new Socket(host, port); // Creates the connection
        }catch (IOException e){
            throw new RuntimeException("Erro ao conectar ao servidor: " + e);
        }
    }

    public void closeConnection(){
        try{
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String selectDestinationSocketIp(){
        Scanner terminalInput = new Scanner(System.in);
        InetAddress destinationIp;
        byte[] destinationIpBuffer;
        byte[] foundDestination = new byte[1];

        try{
            OutputStream sendMsg = clientSocket.getOutputStream();
            InputStream receivedMsg = clientSocket.getInputStream();

            System.out.print("Digite o endereço IP de destino:  ");
            destinationIp = InetAddress.getByName(terminalInput.next());

            String destinationIpStr = destinationIp.getHostName();
            destinationIpBuffer = destinationIpStr.getBytes();

            // Send destination ip adress to server
            sendMsg.write(destinationIpBuffer);
            sendMsg.flush();

            // Receives the signal response from server if destination ip address is connected and accepted connection
            receivedMsg.read(foundDestination);

            if(foundDestination[0] == (byte) 3){ // Destination IP accepted connection
                return destinationIpStr;
            }else if(foundDestination[0] == (byte) 2){
                System.out.println("IP de destino recusou conexao");
            }else if(foundDestination[0] == (byte) 0){
                System.out.println("IP de destino nao conectado ao servidor");
            }else{
                return "";
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException("Endereço IP de destino invalido: " + e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "";
    }


    public int sendFile(String destinationSocketIp){
        // Escolha do arquivo
        Scanner terminalInput = new Scanner(System.in);
        System.out.println("Digite o caminho do arquivo que sera enviado:  ");

        String filePath = terminalInput.next();
        Path filePathClassPath = Path.of(filePath);
        if(Files.exists(filePathClassPath)){
            // Envio do arquivo
            try{
                byte[] fileBytes = Files.readAllBytes(filePathClassPath);
                OutputStream sendMsg = clientSocket.getOutputStream();

                if(fileBytes.length <= 4096){
                    sendMsg.write(fileBytes);
                }else{
                    int sentBytes = 0;
                    int buffSize = 4096;
                    while(sentBytes < fileBytes.length){
                        int remainingBytes = fileBytes.length - sentBytes;
                        // Select the quantity of bytes that will be sent to the server, if remaining bytes < buffSize the quantity to be sent will be the remaining one
                        int bytesToSend = Math.min(buffSize, remainingBytes);
                        sendMsg.write(fileBytes, sentBytes, bytesToSend);
                        sendMsg.flush();

                        sentBytes += bytesToSend;
                    }
                    sendMsg.close();
                    return 1;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{
            System.out.println("Arquivo nao encontrado \nCaminho: " + filePath);
        }
        return 0;
    }


    // When another socket tries to send you a file
    public void sendFileReceiveRequest(InetAddress requesterIp){
        Scanner terminalInput = new Scanner(System.in);
        System.out.println(requesterIp.getHostAddress() + " esta tentando te enviar um arquivo, digite 1 para aceitar e 0 para recusar.");

        if(terminalInput.nextInt() == 1){
            try{
                InputStream receivedMsg = clientSocket.getInputStream();

                // Receives the file path
                System.out.println("Digite o caminho onde sera baixado o arquivo:  ");
                String downloadPath = terminalInput.next();
                Path downloadPathCPath = Path.of(downloadPath);

                // Receive the download file name and extension
                byte[] buffer = new byte[100];
                int bytesRead = 0;
                StringBuilder fileNameAndExtension = new StringBuilder();
                while((bytesRead = receivedMsg.read(buffer)) != -1){
                    String readPart = new String(buffer, 0, bytesRead);
                    fileNameAndExtension.append(readPart);
                }

                String finalDownloadPath = downloadPath + fileNameAndExtension.toString();
                Path finalDownloadPathCPath = Path.of(finalDownloadPath);
                // Checa se o diretorio existe
                if(Files.exists(downloadPathCPath) && Files.isDirectory(downloadPathCPath)){

                    // return 00000011 to server that will return to requester that the request was accepted and the client is ready to receive data
                    OutputStream sendMsg = clientSocket.getOutputStream();
                    byte[] response = {(byte) 3};
                    sendMsg.write(response);
                    sendMsg.flush();
                    sendMsg.close();

                    System.out.println("Recebendo dados...");
                    // recebimento dos dados
                    buffer = new byte[4096];
                    while(receivedMsg.read(buffer) != -1){
                        Files.write(finalDownloadPathCPath, buffer, StandardOpenOption.TRUNCATE_EXISTING);
                    }
                    receivedMsg.close();
                    System.out.println("Arquivo recebido com sucesso!");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        try{
            OutputStream sendMsg = clientSocket.getOutputStream();
            // return 00000010 to server that will return to requester that the request was recused
            byte[] response = {(byte) 2};
            sendMsg.write(response);
            sendMsg.flush();
            sendMsg.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
