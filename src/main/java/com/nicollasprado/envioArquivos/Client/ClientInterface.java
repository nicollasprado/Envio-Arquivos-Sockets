package com.nicollasprado.envioArquivos.Client;

import java.util.Scanner;

public class ClientInterface {
    public static void main(String[] args) {
        Scanner terminalInput = new Scanner(System.in);
        Client clientSocket = new Client();
        clientSocket.connectToServer();
        String selectedDestination = "";


        String command;
        while(!(command = terminalInput.next()).equalsIgnoreCase("!exit")){
            System.out.println("Digite um comando: ");
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
