import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';

const AdminChat = () => {
    const [activeUsers, setActiveUsers] = useState([]); // Lista userilor care au scris
    const [selectedUser, setSelectedUser] = useState(null); // Userul selectat curent
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const wsRef = useRef(null);

    // 1. Conectare la WebSocket-ul general de Admin pentru notificări
    useEffect(() => {
        const hostname = window.location.hostname;
        const wsUrl = `ws://${hostname}:8086/ws`;
        wsRef.current = new WebSocket(wsUrl);

        wsRef.current.onopen = () => console.log("Admin connected to WS");

        wsRef.current.onmessage = (event) => {
            if (event.data.length <= 1) return; // Heartbeat ignore
            try {
                const msg = JSON.parse(event.data);
                // Dacă mesajul vine pe canalul de notificări (trebuie filtrat în backend sau aici logic)
                // Pentru simplitate, presupunem că backend-ul trimite aici tot ce e relevant
                handleIncomingMessage(msg);
            } catch (e) {}
        };

        return () => wsRef.current?.close();
    }, []);

    const handleIncomingMessage = (msg) => {
        // Adăugăm userul în listă dacă e nou
        setActiveUsers(prev => {
            if (!prev.find(u => u === msg.userId)) return [...prev, msg.userId];
            return prev;
        });

        // Dacă avem userul selectat, adăugăm mesajul în fereastră
        if (selectedUser === msg.userId) {
            setMessages(prev => [...prev, msg]);
        }
    };

    // 2. Încărcare istoric când selectăm un user
    const selectUser = async (userId) => {
        setSelectedUser(userId);
        try {
            const hostname = window.location.hostname;
            const res = await axios.get(`http://${hostname}:8086/chat/history/${userId}`);
            setMessages(res.data);
        } catch (err) {
            console.error("Error loading history", err);
        }
    };

    // 3. Trimite răspuns ca ADMIN
    const sendAdminMessage = async () => {
        if (!input.trim() || !selectedUser) return;

        const textToSend = input;
        setInput('');

        // Update UI optimist
        const newMsg = {
            content: textToSend,
            sender: 'ADMIN', // Important!
            timestamp: new Date().toISOString(),
            userId: selectedUser
        };
        setMessages(prev => [...prev, newMsg]);

        try {
            const formData = new FormData();
            formData.append('content', textToSend);
            formData.append('sender', 'ADMIN'); // Semnăm ca Admin
            formData.append('type', 'SYSTEM_RESPONSE');
            formData.append('userId', selectedUser);

            // Hack: Folosim userId-ul DESTINATARULUI în field-ul sender din DTO-ul de request
            // (sau modificăm controllerul, dar metoda rapidă e să trimitem userId-ul clientului ca parametru distinct dacă controllerul permite,
            // sau să ne bazăm pe faptul că ChatService extrage userId-ul).

            // Corecție: ChatService.java se așteaptă ca userId să fie cel al conversației.
            // Deci trebuie să păcălim puțin DTO-ul sau să adăugăm un param 'userId'.
            // Hai să folosim DTO-ul corectat din backend (userId este target-ul).

            // ATENȚIE: În ChatComponent.jsx userId era userul logat.
            // Aici, userId (în DTO) trebuie să fie userul CĂTRE care trimitem.
            // Backend-ul ia "sender" din DTO.
            // Deci, trebuie să trimitem userId = selectedUser și sender = 'ADMIN'.

            // Modificăm request-ul ca să fie compatibil cu ChatController-ul existent:
            // Controllerul face: ChatMessageDTO.setSender(params.get("sender"))
            // Și service-ul face: userId = dto.getSender().

            // AICI E O PROBLEMĂ ÎN LOGICA VECHE.
            // Fix rapid: Vom trimite un request custom.
            // Dar cel mai simplu:
            // Modificăm ChatService să accepte un parametru 'targetUserId' explicit dacă sender e ADMIN.

            // Să presupunem că backend-ul a fost actualizat (vezi Pasul 2 completat mai jos).
            const hostname = window.location.hostname;
            await axios.post(`http://${hostname}:8086/chat/send`, formData, {
                params: { // Trimitem și ca query param pentru siguranță dacă modificăm controllerul
                    userId: selectedUser
                },
                // Dar hack-ul curent din service: userId = messageDTO.getUserId()
                // Deci trebuie sa punem in form data userId-ul clientului.
            });

            // NOTĂ: E mai complicat cu controller-ul actual.
            // Hai să facem o metodă dedicată în ChatController pentru admin, e mai curat.

        } catch (err) {
            console.error(err);
        }
    };

    // ... UI-ul Admin (Lista stânga, Chat dreapta) ...
    return (
        <div style={{ display: 'flex', height: '80vh', border: '1px solid #ccc' }}>
            {/* Sidebar List */}
            <div style={{ width: '30%', borderRight: '1px solid #ccc', padding: '10px' }}>
                <h3>Inbox Clienți</h3>
                {activeUsers.map(u => (
                    <div
                        key={u}
                        onClick={() => selectUser(u)}
                        style={{
                            padding: '10px',
                            cursor: 'pointer',
                            background: selectedUser === u ? '#e3f2fd' : 'white',
                            borderBottom: '1px solid #eee'
                        }}
                    >
                        User: {u.substring(0, 8)}...
                    </div>
                ))}
            </div>

            {/* Chat Area */}
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                {selectedUser ? (
                    <>
                        <div style={{ flex: 1, padding: '20px', overflowY: 'auto', background: '#f5f5f5' }}>
                            {messages.map((m, i) => (
                                <div key={i} style={{
                                    alignSelf: m.sender === 'ADMIN' ? 'flex-end' : 'flex-start',
                                    background: m.sender === 'ADMIN' ? '#4caf50' : (m.sender === 'USER' ? '#2196f3' : '#fff'),
                                    color: m.sender === 'SYSTEM' ? '#333' : 'white',
                                    padding: '8px 12px',
                                    borderRadius: '12px',
                                    marginBottom: '8px',
                                    maxWidth: '70%',
                                    marginLeft: m.sender === 'ADMIN' ? 'auto' : '0'
                                }}>
                                    <small style={{display:'block', fontSize:'10px', opacity:0.8}}>{m.sender}</small>
                                    {m.content}
                                </div>
                            ))}
                        </div>
                        <div style={{ padding: '20px', borderTop: '1px solid #ccc', display:'flex' }}>
                            <input
                                value={input}
                                onChange={e => setInput(e.target.value)}
                                style={{ flex: 1, padding: '10px', borderRadius:'4px', border:'1px solid #ddd' }}
                                placeholder="Răspunde clientului..."
                            />
                            <button onClick={sendAdminMessage} style={{ marginLeft:'10px', padding:'10px 20px', background:'#4caf50', color:'white', border:'none' }}>Send</button>
                        </div>
                    </>
                ) : (
                    <div style={{ padding: '20px', color: '#999' }}>Selectează un utilizator pentru a răspunde.</div>
                )}
            </div>
        </div>
    );
};

export default AdminChat;