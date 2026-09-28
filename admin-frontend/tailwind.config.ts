import type { Config } from 'tailwindcss'

export default {
  content: [
    './components/**/*.{js,vue,ts}',
    './layouts/**/*.vue',
    './pages/**/*.vue',
    './composables/**/*.{js,ts}',
    './app.vue',
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          DEFAULT: '#2F5D8A',
          light: '#5B9BD5',
          tint: '#DCEFFB',
        },
        blush: {
          DEFAULT: '#F4A6B8',
          tint: '#FDE4EC',
        },
        mint: {
          DEFAULT: '#8FD3B6',
          tint: '#DDF3E8',
        },
        cream: '#FFF9F0',
        ink: {
          DEFAULT: '#2B3A4A',
          muted: '#6B7785',
        },
        line: '#E8E2D9',
        danger: '#E5534B',
        warn: '#F0A030',
        surface: '#F5F7FA',
      },
      fontFamily: {
        sans: ['"Kantumruy Pro"', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        card: '1rem',
      },
    },
  },
  plugins: [],
} satisfies Config
