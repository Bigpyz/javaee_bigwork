<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<style>
 :root {
   --primary: #ff4400;
   --primary-700: #e63d00;
   --bg: #f6f7fb;
   --card: #ffffff;
   --text: #1f2937;
   --muted: #6b7280;
   --border: rgba(17, 24, 39, 0.10);
   --shadow: 0 10px 30px rgba(17, 24, 39, 0.08);
   --shadow-sm: 0 6px 18px rgba(17, 24, 39, 0.08);
   --radius: 12px;
 }

 html,
 body {
   height: 100%;
 }

 body {
   margin: 0;
   font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", "Liberation Sans", sans-serif;
   color: var(--text);
   background: radial-gradient(1200px 800px at 20% 0%, rgba(255, 68, 0, 0.10), transparent 50%),
     radial-gradient(900px 600px at 90% 10%, rgba(99, 102, 241, 0.10), transparent 55%),
     var(--bg);
 }

 #app {
   min-height: 100%;
 }

 a {
   color: var(--primary);
 }

 a:hover {
   color: var(--primary-700);
 }

 * {
   box-sizing: border-box;
 }

 button,
 input,
 select,
 textarea {
   font: inherit;
 }

 button {
   cursor: pointer;
 }

 .btn {
   display: inline-flex;
   align-items: center;
   justify-content: center;
   gap: 8px;
   padding: 8px 12px;
   border-radius: 10px;
   border: 1px solid var(--primary);
   background: var(--primary);
   color: #fff;
   font-weight: 600;
   transition: transform .15s ease, box-shadow .15s ease, background-color .15s ease, border-color .15s ease;
   box-shadow: 0 6px 14px rgba(255, 68, 0, 0.18);
 }

 .btn:hover {
   transform: translateY(-1px);
   background: var(--primary-700);
   border-color: var(--primary-700);
 }

 .btn:disabled {
   opacity: 0.55;
   cursor: not-allowed;
   transform: none;
   box-shadow: none;
 }

 .btn.ghost {
   background: #fff;
   color: var(--primary);
   border-color: rgba(255, 68, 0, 0.35);
   box-shadow: none;
 }

 .btn.ghost:hover {
   background: rgba(255, 68, 0, 0.06);
 }

 .btn.danger {
   background: #fff;
   color: #e74c3c;
   border-color: rgba(231, 76, 60, 0.45);
   box-shadow: none;
 }

 .btn.danger:hover {
   background: rgba(231, 76, 60, 0.08);
 }

 .input {
   height: 36px;
   border: 1px solid var(--border);
   border-radius: 10px;
   padding: 0 12px;
   background: #fff;
   transition: border-color .15s ease, box-shadow .15s ease;
 }

 .input:focus {
   outline: none;
   border-color: rgba(255, 68, 0, 0.55);
   box-shadow: 0 0 0 4px rgba(255, 68, 0, 0.12);
 }

 .card {
   background: var(--card);
   border: 1px solid var(--border);
   border-radius: var(--radius);
   box-shadow: var(--shadow-sm);
 }

 .table {
   width: 100%;
   border-collapse: collapse;
 }

 .table th,
 .table td {
   padding: 10px 12px;
   border-bottom: 1px solid var(--border);
   text-align: left;
 }

 .table th {
   color: var(--muted);
   font-weight: 700;
   background: rgba(17, 24, 39, 0.02);
 }

 .tag {
   display: inline-flex;
   align-items: center;
   padding: 3px 10px;
   border-radius: 999px;
   font-size: 12px;
   border: 1px solid var(--border);
   color: var(--muted);
   background: rgba(17, 24, 39, 0.02);
 }

 ::-webkit-scrollbar {
   width: 10px;
   height: 10px;
 }

 ::-webkit-scrollbar-thumb {
   background: rgba(17, 24, 39, 0.18);
   border-radius: 999px;
   border: 2px solid rgba(255, 255, 255, 0.7);
 }

 ::-webkit-scrollbar-thumb:hover {
   background: rgba(17, 24, 39, 0.28);
 }
</style>
