package redmopag.client;

import redmopag.network.TCPConnection;
import redmopag.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {
    // Константы стоит делать static
    private static final String IP_ADDR = "";
    private static final int PORT = 8189;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    public static void main(String[] args) {
        /*
        Для работы JFrame нужен отдельный поток. Создаём его
         */
        SwingUtilities.invokeLater(new Runnable() { // Поток EDT
            public void run() {
                new ClientWindow();
            }
        });
    }

    private final JTextArea log = new JTextArea();
    private final JTextField fieldNickname = new JTextField("Alex");
    private final JTextField fieldInput = new JTextField();
    private TCPConnection connection;
    private ClientWindow(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // Прекращение процесса, при закрытии окна

        // Настройка окна
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null); // Окно по середине
        setAlwaysOnTop(true); // Окно всегда выше остальных вкладок

        log.setEditable(false); // Нельзя изменить
        log.setLineWrap(true); // Перенос строк
        add(log, BorderLayout.CENTER); // Добавляем элемент на окно в центр

        add(fieldNickname, BorderLayout.NORTH); // Добавление на верх окна

        fieldInput.addActionListener(this); // Событие срабатывает по умолчанию, когда нажимается enter
        add(fieldInput, BorderLayout.SOUTH); // Низ окна

        setVisible(true);
        try {
            connection = new TCPConnection(this, IP_ADDR, PORT);
        } catch (IOException e) {
            printMsg("Connection exception: " + e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String value = fieldInput.getText();
        if(value.equals(""))
            return;
        fieldInput.setText(null);
        connection.sendString(fieldNickname.getText() + ": " + value);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready...");
    }

    @Override
    public void onRecieveString(TCPConnection tcpConnection, String value) {
        printMsg(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close...");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: " + e);
    }

    public synchronized void printMsg(String msg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                // Настройка автоскрола
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }
}
