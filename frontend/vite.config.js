import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    server: {
        proxy: {
            '/auth': {
                target: 'https://trainingappexam.dk',
                changeOrigin: true,
            },
            '/api': {
                target: 'https://trainingappexam.dk',
                changeOrigin: true,
            },
        },
    },
})