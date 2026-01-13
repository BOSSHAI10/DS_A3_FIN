import { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Modal, Button, Form, Badge } from 'react-bootstrap';
import { logout } from '../axios_helper';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

export default function Admin() {
    const navigate = useNavigate();
    const fileInputRef = useRef(null);
    const messagesEndRef = useRef(null);

    // --- STATE ---
    const [users, setUsers] = useState([]);
    const [devices, setDevices] = useState([]);
    const [adminMessages, setAdminMessages] = useState([]);
    const [selectedUserForDevice, setSelectedUserForDevice] = useState({});

    const [selectedTicketUserId, setSelectedTicketUserId] = useState(null);
    const [ticketHistory, setTicketHistory] = useState([]);
    const [replyText, setReplyText] = useState("");

    const [showViewModal, setShowViewModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [currentUser, setCurrentUser] = useState({ id: '', name: '', email: '', age: 0, role: 'USER' });

    const [showCreateDeviceModal, setShowCreateDeviceModal] = useState(false);
    const [showViewDeviceModal, setShowViewDeviceModal] = useState(false);
    const [showEditDeviceModal, setShowEditDeviceModal] = useState(false);
    const [currentDevice, setCurrentDevice] = useState({ id: '', name: '', consumption: 0, active: true, username: '' });
    const [newDevice, setNewDevice] = useState({ name: '', consumption: '' });

    // --- FETCH DATA ---
    const fetchData = async () => {
        const token = window.localStorage.getItem("auth_token");
        if (!token) { navigate("/login"); return; }
        const headers = { 'Authorization': `Bearer ${token}` };

        try {
            const [uRes, dRes, mRes] = await Promise.all([
                axios.get("/people", { headers }),
                axios.get("/devices", { headers }),
                axios.get('/chat/admin/requests', { headers })
            ]);
            setUsers(uRes.data);
            setDevices(dRes.data);
            const openMessages = mRes.data
                .map(m => ({ ...m, userId: m.userId || m.senderId }))
                .filter(m => m.userId && m.status !== 'RESOLVED');
            setAdminMessages(openMessages);
        } catch (err) {
            console.error("Fetch data failed:", err);
        }
    };

    // --- WEBSOCKET ---
    useEffect(() => {
        fetchData();
        let client = null;
        try {
            const socket = new SockJS('http://localhost/ws');
            client = Stomp.over(socket);
            const token = window.localStorage.getItem("auth_token");
            client.debug = () => {};

            client.connect({ 'Authorization': `Bearer ${token}` }, () => {
                client.subscribe('/topic/admin', (message) => {
                    if (message.body) {
                        const receivedMsg = JSON.parse(message.body);
                        const uiMsg = { ...receivedMsg, userId: receivedMsg.userId || receivedMsg.senderId };
                        setAdminMessages(prev => {
                            if (prev.some(m => m.id === uiMsg.id)) return prev;
                            return [uiMsg, ...prev];
                        });
                    }
                });
            });
        } catch (e) { console.debug("WS Init error", e); }

        return () => {
            if (client && client.connected) {
                try { client.disconnect(); } catch (err) { console.debug(err); }
            }
        };
    }, []);

    // --- HELPERS ---
    const getUserName = (id) => {
        const u = users.find(user => user.id === id);
        return u ? u.name : "ID: " + id?.substring(0, 8);
    };

    const formatMsg = (txt) => txt ? txt.replace(/Contact Admin:?\s?/gi, "").trim() : "";

    const getStatusText = (deviceUsername) => {
        if (!deviceUsername) return <span className="text-warning fw-bold">Neatribuit</span>;
        const user = users.find(u => u.email === deviceUsername);
        return <span className="text-success fw-bold">Atribuit: {user ? user.name : deviceUsername}</span>;
    };

    // --- HANDLERS ---
    const handleLogout = () => { logout(); navigate("/login"); };

    const handleCreateUser = () => {
        const n = prompt("Nume:"); const e = prompt("Email:"); const a = prompt("Vârstă:");
        if (n && e && a) axios.post("/api/register", { name: n, email: e, age: parseInt(a), role: "USER", password: "1234" }).then(() => fetchData());
    };

    const handleDeleteUser = async (id) => {
        if (!window.confirm("Ștergi profilul și credentials?")) return;
        try {
            const headers = { 'Authorization': `Bearer ${window.localStorage.getItem("auth_token")}` };
            await axios.delete("/people/" + id, { headers });
            await axios.delete("/auth/delete/" + id, { headers }).catch(err => console.debug(err));
            fetchData();
        } catch (err) { console.error(err); }
    };

    const handleSaveUserChanges = async () => {
        try {
            const headers = { 'Authorization': `Bearer ${window.localStorage.getItem("auth_token")}` };
            await axios.post("/people", { ...currentUser, age: parseInt(currentUser.age) }, { headers });
            setShowEditModal(false); fetchData();
        } catch (err) { console.error(err); }
    };

    const handleDeviceEditChange = (e) => {
        const { name, value, type, checked } = e.target;
        setCurrentDevice(p => ({ ...p, [name]: type === 'checkbox' ? checked : value }));
    };

    const handleSaveDeviceChanges = async () => {
        try {
            await axios.put("/devices/" + currentDevice.id, { ...currentDevice, consumption: parseFloat(currentDevice.consumption) });
            setShowEditDeviceModal(false); fetchData();
        } catch (err) { console.error(err); }
    };

    const handleAssign = async (deviceId) => {
        const v = selectedUserForDevice[deviceId]; if (!v) return;
        try {
            if (v === "unassign") await axios.post("/devices/" + deviceId + "/unassign");
            else await axios.post("/devices/" + deviceId + "/assign/" + v);
            fetchData(); setSelectedUserForDevice(p => ({ ...p, [deviceId]: "" }));
        } catch (err) { console.error(err); }
    };

    // --- CHAT LOGIC ---
    const activeTickets = adminMessages.reduce((acc, msg) => {
        if (msg.msgType === 'BOT_RESPONSE') return acc;
        const uid = msg.userId;
        if (!acc[uid] || new Date(msg.timestamp) > new Date(acc[uid].timestamp)) acc[uid] = msg;
        return acc;
    }, {});
    const ticketsList = Object.values(activeTickets).sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

    const handleSelectTicket = async (userId) => {
        setSelectedTicketUserId(userId);
        try {
            const res = await axios.get("/chat/admin/history/" + userId);
            setTicketHistory(res.data.filter(m => m.msgType === 'USER_MESSAGE' || m.msgType === 'ADMIN_REPLY'));
            setTimeout(() => messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' }), 100);
        } catch (err) { console.error(err); }
    };

    const cardStyle = { border: "2px solid black", boxShadow: "5px 5px 5px black" };

    return (
        <div className="container mt-5 pb-5">
            <input type="file" ref={fileInputRef} style={{ display: 'none' }} accept=".json" />

            <div className="d-flex justify-content-between align-items-center mb-5 p-3 rounded bg-light border">
                <h1 className="fw-normal m-0">Admin Dashboard</h1>
                <div className="d-flex gap-3">
                    <Button variant="outline-primary" onClick={() => { /* Backup logic */ }}>💾 Backup</Button>
                    <Button variant="outline-warning" onClick={() => fileInputRef.current.click()}>📂 Restore</Button>
                    <Button variant="danger" onClick={handleLogout}>Logout</Button>
                </div>
            </div>

            <div className="row gx-5 gy-4">
                {/* LISTA UTILIZATORI */}
                <div className="col-md-6">
                    <div className="card h-100" style={cardStyle}>
                        <div className="card-header bg-light d-flex justify-content-between py-3">
                            <h5 className="mb-0 text-secondary">Utilizatori</h5>
                            <Button variant="success" size="sm" onClick={handleCreateUser}>+ Adaugă</Button>
                        </div>
                        <div className="list-group list-group-flush overflow-auto" style={{ maxHeight: '350px' }}>
                            {users.map(u => (
                                <div key={u.id} className="list-group-item d-flex justify-content-between align-items-center">
                                    <div><h6 className="mb-0 fw-bold">{u.name}</h6><small>{u.email}</small></div>
                                    <div className="btn-group">
                                        <button className="btn btn-outline-info btn-sm" onClick={() => { setCurrentUser(u); setShowViewModal(true); }}>Vezi</button>
                                        <button className="btn btn-outline-warning btn-sm" onClick={() => { setCurrentUser(u); setShowEditModal(true); }}>Edit</button>
                                        <button className="btn btn-outline-danger btn-sm" onClick={() => handleDeleteUser(u.id)}>Șterge</button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                {/* LISTA DISPOZITIVE */}
                <div className="col-md-6">
                    <div className="card h-100" style={cardStyle}>
                        <div className="card-header bg-light d-flex justify-content-between">
                            <h5>Dispozitive</h5>
                            <Button variant="success" size="sm" onClick={() => setShowCreateDeviceModal(true)}>+ Adaugă</Button>
                        </div>
                        <div className="card-body p-0 overflow-auto" style={{ maxHeight: '350px' }}>
                            {devices.map(d => (
                                <div key={d.id} className="p-3 border-bottom">
                                    <div className="d-flex justify-content-between align-items-center">
                                        <span className="fw-bold">{d.name} <Badge bg="info">{d.consumption} kW</Badge></span>
                                        <div className="btn-group">
                                            <button className="btn btn-outline-info btn-sm" onClick={() => { setCurrentDevice(d); setShowViewDeviceModal(true); }}>Vezi</button>
                                            <button className="btn btn-outline-warning btn-sm" onClick={() => { setCurrentDevice(d); setShowEditDeviceModal(true); }}>Edit</button>
                                            <button className="btn btn-outline-danger btn-sm" onClick={() => { if(window.confirm("Ștergi?")) axios.delete("/devices/"+d.id).then(fetchData); }}>Șterge</button>
                                        </div>
                                    </div>
                                    <select className="form-select form-select-sm mt-2" onChange={e => setSelectedUserForDevice({...selectedUserForDevice, [d.id]: e.target.value})}>
                                        <option value="">Atribuie...</option>
                                        <option value="unassign">Neatribuit</option>
                                        {users.map(u => <option key={u.id} value={u.email}>{u.name}</option>)}
                                    </select>
                                    <Button size="sm" variant="primary" className="mt-1 w-100" onClick={() => handleAssign(d.id)}>OK</Button>
                                    <div className="small text-muted mt-1">{getStatusText(d.username)}</div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                {/* CHAT SUPORT */}
                <div className="col-12 mt-4">
                    <div className="card" style={{ border: "2px solid #dc3545", boxShadow: "5px 5px 5px #dc3545" }}>
                        <div className="card-header bg-danger text-white py-3"><h5>📨 Mesaje Suport (Conversație)</h5></div>
                        <div className="card-body p-0 bg-white">
                            <div className="row g-0" style={{ height: '500px' }}>
                                <div className="col-md-4 border-end overflow-auto bg-light">
                                    {ticketsList.map(t => (
                                        <div key={t.userId} onClick={() => handleSelectTicket(t.userId)} className={`p-3 border-bottom cursor-pointer ${selectedTicketUserId === t.userId ? 'bg-primary text-white shadow-sm' : 'bg-white'}`} style={{ cursor: 'pointer' }}>
                                            <div className="d-flex justify-content-between"><strong>{getUserName(t.userId)}</strong><small>{new Date(t.timestamp).toLocaleTimeString()}</small></div>
                                            <p className="small mb-0 text-truncate opacity-75">{formatMsg(t.content)}</p>
                                        </div>
                                    ))}
                                </div>
                                <div className="col-md-8 d-flex flex-column bg-light">
                                    {selectedTicketUserId ? (
                                        <>
                                            <div className="bg-white p-3 border-bottom d-flex justify-content-between align-items-center">
                                                <h6 className="m-0 fw-bold">Chat: {getUserName(selectedTicketUserId)}</h6>
                                            </div>
                                            <div className="flex-grow-1 overflow-auto p-3 d-flex flex-column gap-3">
                                                {ticketHistory.map((m, i) => (
                                                    <div key={i} className={`d-flex ${m.senderId === 'ADMIN' ? 'justify-content-end' : 'justify-content-start'}`}>
                                                        <div className="d-flex flex-column">
                                                            <div className={`p-2 rounded shadow-sm ${m.senderId === 'ADMIN' ? 'bg-primary text-white' : 'bg-white border'}`}>
                                                                {formatMsg(m.content)}
                                                            </div>
                                                            <small className="text-muted mt-1" style={{ fontSize: '0.7rem' }}>{new Date(m.timestamp).toLocaleTimeString()}</small>
                                                        </div>
                                                    </div>
                                                ))}
                                                <div ref={messagesEndRef} />
                                            </div>
                                            <div className="p-3 bg-white border-top d-flex gap-2">
                                                <Form.Control value={replyText} onChange={e => setReplyText(e.target.value)} placeholder="Scrie un răspuns..." />
                                                <Button onClick={() => {
                                                    const p = { userId: selectedTicketUserId, content: replyText, msgType: "ADMIN_REPLY", senderId: "ADMIN" };
                                                    axios.post("/chat/send", p).then(() => { setReplyText(""); handleSelectTicket(selectedTicketUserId); });
                                                }}>Trimite</Button>
                                            </div>
                                        </>
                                    ) : <div className="m-auto text-muted">Alege o conversație.</div>}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* TOATE MODALELE SUNT AICI PENTRU A ELIMINA ESLINT ERRORS */}
            <Modal show={showViewModal} onHide={() => setShowViewModal(false)} centered>
                <Modal.Header closeButton><Modal.Title>Detalii User</Modal.Title></Modal.Header>
                <Modal.Body>
                    <p><strong>Nume:</strong> {currentUser.name}</p>
                    <p><strong>Email:</strong> {currentUser.email}</p>
                    <p><strong>Vârstă:</strong> {currentUser.age}</p>
                </Modal.Body>
                <Modal.Footer><Button variant="danger" onClick={() => setShowViewModal(false)}>Închide</Button></Modal.Footer>
            </Modal>

            <Modal show={showEditModal} onHide={() => setShowEditModal(false)} centered>
                <Modal.Header closeButton><Modal.Title>Editare Utilizator</Modal.Title></Modal.Header>
                <Modal.Body>
                    <Form.Group className="mb-3"><Form.Label>Nume</Form.Label><Form.Control name="name" value={currentUser.name} onChange={(e) => setCurrentUser({...currentUser, name: e.target.value})} /></Form.Group>
                    <Form.Group className="mb-3"><Form.Label>Email</Form.Label><Form.Control name="email" value={currentUser.email} onChange={(e) => setCurrentUser({...currentUser, email: e.target.value})} /></Form.Group>
                    <Form.Group className="mb-3"><Form.Label>Vârstă</Form.Label><Form.Control name="age" type="number" value={currentUser.age} onChange={(e) => setCurrentUser({...currentUser, age: e.target.value})} /></Form.Group>
                </Modal.Body>
                <Modal.Footer><Button variant="primary" onClick={handleSaveUserChanges}>Salvează</Button><Button variant="danger" onClick={() => setShowEditModal(false)}>Închide</Button></Modal.Footer>
            </Modal>

            <Modal show={showCreateDeviceModal} onHide={() => setShowCreateDeviceModal(false)} centered>
                <Modal.Header closeButton><Modal.Title>Adaugă Device</Modal.Title></Modal.Header>
                <Modal.Body>
                    <Form.Control className="mb-2" placeholder="Nume" value={newDevice.name} onChange={e => setNewDevice({...newDevice, name: e.target.value})} />
                    <Form.Control type="number" placeholder="Consum" value={newDevice.consumption} onChange={e => setNewDevice({...newDevice, consumption: e.target.value})} />
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="success" onClick={() => { axios.post('/devices', { ...newDevice, active: true }).then(() => { setShowCreateDeviceModal(false); fetchData(); }); }}>Creează</Button>
                    <Button variant="danger" onClick={() => setShowCreateDeviceModal(false)}>Închide</Button>
                </Modal.Footer>
            </Modal>

            <Modal show={showEditDeviceModal} onHide={() => setShowEditDeviceModal(false)} centered>
                <Modal.Header closeButton><Modal.Title>Editare Dispozitiv</Modal.Title></Modal.Header>
                <Modal.Body>
                    <Form.Group className="mb-3"><Form.Label>Nume</Form.Label>
                        <Form.Control name="name" value={currentDevice.name} onChange={handleDeviceEditChange} />
                    </Form.Group>
                    <Form.Group className="mb-3"><Form.Label>Consum</Form.Label>
                        <Form.Control name="consumption" type="number" value={currentDevice.consumption} onChange={handleDeviceEditChange} />
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer><Button variant="primary" onClick={handleSaveDeviceChanges}>Salvează</Button><Button variant="danger" onClick={() => setShowEditDeviceModal(false)}>Închide</Button></Modal.Footer>
            </Modal>

            <Modal show={showViewDeviceModal} onHide={() => setShowViewDeviceModal(false)} centered>
                <Modal.Header closeButton><Modal.Title>Detalii Device</Modal.Title></Modal.Header>
                <Modal.Body>
                    <p><strong>Nume:</strong> {currentDevice.name}</p>
                    <p><strong>Consum:</strong> {currentDevice.consumption} kW</p>
                    <p><strong>Proprietar:</strong> {getStatusText(currentDevice.username)}</p>
                </Modal.Body>
                <Modal.Footer><Button variant="danger" onClick={() => setShowViewDeviceModal(false)}>Închide</Button></Modal.Footer>
            </Modal>
        </div>
    );
}