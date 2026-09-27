import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Configured with relative base './' so it works on any GitHub Pages URL
// (both https://username.github.io/ and https://username.github.io/repo-name/)
export default defineConfig({
  plugins: [react()],
  base: './',
});
