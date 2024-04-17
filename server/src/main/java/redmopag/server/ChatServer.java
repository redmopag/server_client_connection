package redmopag.server;

import redmopag.network.TCPConnection;
import redmopag.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ChatServer implements TCPConnectionListener {
    public static void main(String[] args) {
        new ChatServer();
    }

    // Список соединений
    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    // Если нет необходимости в public или другом модификаторе доступа, то лучше использовать private
    private ChatServer(){
        System.out.println("Server in running...");
        // try-with-resources
        try (ServerSocket serverSocket = new ServerSocket(8189)){ // Слушает указанный порт и принимает входящее соединение
            while(true){
                try {
                    /*
                    Создаётся соединение. Передём соединению обработчик событий и сокет.
                    accept() - возвращает объект Socket
                    */
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e){
                    /*
                    Конструктор TCPConnection может выдать исключение, которое нужно отбработать.
                    Исключение может выдать сокет
                     */
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    // Ожидается сразу несколько соединений, следовательно будет несколько потоков, которые будут
    // вызывать методы ниже

    /*
        В теле конструктора TCPConnection вызывается метод onConnectionReady только тогда, когда
        соединение удалось создать. Поэтому добавляем соединение в список соединений
    */
    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        sendToAllConnections("Client connected: " + tcpConnection);
    }

    @Override
    public synchronized void onRecieveString(TCPConnection tcpConnection, String value) {
        sendToAllConnections(value);
    }

    /*
    Удаляет соединение из списка, если произошло отсоединение
     */
    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private void sendToAllConnections(String msg){
        System.out.println(msg);
        for(TCPConnection connection : connections) connection.sendString(msg);
    }
}
