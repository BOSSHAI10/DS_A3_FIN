import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

const NotificationsComponent = () => {
    const [notifications, setNotifications] = useState([]);
    const stompClientRef = useRef(null);



    const connectWebSocket = (userId) => {
        // Conectare SockJS prin HTTP (port 80 -> Traefik -> 8086)
        // window.location.hostname ia automat IP-ul sau domeniul din bara de adresă
        const socket = new SockJS(`http://${window.location.hostname}/ws`);
        const client = Stomp.over(socket);

        // client.debug = null; // Poți decomenta pentru a reduce logurile

        client.connect({}, (frame) => {
            console.log('Notifications Connected');
            stompClientRef.current = client;

            // Abonare la coada specifică utilizatorului
            // Verifică în backend dacă trimiți la /user/... sau direct pe topic
            // De obicei: @SendToUser("/queue/notifications") -> client subscrie la /user/queue/notifications
            client.subscribe('/user/queue/notifications', (message) => {
                if (message.body) {
                    try {
                        const notification = JSON.parse(message.body);
                        addNotification(notification);
                    } catch (e) {
                        console.error("Eroare parsare notificare", e);
                    }
                }
            });

        }, (error) => {
            console.error('Notifications WS error:', error);
            setTimeout(() => connectWebSocket(userId), 5000);
        });
    };

    const addNotification = (notification) => {
        const id = Date.now();
        // Extragem textul corect din mesajul JSON
        const messageText = notification.message || JSON.stringify(notification);

        setNotifications(prev => [...prev, { message: messageText, id }]);

        // Auto-close după 6 secunde
        setTimeout(() => {
            setNotifications(prev => prev.filter(n => n.id !== id));
        }, 6000);
    };

    useEffect(() => {
        const savedUserId = localStorage.getItem('user_id');
        if (savedUserId) {
            connectWebSocket(savedUserId);
        }

        return () => {
            if (stompClientRef.current) {
                try {
                    stompClientRef.current.disconnect();
                } catch (e) {
                    console.log("Deconectare forțată", e);
                }
            }
        };
    }, []);

    return (
        <div className="notifications-container">
            {notifications.map(note => (
                <div key={note.id} className="notification-toast">
                    <div className="notification-icon">🔔</div>
                    <div className="notification-content">
                        <strong>Notificare Nouă</strong>
                        <p>{note.message}</p>
                    </div>
                </div>
            ))}

            <style>{`
                .notifications-container {
                    position: fixed;
                    top: 80px; /* Sub header-ul principal */
                    right: 20px;
                    z-index: 9999;
                    display: flex;
                    flex-direction: column;
                    gap: 10px;
                    pointer-events: none;
                }
                .notification-toast {
                    background-color: #ffffff;
                    border-left: 5px solid #ffc107;
                    color: #333;
                    padding: 15px;
                    border-radius: 4px;
                    box-shadow: 0 4px 15px rgba(0,0,0,0.2);
                    min-width: 300px;
                    max-width: 400px;
                    display: flex;
                    align-items: flex-start;
                    gap: 12px;
                    animation: slideIn 0.3s ease-out;
                    pointer-events: auto;
                }
                .notification-icon {
                    font-size: 1.2rem;
                }
                .notification-content strong {
                    display: block;
                    margin-bottom: 4px;
                    font-size: 0.9rem;
                    color: #856404;
                }
                .notification-content p {
                    margin: 0;
                    font-size: 0.85rem;
                }
                @keyframes slideIn {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
            `}</style>
        </div>
    );
};

export default NotificationsComponent;