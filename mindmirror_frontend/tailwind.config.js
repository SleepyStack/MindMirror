/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        twilight: {
          bg: '#F4F0FC',       
          card: '#FFFFFF',     
          primary: '#BDB2FF',  
          secondary: '#FFC6FF',
          text: '#4A4E69',     
        }
      },
      borderRadius: {
        '3xl': '2rem', 
      }
    },
  },
  plugins: [],
}