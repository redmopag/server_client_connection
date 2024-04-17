package redmopag.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPConnection {
    private final Socket socket; // Содержит сокет, через который идёт информация
    private final Thread rxThread; // Поток, который работает с сокетом
    // У клиента и сервера методы одинаковый, но их реализация разная. Для того, чтобы не писать лишний код
    // Ввели доп абстракцию TCPConnectionListener
    private final TCPConnectionListener eventListener; // Обработчик событий
    private final BufferedReader in; // Содержит буффер чтения с сокета
    private final BufferedWriter out; // Содержит буффер записи

    public TCPConnection(final TCPConnectionListener eventListener, String ipAddress, int port) throws IOException{
        this(eventListener, new Socket(ipAddress, port)); // Вывоз другого конструктора
    }

    /*
    Процесс создания свзя (сокета) одинаков для сервера и клиента и отличается только в процессе обработки информации
    так что нужно создать доп уровень абстракции, который поможет настроить обработку информации нужным образом
    для каждого из объектов соединения.
     */
    public TCPConnection(final TCPConnectionListener eventListener, Socket socket) throws IOException{
        this.eventListener = eventListener;
        this.socket = socket; // Передача сокета
        // Создание буффера чтения с сокета
        in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
        // Создание буффера записи на сокет
        out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8));
        // Создание и настройка потока
        rxThread = new Thread(new Runnable() { // Используем анонимный класс, чтоб опеределить, что будет выполнятся в потоке
            public void run() {
                try {
                    // Соединение готово - уведомляем
                    eventListener.onConnectionReady(TCPConnection.this);
                    // Чтение потока - получения инфы с соединения
                    while (!rxThread.isInterrupted())
                        eventListener.onRecieveString(TCPConnection.this, in.readLine());
                } catch (IOException e){
                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    // Произошла ошибка, прервём соединение
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        // Запуск потока
        rxThread.start();
    }

    // Отправка сообщения (строки)
    /*
    Так как две нижние функции будут вызываться в разных созданных потоках, то их нужно сделать многопоточными,
    только один поток одновременно имеет доступ к фукнции
     */
    public synchronized void sendString(String value){
        try {
            // write отправляет значение без символов окончания строки, добавим свой
            out.write(value + '\n');
            // Информация может задержатся в буффере (мы ведь его используем). Принудительно отправляем инфу дальше
            out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            // Если возникла ошибка, то лучше прервать соединение
            disconnect();
        }
    }
    // Прерывание соединения
    public synchronized void disconnect(){
        // Прерывание потока - больше не читаем поток
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    // Описание соединения, фукнция затграгивает класс и в потоках не вызывается, так что не многопоточная
    @Override
    public String toString(){
        // Возвращает ip адресс и номер порта соединения
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
