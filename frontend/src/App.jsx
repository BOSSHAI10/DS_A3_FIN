import { HashRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Admin from './pages/Admin';
import Client from './pages/Client';
import ChatComponent from './components/ChatComponent';
import { isLoggedIn, getRole } from './axios_helper';
import AdminChatComponent from './components/AdminChatComponent';
import './App.css';

<Route path="/admin/chat" element={<AdminChatComponent />} />

// Componenta de protecție (Rămâne neschimbată)
const ProtectedRoute = ({ children, roleRequired }) => {
    if (!isLoggedIn()) {
        return <Navigate to="/login" />;
    }

    const currentRole = getRole();
    if (roleRequired && currentRole !== roleRequired) {
        if (currentRole === 'ADMIN') return <Navigate to="/admin" />;
        if (currentRole === 'USER') return <Navigate to="/client" />;
        return <Navigate to="/login" />;
    }

    return children;
};

function App() {
    return (
        // FOLOSIM HashRouter -> Rezolvă ecranul alb/404, nu strică grafica
        <HashRouter>
            <Routes>
                {/* Ruta Principală */}
                <Route path="/" element={
                    isLoggedIn() ? (
                        getRole() === 'ADMIN' ? <Navigate to="/admin" /> : <Navigate to="/client" />
                    ) : (
                        <Navigate to="/login" />
                    )
                } />

                {/* Login */}
                <Route path="/login" element={<Login />} />

                {/* Admin */}
                <Route
                    path="/admin"
                    element={
                        <ProtectedRoute roleRequired="ADMIN">
                            <Admin />
                        </ProtectedRoute>
                    }
                />

                {/* Client */}
                <Route
                    path="/client"
                    element={
                        <ProtectedRoute roleRequired="USER">
                            <Client />
                        </ProtectedRoute>
                    }
                />

                {/* Chat */}
                <Route
                    path="/chat"
                    element={
                        <ProtectedRoute>
                            <ChatComponent />
                        </ProtectedRoute>
                    }
                />

                {/* Orice altceva -> Login */}
                <Route path="*" element={<Navigate to="/login" />} />
            </Routes>
        </HashRouter>
    );
}

export default App;