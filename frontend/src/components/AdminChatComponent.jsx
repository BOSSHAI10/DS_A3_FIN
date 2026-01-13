import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

const AdminChatComponent = () => {
    const [users, setUsers] = useState([]);
    const [selectedUser, setSelectedUser] = useState(null);
    const [messages, setMessages] = useState([]);
    const [adminInput, setAdminInput] = useState('');
    const stompClientRef = useRef(null);
    const messagesEndRef = useRef(null);

    // 1. Încarcă lista de useri care au scris

    const fetchUsers = () => {
        // Folosim axios configurat global sau calea completă
        axios.get(`http://${window.location.hostname}/chat/admin/users`)
            .then(res => setUsers(res.data))
            .catch(err => console.error("Err fetch users chat:", err));
    };

    // 2. Când selectezi un user, încarcă istoricul
    const handleSelectUser = (userId) => {
        setSelectedUser(userId);
        axios.get(`http://${window.location.hostname}/chat/admin/history/${userId}`)
            .then(res => setMessages(res.data))
            .catch(err => console.error("Err fetch history:", err));
    };

    // 3. Conectare WS (doar pentru a trimite, primirea e opțională aici momentan)
    const connectWebSocket = () => {
        const socket = new SockJS(`http://${window.location.hostname}/ws`);
        const client = Stomp.over(socket);
        client.debug = null;

        client.connect({}, () => {
            stompClientRef.current = client;
        });
    };

    // 4. Trimite mesaj ca ADMIN
    const sendReply = () => {
        if (!adminInput.trim() || !selectedUser) return;

        const replyMsg = {
            userId: selectedUser, // Destinatarul
            content: adminInput,
            senderId: "ADMIN",
            msgType: "ADMIN_REPLY"
        };

        // Trimitem pe ruta specială de admin din Java
        stompClientRef.current.send("/app/admin/reply", {}, JSON.stringify(replyMsg));

        // Adăugăm local în listă
        setMessages(prev => [...prev, { ...replyMsg, timestamp: new Date().toISOString() }]);
        setAdminInput('');
    };

    useEffect(() => {
        fetchUsers();
        connectWebSocket(); // Adminul ascultă global sau pe un topic special dacă vrei real-time updates la listă

        return () => {
            if (stompClientRef.current) stompClientRef.current.disconnect();
        };
    }, []);

    // Scroll automat
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    return (
        <div className="admin-chat-layout">
            {/* Sidebar Useri */}
            <div className="users-list-panel">
                <h4>Inbox Clienti</h4>
                <div className="users-list">
                    {users.length === 0 && <p className="no-data">Niciun mesaj încă.</p>}
                    {users.map(u => (
                        <div
                            key={u}
                            className={`user-item ${selectedUser === u ? 'active' : ''}`}
                            onClick={() => handleSelectUser(u)}
                        >
                            <div className="user-avatar">👤</div>
                            <div className="user-info">
                                <span className="user-id-text">{u}</span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* Chat Area */}
            <div className="chat-conversation-panel">
                {!selectedUser ? (
                    <div className="no-selection">Selectează un utilizator pentru a răspunde.</div>
                ) : (
                    <>
                        <div className="chat-header-admin">
                            Conversație cu: <strong>{selectedUser}</strong>
                        </div>

                        <div className="messages-list">
                            {messages.map((msg, idx) => {
                                const isAdmin = msg.senderId === 'ADMIN' || msg.senderId === 'System-AI';
                                const isBot = msg.senderId === 'System-AI';

                                return (
                                    <div key={idx} className={`msg-row ${isAdmin ? 'row-right' : 'row-left'}`}>
                                        <div className={`msg-bubble ${isBot ? 'bot' : (isAdmin ? 'admin' : 'client')}`}>
                                            <small className="sender-name">{msg.senderId}</small>
                                            <div>{msg.content}</div>
                                        </div>
                                    </div>
                                );
                            })}
                            <div ref={messagesEndRef} />
                        </div>

                        <div className="reply-area">
                            <input
                                type="text"
                                placeholder="Răspunde clientului..."
                                value={adminInput}
                                onChange={e => setAdminInput(e.target.value)}
                                onKeyPress={e => e.key === 'Enter' && sendReply()}
                            />
                            <button onClick={sendReply}>Trimite</button>
                        </div>
                    </>
                )}
            </div>

            <style>{`
                .admin-chat-layout { display: flex; height: 80vh; background: white; border: 1px solid #ddd; border-radius: 8px; overflow: hidden; }
                
                .users-list-panel { width: 300px; border-right: 1px solid #eee; display: flex; flex-direction: column; background: #f8f9fa; }
                .users-list-panel h4 { padding: 15px; margin: 0; border-bottom: 1px solid #ddd; background: #e9ecef; }
                .users-list { flex: 1; overflow-y: auto; }
                .user-item { padding: 15px; cursor: pointer; border-bottom: 1px solid #eee; display: flex; align-items: center; gap: 10px; transition: background 0.2s; }
                .user-item:hover { background: #e2e6ea; }
                .user-item.active { background: #0d6efd; color: white; }
                .user-avatar { font-size: 1.2rem; }
                .user-id-text { font-size: 0.85rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

                .chat-conversation-panel { flex: 1; display: flex; flex-direction: column; }
                .no-selection { flex: 1; display: flex; align-items: center; justify-content: center; color: #888; }
                
                .chat-header-admin { padding: 15px; border-bottom: 1px solid #eee; background: #fff; font-size: 1.1rem; }
                
                .messages-list { flex: 1; padding: 20px; overflow-y: auto; background: #f1f3f5; display: flex; flex-direction: column; gap: 10px; }
                
                .msg-row { display: flex; width: 100%; }
                .row-left { justify-content: flex-start; }
                .row-right { justify-content: flex-end; }
                
                .msg-bubble { max-width: 70%; padding: 10px 15px; border-radius: 12px; font-size: 0.9rem; position: relative; }
                .msg-bubble .sender-name { display: block; font-size: 0.7rem; opacity: 0.7; margin-bottom: 3px; font-weight: bold; }
                
                .msg-bubble.client { background: white; border: 1px solid #ddd; color: #333; border-bottom-left-radius: 2px; }
                .msg-bubble.admin { background: #0d6efd; color: white; border-bottom-right-radius: 2px; }
                .msg-bubble.bot { background: #6c757d; color: white; border-bottom-right-radius: 2px; font-style: italic; }

                .reply-area { padding: 15px; background: white; border-top: 1px solid #ddd; display: flex; gap: 10px; }
                .reply-area input { flex: 1; padding: 10px; border: 1px solid #ced4da; border-radius: 4px; }
                .reply-area button { padding: 0 20px; background: #198754; color: white; border: none; border-radius: 4px; cursor: pointer; }
            `}</style>
        </div>
    );
};

export default AdminChatComponent;