package com.campus.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务
 */
@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {

    //使用 ConcurrentHashMap 保证线程安全
    private static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        sessionMap.put(sid, session);
        log.info("WebSocket建立连接，客户端ID: {}", sid); // 打印日志
    }

    /**
     * 收到客户端消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.info("收到客户端 {} 的消息: {}", sid, message);
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        sessionMap.remove(sid);
        log.info("WebSocket连接断开，客户端ID: {}", sid);
    }

    /**
     * 发生错误
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误", error);
    }

    /**
     * 群发消息 (保留用于广播场景)
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("WebSocket群发异常", e);
            }
        }
    }

    /**
     * 指定发送给某个客户端
     */
    public void sendToClient(String sid, String message) {
        Session session = sessionMap.get(sid);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("WebSocket发送消息给 {} 异常", sid, e);
            }
        } else {
            log.warn("客户端 {} 不在线，消息发送失败", sid);
        }
    }

    /**
     * 群发消息给所有骑手
     *
     * @param message
     */
    public void sendToAllRiders(String message) {
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            String sid = entry.getKey();
            Session session = entry.getValue();

            // 骑手的客户端连接时，sid 都是以 "rider_" 开头
            if (sid.startsWith("rider_")) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


/*package com.campus.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

*//**
 * WebSocket服务
 * <p>
 * 连接建立成功调用的方法
 * <p>
 * 收到客户端消息后调用的方法
 *
 * @param message 客户端发送过来的消息
 * <p>
 * 连接关闭调用的方法
 * @param sid
 * <p>
 * 群发
 * @param message
 *//*
@Slf4j
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    //存放会话对象
    private static Map<String, Session> sessionMap = new HashMap();

    *//**
 * 连接建立成功调用的方法
 *//*
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        log.info("客户端：{}建立连接", sid);
        sessionMap.put(sid, session);
    }

    *//**
 * 收到客户端消息后调用的方法
 *
 * @param message 客户端发送过来的消息
 *//*
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.info("收到来自客户端：{}的信息:", message);
    }

    *//**
 * 连接关闭调用的方法
 *
 * @param sid
 *//*
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        log.info("连接断开:{}", sid);
        sessionMap.remove(sid);
    }

    *//**
 * 群发
 *
 * @param message
 *//*
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}*/
