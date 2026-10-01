import type { Config } from 'tailwindcss'

export default {
  content: [
    './components/**/*.{js,vue,ts}',
    './layouts/**/*.{js,vue,ts}',
    './pages/**/*.{js,vue,ts}',
    './composables/**/*.{js,ts}',
    './plugins/**/*.{js,ts}',
    './app.vue',
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          DEFAULT: '#FF2E51',
          light: '#FF8297',
          tint: '#FFE6EA',
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
        // legacy aliases used in existing components
        'baby-blue': '#FFE6EA',
        'baby-blue-deep': '#FF2E51',
        'light-pink': '#FDE4EC',
        muted: '#6B7785',
      },
      fontFamily: {
        sans: ['"Kantumruy Pro"', '"Noto Sans Khmer"', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        card: '1rem',
      },
      boxShadow: {
        soft: '0 10px 28px rgba(43, 58, 74, 0.08)',
      },
    },
  },
  plugins: [],
} satisfies Config
