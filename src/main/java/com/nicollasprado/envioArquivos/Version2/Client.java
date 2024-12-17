package com.nicollasprado.envioArquivos.Test;

import lombok.Getter;
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

@Getter
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



    public String selectDestinationSocketIp() {
        try{
            // Receives the destination ip adrress
            Scanner terminalInput = new Scanner(System.in);
            System.out.println("Digite o IP de destino:  ");
            String destinationIp = terminalInput.next();
            InetAddress destinationInetAddr = InetAddress.getByName(destinationIp);

            // Communication with server to check if ip is connected

            // Send the client signal "10" to server to check if destination IP is connected
            OutputStream clientOS = clientSocket.getOutputStream();
            byte[] clientSignal = {(byte) 10};
            clientOS.write(clientSignal);

            // Receive the answer from server 1 if found and 0 if not found
            InputStream clientIS = clientSocket.getInputStream();
            byte[] serverSignal = new byte[1];
            clientIS.read(serverSignal);

            // if server signal == "2" -> send to the server the destination IP to be processed
            if(serverSignal[0] == (byte) 2){
                clientOS.flush();
                clientOS.write(destinationIp.getBytes());

                // Receive the answer from server 1 if found and 0 if not found
                clientIS.read(serverSignal);

                switch (serverSignal[0]){
                    case (byte) 0:
                        return "";
                    case (byte) 1:
                        return destinationIp;
                    default:
                        throw new RuntimeException("ServerSignal out of bounds on function selectDestinationSocketIp!");
                }
            }else{
                throw new RuntimeException("Erro ao selecionar IP de destino, serverSignal is not '2'");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
