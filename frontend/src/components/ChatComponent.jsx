import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

const ChatComponent = () => {
    // --- STATE-URI CHAT ---
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [isConnected, setIsConnected] = useState(false);
    const [userId, setUserId] = useState('');
    const [isTyping, setIsTyping] = useState(false);

    // --- STATE PENTRU MODALE ---
    // 1. Modala de confirmare (cea existentă, când primești răspuns)
    const [showInfoModal, setShowInfoModal] = useState(false);
    const [infoMessage, setInfoMessage] = useState('');

    // 2. Modala de INPUT pentru contactare admin (NOUĂ)
    const [showContactModal, setShowContactModal] = useState(false);
    const [contactMessage, setContactMessage] = useState('');

    const messagesEndRef = useRef(null);
    const stompClientRef = useRef(null);

    // --- 1. LOGICA DE CONEXIUNE ---

    const handleIncomingMessage = (msg) => {
        // Dacă e răspuns automat de tip ADMIN_REQUEST (confirmare de la server)
        if (msg.msgType === 'ADMIN_REQUEST' || (msg.content && msg.content.includes("ADMIN_REQUEST"))) {
            const cleanMsg = msg.content.replace("ADMIN_REQUEST:", "").trim();
            setInfoMessage(cleanMsg);
            setShowInfoModal(true);
        }
        setMessages(prev => [...prev, msg]);
    };

    const connectWebSocket = () => {
        const socket = new SockJS(`http://${window.location.hostname}/ws`);
        const client = Stomp.over(socket);

        client.connect({}, (frame) => {
            console.log('Chat Conectat:', frame);
            setIsConnected(true);
            stompClientRef.current = client;

            // Ne abonăm la topicul dedicat pentru răspunsurile de la bot/admin
            client.subscribe(`/topic/reply/${userId}`, (message) => {
                if (message.body) {
                    const receivedMsg = JSON.parse(message.body);
                    console.log('Mesaj nou de la sistem/admin:', receivedMsg);
                    handleIncomingMessage(receivedMsg);
                }
            });

            // Keep the existing subscriptions for backward compatibility
            client.subscribe('/topic/messages', (message) => {
                if (message.body) {
                    const receivedMsg = JSON.parse(message.body);
                    handleIncomingMessage(receivedMsg);
                }
            });

            client.subscribe('/user/queue/private', (message) => {
                if (message.body) {
                    const receivedMsg = JSON.parse(message.body);
                    handleIncomingMessage(receivedMsg);
                }
            });

        }, (error) => {
            console.error('Eroare STOMP:', error);
            setIsConnected(false);
            setTimeout(connectWebSocket, 5000);
        });
    };

    // --- 2. LOGICA DE TRIMITERE ---

    const sendMessage = (textToSend = null) => {
        const content = typeof textToSend === 'string' ? textToSend : input;

        if (!content || !content.trim() || !stompClientRef.current || !isConnected) return;

        const chatMessage = {
            senderId: userId,
            content: content,
            timestamp: new Date().toISOString(),
            msgType: 'USER_MESSAGE'
        };

        setMessages(prev => [...prev, chatMessage]);

        try {
            stompClientRef.current.send("/app/chat", {}, JSON.stringify(chatMessage));
            if (!textToSend) setInput('');
            setIsTyping(false);
        } catch (error) {
            console.error("Failed to send message via STOMP:", error);
        }
    };

    // --- 3. FUNCȚIE NOUĂ: TRIMITE CĂTRE ADMIN DIN MODALĂ ---
    const handleSendToAdmin = () => {
        if (!contactMessage.trim()) return;

        // Trimitem un mesaj special care începe cu un prefix,
        // sau backend-ul îl va detecta ca "contactează administratorul" dacă modificăm regula,
        // dar cel mai simplu e să trimitem textul și să îl afișăm în chat.

        // Putem prefixa pentru claritate în backend, sau trimitem textul brut
        // Vom trimite textul brut, dar în backend regula "contact" va prinde doar cuvinte cheie.
        // Ca să fim siguri că ajunge la Admin, putem trimite:
        // "Contact Admin: [mesajul userului]"

        const messagePayload = `Contact Admin: ${contactMessage}`;
        sendMessage(messagePayload);

        setContactMessage('');
        setShowContactModal(false);
    };

    // Handlers UI
    const handleTyping = (e) => {
        setInput(e.target.value);
        if (!isTyping) {
            setIsTyping(true);
            setTimeout(() => setIsTyping(false), 2000);
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') sendMessage();
    };

    // --- 4. EFFECTS ---

    useEffect(() => {
        const savedUserId = localStorage.getItem('user_id');
        if (savedUserId) {
            setUserId(savedUserId);
        }
        return () => {
            if (stompClientRef.current && stompClientRef.current.connected) {
                stompClientRef.current.disconnect();
                stompClientRef.current = null;
            }
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    useEffect(() => {
        if (userId && !stompClientRef.current) {
            connectWebSocket();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [userId]);

    useEffect(() => {
        if (userId && stompClientRef.current && stompClientRef.current.connected) {
            // Re-subscribe to the user-specific topic when userId is available
            stompClientRef.current.subscribe(`/topic/reply/${userId}`, (message) => {
                if (message.body) {
                    const receivedMsg = JSON.parse(message.body);
                    console.log('Mesaj primit pe reply topic:', receivedMsg);
                    handleIncomingMessage(receivedMsg);
                }
            });
        }
    }, [userId]);


    // --- 5. RENDERIZARE ---
    return (
        <div className="chat-layout">
            <div className="chat-main-container">

                {/* STÂNGA: CHAT AREA */}
                <div className="chat-left-column">
                    <div className="chat-header">
                        <div className="header-info">
                            <h3>Asistent Energie</h3>
                            <span className={`status-badge ${isConnected ? 'online' : 'offline'}`}>
                                {isConnected ? 'Conectat' : 'Reconectare...'}
                            </span>
                        </div>
                    </div>

                    <div className="chat-messages">
                        {messages.length === 0 && (
                            <div className="empty-state">
                                <p>Salut! Alege o întrebare din dreapta sau scrie un mesaj.</p>
                            </div>
                        )}

                        {messages.map((msg, idx) => {
                            const isMe = msg.senderId === userId;
                            let bubbleClass = 'bubble-bot';
                            let messageBadge = null;

                            if (isMe) {
                                const text = msg.content ? msg.content.toLowerCase() : "";
                                if (text.includes('contact admin') || text.includes('contactează administratorul')) {
                                    bubbleClass = 'bubble-admin'; // Roz
                                    messageBadge = <span className="message-badge admin-badge">Admin</span>;
                                } else {
                                    bubbleClass = 'bubble-user'; // Albastru
                                }
                            } else {
                                // Mesaje de la bot/admin
                                if (msg.senderId === 'System-AI') {
                                    // Nu mai afișăm badge pentru AI
                                    bubbleClass = 'bubble-bot';
                                } else if (msg.senderId === 'ADMIN') {
                                    messageBadge = <span className="message-badge admin-reply-badge">Admin</span>;
                                    bubbleClass = 'bubble-bot';
                                } else {
                                    bubbleClass = 'bubble-bot';
                                }
                            }

                            return (
                                <div key={idx} className={`message-row ${isMe ? 'my-message-row' : 'other-message-row'}`}>
                                    <div className={`message-bubble ${bubbleClass}`}>
                                        {messageBadge}
                                        <div className="message-content">{msg.content}</div>
                                        <div className="message-time">
                                            {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                        </div>
                                    </div>
                                </div>
                            );
                        })}
                        <div ref={messagesEndRef} />
                    </div>

                    <div className="chat-input-area">
                        <input
                            type="text"
                            value={input}
                            onChange={handleTyping}
                            onKeyPress={handleKeyPress}
                            placeholder={isConnected ? "Scrie un mesaj..." : "Se conectează..."}
                            disabled={!isConnected}
                            className="chat-input"
                        />
                        <button
                            onClick={() => sendMessage()}
                            disabled={!isConnected || !input.trim()}
                            className="send-btn"
                        >
                            ➤
                        </button>
                    </div>
                </div>

                {/* DREAPTA: SUGESTII */}
                <div className="chat-right-column">
                    <div className="suggestions-header">
                        <h4>Întrebări frecvente</h4>
                    </div>

                    <div className="suggestions-list">
                        <button className="suggestion-btn" onClick={() => sendMessage('Cum pot vedea istoricul consumului?')} disabled={!isConnected}>📊 Cum pot vedea istoricul consumului?</button>
                        <button className="suggestion-btn" onClick={() => sendMessage('Cum adaug un dispozitiv nou?')} disabled={!isConnected}>➕ Cum adaug un dispozitiv nou?</button>
                        <button className="suggestion-btn" onClick={() => sendMessage('Primesc notificări dacă consum prea mult?')} disabled={!isConnected}>🔔 Primesc notificări dacă consum prea mult?</button>
                        <button className="suggestion-btn" onClick={() => sendMessage('Ce fac dacă un senzor nu merge?')} disabled={!isConnected}>🛠️ Ce fac dacă un senzor nu merge?</button>
                        <button className="suggestion-btn" onClick={() => sendMessage('Cum pot economisi energie?')} disabled={!isConnected}>💰 Cum pot economisi energie?</button>
                        <button className="suggestion-btn" onClick={() => sendMessage('Pot schimba parola contului?')} disabled={!isConnected}>🔑 Pot schimba parola contului?</button>
                        <button className="suggestion-btn" onClick={() => sendMessage('Care este prețul per kWh?')} disabled={!isConnected}>🏷️ Care este prețul per kWh?</button>
                        <button className="suggestion-btn" onClick={() => sendMessage('Cine este administratorul sistemului?')} disabled={!isConnected}>👨‍💼 Cine este administratorul sistemului?</button>
                        <button className="suggestion-btn" onClick={() => sendMessage('Ce este un Smart Meter?')} disabled={!isConnected}>📟 Ce este un Smart Meter?</button>
                        <button className="suggestion-btn" onClick={() => sendMessage('Care este programul de suport?')} disabled={!isConnected}>🕒 Care este programul de suport?</button>
                    </div>

                    {/* Buton Admin (Deschide Modala) */}
                    <div className="admin-btn-container">
                        <button
                            className="admin-contact-btn"
                            onClick={() => setShowContactModal(true)}
                            disabled={!isConnected}
                        >
                            📞 Contactează administratorul
                        </button>
                    </div>
                </div>
            </div>

            {/* --- MODALĂ 1: CONFIRMARE (Info de la server) --- */}
            {showInfoModal && (
                <div className="admin-modal-overlay">
                    <div className="admin-modal">
                        <h4>Mesaj de la Sistem</h4>
                        <p>{infoMessage}</p>
                        <div className="modal-actions">
                            <button className="confirm-btn" onClick={() => setShowInfoModal(false)}>Am înțeles</button>
                        </div>
                    </div>
                </div>
            )}

            {/* --- MODALĂ 2: INPUT PENTRU ADMIN (Formular) --- */}
            {showContactModal && (
                <div className="admin-modal-overlay">
                    <div className="contact-modal">
                        <h4>Contactează Administratorul</h4>
                        <p className="modal-subtitle">Descrie problema ta mai jos:</p>

                        <textarea
                            className="contact-textarea"
                            rows="4"
                            placeholder="Ex: Senzorul din bucătărie nu mai transmite date..."
                            value={contactMessage}
                            onChange={(e) => setContactMessage(e.target.value)}
                        />

                        <div className="modal-actions">
                            <button className="cancel-btn" onClick={() => setShowContactModal(false)}>
                                Anulare
                            </button>
                            <button className="send-admin-btn" onClick={handleSendToAdmin}>
                                Trimite
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* CSS */}
            <style>{`
                /* Layout General */
                .chat-layout { display: flex; justify-content: center; padding: 20px; width: 100%; height: 85vh; background-color: #f8f9fa; }
                .chat-main-container { display: flex; flex-direction: row; width: 100%; max-width: 1200px; background: white; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); overflow: hidden; height: 100%; }

                /* STÂNGA */
                .chat-left-column { flex: 1; display: flex; flex-direction: column; border-right: 1px solid #dee2e6; }
                .chat-header { padding: 15px 20px; background-color: #0d6efd; color: white; }
                .header-info h3 { margin: 0; font-size: 1.2rem; }
                .status-badge { font-size: 0.8rem; margin-left: 10px; padding: 4px 8px; border-radius: 12px; background: rgba(255,255,255,0.2); }
                
                .chat-messages { flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 12px; background-color: #f1f3f5; }
                .empty-state { text-align: center; color: #adb5bd; margin-top: 50px; font-style: italic; }

                /* BULE */
                .message-row { display: flex; width: 100%; }
                .my-message-row { justify-content: flex-end; }
                .other-message-row { justify-content: flex-start; }
                .message-bubble { max-width: 70%; padding: 10px 15px; border-radius: 15px; position: relative; word-wrap: break-word; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
                .bubble-user { background-color: #e7f5ff; color: #004085; border-bottom-right-radius: 2px; }
                .bubble-admin { background-color: #fff0f6; color: #a61e4d; border: 1px solid #fcc2d7; border-bottom-right-radius: 2px; }
                .bubble-bot { background-color: #ffffff; color: #212529; border-bottom-left-radius: 2px; }
                .message-time { font-size: 0.7rem; margin-top: 5px; text-align: right; opacity: 0.7; }

                /* Message Badges */
                .message-badge { 
                    position: absolute; 
                    top: -8px; 
                    right: -8px; 
                    font-size: 0.6rem; 
                    padding: 2px 6px; 
                    border-radius: 8px; 
                    font-weight: bold; 
                    text-transform: uppercase;
                }
                .bot-badge { 
                    background-color: #28a745; 
                    color: white; 
                }
                .admin-badge { 
                    background-color: #dc3545; 
                    color: white; 
                }
                .admin-reply-badge { 
                    background-color: #fd7e14; 
                    color: white; 
                }
                .admin-contact-btn { width: 100%; background-color: white; color: #dc3545; border: 2px solid #dc3545; border-radius: 8px; padding: 12px; font-weight: 700; cursor: pointer; text-transform: uppercase; font-size: 0.85rem; transition: all 0.2s; }
                .admin-contact-btn:hover { background-color: #dc3545; color: white; }

                .chat-input-area { padding: 15px; background: white; border-top: 1px solid #dee2e6; display: flex; gap: 10px; }
                .chat-input { flex: 1; padding: 10px 15px; border: 1px solid #ced4da; border-radius: 25px; outline: none; }
                .send-btn { background-color: #0d6efd; color: white; border: none; border-radius: 25px; padding: 0 25px; cursor: pointer; }
                .send-btn:disabled { background-color: #6c757d; }

                /* DREAPTA */
                .chat-right-column { width: 320px; background-color: #fff; display: flex; flex-direction: column; padding: 15px; }
                .suggestions-header h4 { margin-top: 0; margin-bottom: 15px; font-size: 1rem; color: #495057; border-bottom: 2px solid #f1f3f5; padding-bottom: 10px; }
                .suggestions-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 8px; padding-right: 5px; }
                .suggestion-btn { background: #f8f9fa; border: 1px solid #ced4da; border-radius: 8px; padding: 10px; font-size: 0.85rem; color: #495057; cursor: pointer; text-align: left; transition: all 0.2s; width: 100%; }
                .suggestion-btn:hover { background-color: #e9ecef; border-color: #adb5bd; transform: translateX(2px); }
                .suggestion-btn:disabled { opacity: 0.6; cursor: not-allowed; }
                .suggestions-list::-webkit-scrollbar { width: 6px; }
                .suggestions-list::-webkit-scrollbar-thumb { background: #ced4da; border-radius: 3px; }

                /* Buton Admin */
                .admin-btn-container { margin-top: 15px; border-top: 1px solid #dee2e6; padding-top: 15px; }
                .admin-contact-btn { width: 100%; background-color: white; color: #dc3545; border: 2px solid #dc3545; border-radius: 8px; padding: 12px; font-weight: 700; cursor: pointer; text-transform: uppercase; font-size: 0.85rem; transition: all 0.2s; }
                .admin-contact-btn:hover { background-color: #dc3545; color: white; }

                /* MODALE */
                .admin-modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 9999; }
                
                /* Modala Informativă */
                .admin-modal { background: white; padding: 25px; border-radius: 8px; width: 90%; max-width: 400px; text-align: center; box-shadow: 0 10px 25px rgba(0,0,0,0.2); }
                .admin-modal h4 { color: #0d6efd; margin-bottom: 15px; }
                
                /* Modala Contact (Input) */
                .contact-modal { background: white; padding: 30px; border-radius: 12px; width: 90%; max-width: 500px; box-shadow: 0 10px 30px rgba(0,0,0,0.3); display: flex; flex-direction: column; }
                .contact-modal h4 { color: #dc3545; margin-bottom: 5px; }
                .modal-subtitle { color: #6c757d; font-size: 0.9rem; margin-bottom: 20px; }
                .contact-textarea { width: 100%; padding: 12px; border: 1px solid #ced4da; border-radius: 8px; font-family: inherit; font-size: 0.95rem; resize: vertical; outline: none; margin-bottom: 20px; }
                .contact-textarea:focus { border-color: #dc3545; box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1); }
                
                .modal-actions { display: flex; justify-content: flex-end; gap: 10px; }
                .confirm-btn { background-color: #0d6efd; color: white; border: none; padding: 8px 20px; border-radius: 6px; cursor: pointer; }
                
                .cancel-btn { background-color: white; color: #6c757d; border: 1px solid #ced4da; padding: 8px 20px; border-radius: 6px; cursor: pointer; transition: all 0.2s; }
                .cancel-btn:hover { background-color: #f1f3f5; }
                
                .send-admin-btn { background-color: #dc3545; color: white; border: none; padding: 8px 25px; border-radius: 6px; cursor: pointer; font-weight: 600; transition: all 0.2s; }
                .send-admin-btn:hover { background-color: #bb2d3b; }

                @media (max-width: 768px) {
                    .chat-main-container { flex-direction: column; }
                    .chat-right-column { width: 100%; height: 250px; border-left: none; border-top: 1px solid #dee2e6; }
                }
            `}</style>
        </div>
    );
};

export default ChatComponent;