package redmopag.network;

/**
 * Предназначен для обработки событий, которые возникнут в TCPConnection
 * Поможет использовать TCPConnection без изменений с server и client
 */
public interface TCPConnectionListener {
    // Для обработки события - готовность содинения
    void onConnectionReady(TCPConnection tcpConnection);
    // Обработка события - получена строка
    void onRecieveString(TCPConnection tcpConnection, String value);
    void onDisconnect(TCPConnection tcpConnection);
    void onException(TCPConnection tcpConnection, Exception e);
    // Передаём TCPConnection для работы с ним
}
