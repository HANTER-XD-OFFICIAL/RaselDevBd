import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Configured with relative base './' for seamless GitHub Pages hosting
export default defineConfig({
  plugins: [react()],
  base: './',
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false
  }
});
