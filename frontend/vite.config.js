import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        host: '0.0.0.0',
        port: 5173,
        strictPort: true,
        watch: {
            usePolling: true,
            // ACEASTA ESTE LINIA CARE REZOLVĂ PROBLEMA DE MEMORIE:
            ignored: [
                '**/node_modules/**',
                '**/.git/**',
                '**/target/**',       // Ignoră build-urile Java
                '**/*.java',          // Ignoră fișierele sursă Java
                '**/pom.xml',         // Ignoră Maven configs
                '**/dist/**'
            ]
        },
        hmr: {
            clientPort: 80,
        }
    },
    base: '/',
})
