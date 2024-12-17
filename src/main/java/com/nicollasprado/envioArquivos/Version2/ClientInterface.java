package com.nicollasprado.envioArquivos.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class ClientInterface {
    public static void main(String[] args) {
        Scanner terminalInput = new Scanner(System.in);
        Client clientSocket = new Client();
        clientSocket.connectToServer();
        String selectedDestination = "";

        System.out.println("Comandos disponiveis: \n!exit - Fecha o programa \n!sendFile - Inicia o processo de envio de um arquivo \nDigite um comando: ");
        String command;
        while(!(command = terminalInput.next()).equalsIgnoreCase("!exit")){
            // Checks if received file transfer attempt
            try(OutputStream clientOS = clientSocket.getClientSocket().getOutputStream();
                InputStream clientIS = clientSocket.getClientSocket().getInputStream();
                ){
                byte[] serverSignal = new byte[5]; // Signal + Requester ip address
                if(clientIS.read(serverSignal) != -1 && serverSignal[0] == (byte) 3){
                    System.out.println("Voce recebeu uma ");
                }

            }catch (IOException e){
                throw new RuntimeException(e);
            }



            switch (command){
                case "!help":
                    System.out.println("Comandos disponiveis: \n!exit - Fecha o programa \n!sendFile - Inicia o processo de envio de um arquivo");
                    break;
                case "!sendFile":
                    selectedDestination = clientSocket.selectDestinationSocketIp();

                    if(selectedDestination.isEmpty()){
                        System.out.println("Um IP de destino tem que ser especificado \nUse: !selectDestination");
                    }else{
                        if(clientSocket.sendFile(selectedDestination) == 1){
                            System.out.println("Arquivo enviado com sucesso para " + selectedDestination);
                        }
                    }
                    break;
                default:
                    System.out.println("Digite !help para ver a lista de comandos.");
                    break;
            }
        }
        clientSocket.closeConnection();
    }
}
