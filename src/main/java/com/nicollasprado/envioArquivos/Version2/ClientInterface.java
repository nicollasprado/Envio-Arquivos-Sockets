package com.nicollasprado.envioArquivos.Version2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class ClientInterface {
    private static boolean processTransferRequests(Client client){
        // Checks if received file transfer attempt
        try(OutputStream clientOS = client.getClientSocket().getOutputStream();
            InputStream clientIS = client.getClientSocket().getInputStream();
        ){
            byte[] serverSignal = new byte[5]; // Signal + Requester ip address
            if(clientIS.read(serverSignal) != -1 && serverSignal[0] == (byte) 3){
                String requesterIpAdress = new String(serverSignal, 1, serverSignal.length);

                Scanner cmd = new Scanner(System.in);
                System.out.println(requesterIpAdress + " lhe enviou um pedido de transferencia... \nDigite 1 para aceitar e 0 para recusar");
                if(cmd.nextInt() == 1){
                    // Send to the server the client signal "11" - request accepted
                    clientOS.write(11);
                    return true;
                }else{
                    clientOS.write(12); // request deny
                    return false;
                }
            }

        }catch (IOException e){
            throw new RuntimeException(e);
        }
        return false;
    }

    public static void main(String[] args) {
        Scanner terminalInput = new Scanner(System.in);
        Client clientSocket = new Client();
        clientSocket.connectToServer();
        String selectedDestination = "";

        System.out.println("Comandos disponiveis: \n!exit - Fecha o programa \n!sendFile - Inicia o processo de envio de um arquivo \nDigite um comando: ");
        String command;
        while(!(command = terminalInput.next()).equalsIgnoreCase("!exit")){
            // Checks if received file transfer request
            processTransferRequests(clientSocket);



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
