import { useState, useEffect, useCallback } from "react";

// ─── STYLES ──────────────────────────────────────────────────────────────────
const STYLES = `
  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap');

  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

  :root {
    --bg:       #080F1E;
    --surface:  #0D1A30;
    --surface2: #112240;
    --border:   #1E3A5F;
    --accent:   #00D4AA;
    --accent2:  #0099FF;
    --warn:     #FFB547;
    --danger:   #FF4D6D;
    --success:  #00D4AA;
    --text:     #E8F0FE;
    --muted:    #7A90B0;
    --mono:     'JetBrains Mono', monospace;
  }

  body {
    font-family: 'Inter', sans-serif;
    background: var(--bg);
    color: var(--text);
    min-height: 100vh;
    overflow-x: hidden;
  }

  /* Scrollbar */
  ::-webkit-scrollbar { width: 5px; }
  ::-webkit-scrollbar-track { background: var(--surface); }
  ::-webkit-scrollbar-thumb { background: var(--border); border-radius: 10px; }

  /* ── Animations ── */
  @keyframes fadeUp {
    from { opacity: 0; transform: translateY(18px); }
    to   { opacity: 1; transform: translateY(0); }
  }
  @keyframes fadeIn {
    from { opacity: 0; }
    to   { opacity: 1; }
  }
  @keyframes slideRight {
    from { opacity: 0; transform: translateX(-20px); }
    to   { opacity: 1; transform: translateX(0); }
  }
  @keyframes pulse {
    0%, 100% { opacity: 1; }
    50%       { opacity: 0.5; }
  }
  @keyframes shimmer {
    0%   { background-position: -200% 0; }
    100% { background-position:  200% 0; }
  }
  @keyframes spin {
    to { transform: rotate(360deg); }
  }
  @keyframes gradBorder {
    0%   { background-position: 0% 50%; }
    50%  { background-position: 100% 50%; }
    100% { background-position: 0% 50%; }
  }
  @keyframes countUp {
    from { opacity: 0; transform: scale(0.8); }
    to   { opacity: 1; transform: scale(1); }
  }
  @keyframes toastIn {
    from { opacity: 0; transform: translateY(20px) scale(0.95); }
    to   { opacity: 1; transform: translateY(0) scale(1); }
  }

  .page { animation: fadeIn 0.35s ease forwards; }
  .card-enter { animation: fadeUp 0.4s ease forwards; }
  .slide-right { animation: slideRight 0.35s ease forwards; }

  /* ── Auth Layout ── */
  .auth-wrap {
    min-height: 100vh;
    display: grid;
    grid-template-columns: 1fr 1fr;
    overflow: hidden;
  }
  @media (max-width: 768px) {
    .auth-wrap { grid-template-columns: 1fr; }
    .auth-hero { display: none; }
  }
  .auth-hero {
    background: linear-gradient(135deg, #0D1A30 0%, #1E3A5F 60%, #0D2A4A 100%);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 60px;
    position: relative;
    overflow: hidden;
  }
  .auth-hero::before {
    content: '';
    position: absolute;
    width: 400px; height: 400px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(0,212,170,0.12) 0%, transparent 70%);
    top: -100px; right: -100px;
    animation: pulse 4s ease-in-out infinite;
  }
  .auth-hero::after {
    content: '';
    position: absolute;
    width: 300px; height: 300px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(0,153,255,0.1) 0%, transparent 70%);
    bottom: -80px; left: -80px;
    animation: pulse 5s ease-in-out infinite reverse;
  }
  .auth-hero-grid {
    position: absolute; inset: 0;
    background-image:
      linear-gradient(rgba(0,212,170,0.05) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0,212,170,0.05) 1px, transparent 1px);
    background-size: 40px 40px;
  }
  .auth-form-panel {
    background: var(--surface);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 60px 48px;
    overflow-y: auto;
  }
  .auth-form-inner {
    width: 100%;
    max-width: 400px;
    animation: fadeUp 0.5s ease forwards;
  }

  /* ── App Shell ── */
  .app-shell {
    display: grid;
    grid-template-columns: 240px 1fr;
    min-height: 100vh;
  }
  @media (max-width: 900px) {
    .app-shell { grid-template-columns: 1fr; }
    .sidebar { display: none; }
    .topbar { display: flex !important; }
  }

  /* ── Sidebar ── */
  .sidebar {
    background: var(--surface);
    border-right: 1px solid var(--border);
    display: flex;
    flex-direction: column;
    padding: 24px 0;
    position: sticky;
    top: 0;
    height: 100vh;
    overflow-y: auto;
    animation: slideRight 0.4s ease forwards;
  }
  .sidebar-logo {
    padding: 0 24px 32px;
    display: flex;
    align-items: center;
    gap: 10px;
    border-bottom: 1px solid var(--border);
    margin-bottom: 16px;
  }
  .logo-icon {
    width: 36px; height: 36px;
    background: linear-gradient(135deg, var(--accent), var(--accent2));
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    flex-shrink: 0;
  }
  .logo-text { font-size: 16px; font-weight: 700; line-height: 1.1; }
  .logo-sub { font-size: 11px; color: var(--muted); font-weight: 400; }
  .nav-section { padding: 8px 12px; margin-bottom: 4px; }
  .nav-label { font-size: 10px; font-weight: 600; color: var(--muted); letter-spacing: 1.2px; text-transform: uppercase; padding: 0 12px; margin-bottom: 6px; }
  .nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s ease;
    font-size: 14px;
    font-weight: 500;
    color: var(--muted);
    position: relative;
    overflow: hidden;
  }
  .nav-item:hover { background: var(--surface2); color: var(--text); }
  .nav-item.active {
    background: rgba(0,212,170,0.1);
    color: var(--accent);
  }
  .nav-item.active::before {
    content: '';
    position: absolute;
    left: 0; top: 20%; bottom: 20%;
    width: 3px;
    background: var(--accent);
    border-radius: 0 3px 3px 0;
  }
  .nav-icon { font-size: 18px; width: 22px; text-align: center; flex-shrink: 0; }
  .sidebar-footer { margin-top: auto; padding: 16px 12px 0; border-top: 1px solid var(--border); }

  /* ── Main ── */
  .main-content {
    background: var(--bg);
    overflow-y: auto;
    min-height: 100vh;
  }
  .page-header {
    padding: 32px 36px 0;
    animation: fadeUp 0.35s ease forwards;
  }
  .page-header h1 { font-size: 26px; font-weight: 700; margin-bottom: 4px; }
  .page-header p { color: var(--muted); font-size: 14px; }
  .page-body { padding: 24px 36px 48px; }
  @media (max-width: 600px) {
    .page-header { padding: 20px 16px 0; }
    .page-body { padding: 16px 16px 48px; }
    .page-header h1 { font-size: 20px; }
  }

  /* ── Cards ── */
  .card {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 16px;
    padding: 24px;
    transition: border-color 0.25s, box-shadow 0.25s;
    animation: fadeUp 0.4s ease forwards;
  }
  .card:hover { border-color: rgba(0,212,170,0.3); box-shadow: 0 0 0 1px rgba(0,212,170,0.08), 0 8px 32px rgba(0,0,0,0.3); }
  .card-sm { padding: 16px 20px; border-radius: 12px; }

  /* Stat cards */
  .stat-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px,1fr)); gap: 16px; margin-bottom: 28px; }
  .stat-card {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 16px;
    padding: 20px 22px;
    position: relative;
    overflow: hidden;
    transition: all 0.25s;
    animation: fadeUp 0.4s ease both;
  }
  .stat-card:hover { transform: translateY(-2px); box-shadow: 0 12px 40px rgba(0,0,0,0.3); }
  .stat-card::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(0,212,170,0.04) 0%, transparent 60%);
    pointer-events: none;
  }
  .stat-label { font-size: 12px; color: var(--muted); font-weight: 500; letter-spacing: 0.5px; text-transform: uppercase; margin-bottom: 8px; }
  .stat-value { font-size: 28px; font-weight: 700; font-family: var(--mono); animation: countUp 0.5s ease forwards; }
  .stat-icon { position: absolute; right: 16px; top: 16px; font-size: 24px; opacity: 0.4; }
  .stat-sub { font-size: 12px; color: var(--muted); margin-top: 6px; }

  /* Balance chip */
  .balance-chip {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    background: rgba(0,212,170,0.12);
    border: 1px solid rgba(0,212,170,0.25);
    border-radius: 999px;
    padding: 4px 12px;
    font-size: 13px;
    font-weight: 600;
    color: var(--accent);
    font-family: var(--mono);
  }

  /* Account card */
  .account-card {
    background: linear-gradient(135deg, var(--surface2) 0%, #0A1628 100%);
    border: 1px solid var(--border);
    border-radius: 20px;
    padding: 28px;
    position: relative;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.25s;
  }
  .account-card::before {
    content: '';
    position: absolute;
    top: -60px; right: -60px;
    width: 200px; height: 200px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(0,212,170,0.12) 0%, transparent 70%);
    transition: all 0.4s;
  }
  .account-card:hover::before { transform: scale(1.3); }
  .account-card:hover { transform: translateY(-3px); box-shadow: 0 20px 60px rgba(0,0,0,0.4), 0 0 0 1px rgba(0,212,170,0.2); }
  .account-type-badge {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    background: rgba(0,153,255,0.15);
    border: 1px solid rgba(0,153,255,0.3);
    border-radius: 6px;
    padding: 3px 10px;
    font-size: 11px;
    font-weight: 600;
    color: var(--accent2);
    letter-spacing: 0.8px;
    text-transform: uppercase;
    margin-bottom: 20px;
  }
  .account-number { font-family: var(--mono); font-size: 15px; color: var(--muted); letter-spacing: 1px; margin-bottom: 6px; }
  .account-balance { font-family: var(--mono); font-size: 32px; font-weight: 700; color: var(--accent); }
  .account-balance-label { font-size: 12px; color: var(--muted); margin-top: 2px; }
  .active-dot {
    display: inline-block;
    width: 7px; height: 7px;
    border-radius: 50%;
    background: var(--accent);
    margin-right: 5px;
    animation: pulse 2s infinite;
  }

  /* ── Forms ── */
  .form-group { margin-bottom: 18px; }
  .form-label { display: block; font-size: 13px; font-weight: 500; color: var(--muted); margin-bottom: 7px; }
  .form-input {
    width: 100%;
    background: var(--bg);
    border: 1px solid var(--border);
    border-radius: 10px;
    padding: 12px 14px;
    font-size: 14px;
    color: var(--text);
    font-family: inherit;
    transition: border-color 0.2s, box-shadow 0.2s;
    outline: none;
  }
  .form-input:focus { border-color: var(--accent); box-shadow: 0 0 0 3px rgba(0,212,170,0.12); }
  .form-input::placeholder { color: #3A5070; }
  select.form-input { cursor: pointer; }
  select.form-input option { background: var(--surface2); }
  .form-hint { font-size: 12px; color: var(--muted); margin-top: 5px; }
  .form-error { font-size: 12px; color: var(--danger); margin-top: 5px; }

  /* ── Buttons ── */
  .btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    border: none;
    border-radius: 10px;
    padding: 12px 22px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    font-family: inherit;
    outline: none;
    position: relative;
    overflow: hidden;
  }
  .btn:active { transform: scale(0.97); }
  .btn-primary {
    background: linear-gradient(135deg, var(--accent) 0%, #00B894 100%);
    color: #051020;
  }
  .btn-primary:hover { box-shadow: 0 6px 20px rgba(0,212,170,0.4); filter: brightness(1.05); }
  .btn-secondary {
    background: var(--surface2);
    color: var(--text);
    border: 1px solid var(--border);
  }
  .btn-secondary:hover { border-color: var(--accent); color: var(--accent); }
  .btn-danger { background: rgba(255,77,109,0.15); color: var(--danger); border: 1px solid rgba(255,77,109,0.3); }
  .btn-danger:hover { background: var(--danger); color: white; }
  .btn-warn { background: rgba(255,181,71,0.15); color: var(--warn); border: 1px solid rgba(255,181,71,0.3); }
  .btn-warn:hover { background: var(--warn); color: #051020; }
  .btn-ghost { background: transparent; color: var(--muted); }
  .btn-ghost:hover { color: var(--text); background: var(--surface2); }
  .btn-full { width: 100%; }
  .btn-sm { padding: 8px 14px; font-size: 13px; border-radius: 8px; }
  .btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none !important; }

  /* ── Tables ── */
  .table-wrap { overflow-x: auto; border-radius: 12px; border: 1px solid var(--border); }
  table { width: 100%; border-collapse: collapse; font-size: 14px; }
  thead { background: var(--surface2); }
  th { padding: 12px 16px; text-align: left; font-size: 11px; font-weight: 600; color: var(--muted); letter-spacing: 0.8px; text-transform: uppercase; white-space: nowrap; }
  td { padding: 14px 16px; border-top: 1px solid rgba(30,58,95,0.5); color: var(--text); }
  tr:hover td { background: rgba(255,255,255,0.02); }
  .tx-type {
    display: inline-flex; align-items: center; gap: 5px;
    font-size: 12px; font-weight: 600; border-radius: 6px;
    padding: 3px 10px;
  }
  .tx-deposit { background: rgba(0,212,170,0.12); color: var(--accent); }
  .tx-withdraw { background: rgba(255,77,109,0.12); color: var(--danger); }
  .tx-transfer { background: rgba(0,153,255,0.12); color: var(--accent2); }
  .mono { font-family: var(--mono); font-size: 13px; }

  /* ── Tabs ── */
  .tabs-row { display: flex; gap: 8px; margin-bottom: 20px; border-bottom: 1px solid var(--border); padding-bottom: 8px; }
  .tab-btn { background: transparent; border: none; color: var(--muted); padding: 8px 16px; font-weight: 600; cursor: pointer; border-radius: 8px; transition: all 0.2s; }
  .tab-btn:hover { color: var(--text); background: var(--surface2); }
  .tab-btn.active { color: var(--accent); background: rgba(0,212,170,0.1); }

  /* ── Dialog / Modal ── */
  .overlay {
    position: fixed; inset: 0; z-index: 100;
    background: rgba(5,10,20,0.7);
    backdrop-filter: blur(4px);
    display: flex; align-items: center; justify-content: center;
    padding: 20px;
    animation: fadeIn 0.2s ease;
  }
  .dialog {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 20px;
    padding: 32px;
    width: 100%; max-width: 440px;
    animation: fadeUp 0.3s ease;
    position: relative;
  }
  .dialog-title { font-size: 20px; font-weight: 700; margin-bottom: 6px; }
  .dialog-sub { font-size: 14px; color: var(--muted); margin-bottom: 24px; }
  .dialog-close {
    position: absolute; top: 16px; right: 16px;
    background: var(--surface2); border: 1px solid var(--border);
    border-radius: 8px; width: 32px; height: 32px;
    display: flex; align-items: center; justify-content: center;
    cursor: pointer; font-size: 16px; color: var(--muted);
    transition: all 0.2s;
  }
  .dialog-close:hover { color: var(--text); border-color: var(--text); }

  /* ── Toast ── */
  .toast-container { position: fixed; bottom: 24px; right: 24px; z-index: 999; display: flex; flex-direction: column; gap: 10px; }
  .toast {
    background: var(--surface2);
    border: 1px solid var(--border);
    border-radius: 12px;
    padding: 14px 18px;
    display: flex; align-items: center; gap: 12px;
    font-size: 14px; font-weight: 500;
    box-shadow: 0 8px 32px rgba(0,0,0,0.4);
    animation: toastIn 0.3s ease;
    max-width: 340px;
    min-width: 240px;
  }
  .toast-success { border-left: 4px solid var(--accent); }
  .toast-error   { border-left: 4px solid var(--danger); }
  .toast-info    { border-left: 4px solid var(--accent2); }

  /* ── Loading ── */
  .spinner { width: 20px; height: 20px; border: 2px solid rgba(0,212,170,0.2); border-top-color: var(--accent); border-radius: 50%; animation: spin 0.7s linear infinite; }
  .skeleton { background: linear-gradient(90deg, var(--surface2) 25%, var(--border) 50%, var(--surface2) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: 8px; }

  /* ── Transaction action buttons row ── */
  .action-row { display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 28px; }
  .action-btn {
    flex: 1; min-width: 120px;
    display: flex; flex-direction: column; align-items: center; gap: 6px;
    background: var(--surface); border: 1px solid var(--border);
    border-radius: 14px; padding: 20px 10px;
    cursor: pointer; transition: all 0.25s;
    font-family: inherit;
  }
  .action-btn:hover { border-color: var(--accent); background: rgba(0,212,170,0.05); transform: translateY(-2px); }
  .action-btn .ab-icon { font-size: 26px; }
  .action-btn .ab-label { font-size: 13px; font-weight: 600; color: var(--text); }
  .action-btn .ab-sub { font-size: 11px; color: var(--muted); }

  /* ── Divider ── */
  .divider { height: 1px; background: var(--border); margin: 20px 0; }

  /* ── Empty state ── */
  .empty-state { text-align: center; padding: 60px 24px; color: var(--muted); }
  .empty-state .es-icon { font-size: 48px; margin-bottom: 16px; opacity: 0.5; }
  .empty-state .es-title { font-size: 18px; font-weight: 600; color: var(--text); margin-bottom: 6px; }
  .empty-state p { font-size: 14px; }

  /* ── Grid helpers ── */
  .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
  @media (max-width: 600px) { .grid-2 { grid-template-columns: 1fr; } }
  .accounts-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px,1fr)); gap: 20px; }

  /* ── Tags ── */
  .tag { display: inline-flex; align-items: center; gap: 4px; border-radius: 6px; padding: 2px 9px; font-size: 11px; font-weight: 600; }
  .tag-active { background: rgba(0,212,170,0.12); color: var(--accent); }
  .tag-frozen { background: rgba(0,153,255,0.12); color: var(--accent2); }
  .tag-inactive { background: rgba(255,77,109,0.12); color: var(--danger); }

  /* Alert info strip */
  .alert-strip {
    border-radius: 10px; padding: 12px 16px;
    display: flex; align-items: center; gap: 10px;
    font-size: 13px;
    margin-bottom: 20px;
  }
  .alert-info { background: rgba(0,153,255,0.1); border: 1px solid rgba(0,153,255,0.25); color: #7BBFFF; }
  .alert-warn { background: rgba(255,181,71,0.1); border: 1px solid rgba(255,181,71,0.25); color: var(--warn); }

  /* Password strength */
  .pw-strength { display: flex; gap: 4px; margin-top: 6px; }
  .pw-bar { flex: 1; height: 3px; border-radius: 2px; background: var(--border); transition: background 0.3s; }
  .pw-bar.filled-weak  { background: var(--danger); }
  .pw-bar.filled-fair  { background: var(--warn); }
  .pw-bar.filled-strong{ background: var(--accent); }

  /* Quick stat strip */
  .quick-strip { display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 28px; }
  .quick-item {
    flex: 1; min-width: 140px;
    background: var(--surface); border: 1px solid var(--border);
    border-radius: 12px; padding: 14px 18px;
    animation: fadeUp 0.4s ease both;
  }
  .quick-item .qi-label { font-size: 11px; color: var(--muted); font-weight: 600; text-transform: uppercase; letter-spacing: 0.7px; }
  .quick-item .qi-value { font-family: var(--mono); font-size: 22px; font-weight: 700; margin-top: 4px; }

  /* Topbar (mobile) */
  .topbar {
    display: none;
    background: var(--surface);
    border-bottom: 1px solid var(--border);
    padding: 14px 20px;
    align-items: center;
    justify-content: space-between;
  }

  /* Section heading */
  .section-heading { font-size: 16px; font-weight: 700; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }
  .section-heading::before { content: ''; display: block; width: 4px; height: 18px; background: var(--accent); border-radius: 2px; }
`;

// ─── API CLIENT ───────────────────────────────────────────────────────────────
const BASE = "http://localhost:8080";
const getToken = () => localStorage.getItem("authToken");
const getHeaders = (json = true) => ({
  ...(json ? { "Content-Type": "application/json" } : {}),
  ...(getToken() ? { Authorization: `Bearer ${getToken()}` } : {}),
});

const api = async (endpoint, opts = {}) => {
  const r = await fetch(`${BASE}${endpoint}`, { ...opts, headers: { ...getHeaders(), ...opts.headers } });
  const data = await r.json().catch(() => ({}));
  if (!r.ok) throw new Error(data.message || `Error ${r.status}`);
  return data;
};
const authAPI = {
  register: (d) => api("/api/auth/register", { method: "POST", body: JSON.stringify(d) }),
  login: async (d) => {
    const res = await api("/api/auth/login", { method: "POST", body: JSON.stringify(d) });
    if (res.token) localStorage.setItem("authToken", res.token);
    return res;
  },
  session: () => api("/api/auth/session"),
};
const accountAPI = {
  list: () => api("/api/accounts"),
  get: (n) => api(`/api/accounts/${n}`),
  create: (d) => api("/api/accounts/create", { method: "POST", body: JSON.stringify(d) }),
};
const txAPI = {
  deposit: (d) => api("/api/transactions/deposit", { method: "POST", body: JSON.stringify(d) }),
  withdraw: (d) => api("/api/transactions/withdraw", { method: "POST", body: JSON.stringify(d) }),
  transfer: (d) => api("/api/transactions/transfer", { method: "POST", body: JSON.stringify(d) }),
  history: (n) => api(`/api/transactions/history/${n}`),
};
const adminAPI = {
  freeze: (n) => api(`/api/admin/accounts/${n}/freeze`, { method: "PUT" }),
  unblock: (n) => api(`/api/admin/accounts/${n}/unblock`, { method: "PUT" }),
  deleteUser: (id) => api(`/api/admin/users/${id}`, { method: "DELETE" }),
  listUsers: () => api("/api/admin/users", { method: "GET" }),
  deleteAccount: (n) => api(`/api/admin/accounts/${n}`, { method: "DELETE" }),
};

// ─── HELPERS ─────────────────────────────────────────────────────────────────
const fmt = (n) => new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(n);
const fmtDate = (s) => new Date(s).toLocaleString("en-US", { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" });

function pwStrength(pw) {
  let s = 0;
  if (pw.length >= 8) s++;
  if (/[A-Z]/.test(pw)) s++;
  if (/[0-9]/.test(pw)) s++;
  if (/[^A-Za-z0-9]/.test(pw)) s++;
  return s;
}

const parseJwt = (token) => {
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch (e) {
    return null;
  }
};

// ─── TOAST ───────────────────────────────────────────────────────────────────
let toastFn = null;
function Toast({ toasts }) {
  return (
    <div className="toast-container">
      {toasts.map((t) => (
        <div key={t.id} className={`toast toast-${t.type}`}>
          <span>{t.type === "success" ? "✅" : t.type === "error" ? "❌" : "ℹ️"}</span>
          <span>{t.msg}</span>
        </div>
      ))}
    </div>
  );
}

// ─── SPINNER ─────────────────────────────────────────────────────────────────
const Spinner = () => <div className="spinner" />;

// ─── DIALOG ──────────────────────────────────────────────────────────────────
function Dialog({ title, sub, onClose, children }) {
  return (
    <div className="overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="dialog">
        <div className="dialog-close" onClick={onClose}>✕</div>
        <div className="dialog-title">{title}</div>
        {sub && <div className="dialog-sub">{sub}</div>}
        {children}
      </div>
    </div>
  );
}

// ─── AUTH PAGES ──────────────────────────────────────────────────────────────
function HeroPanel() {
  return (
    <div className="auth-hero">
      <div className="auth-hero-grid" />
      <div style={{ position: "relative", zIndex: 1, textAlign: "center" }}>
        <div style={{ fontSize: 56, marginBottom: 24 }}>🏦</div>
        <h1 style={{ fontSize: 36, fontWeight: 800, lineHeight: 1.1, marginBottom: 16 }}>
          Secure.<br />Smart.<br />
          <span style={{ background: "linear-gradient(90deg,#00D4AA,#0099FF)", WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent" }}>
            Reliable.
          </span>
        </h1>
        <p style={{ color: "#7A90B0", maxWidth: 280, margin: "0 auto", lineHeight: 1.7 }}>
          SecureBankPro gives you complete control of your finances with enterprise-grade security and real-time transparency.
        </p>
        <div style={{ display: "flex", gap: 24, justifyContent: "center", marginTop: 40 }}>
          {[["🔒", "Encrypted"], ["⚡", "Instant"], ["📊", "Analytics"]].map(([ic, lb]) => (
            <div key={lb} style={{ textAlign: "center" }}>
              <div style={{ fontSize: 24, marginBottom: 4 }}>{ic}</div>
              <div style={{ fontSize: 12, color: "#7A90B0", fontWeight: 600 }}>{lb}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function LoginPage({ onLogin, onSwitch }) {
  const [form, setForm] = useState({ email: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  const submit = async () => {
    setErr(""); setLoading(true);
    try {
      await authAPI.login(form);
      onLogin();
    } catch (e) { setErr(e.message); }
    setLoading(false);
  };

  return (
    <div className="auth-wrap">
      <HeroPanel />
      <div className="auth-form-panel">
        <div className="auth-form-inner">
          <div style={{ marginBottom: 36 }}>
            <div className="logo-icon" style={{ width: 48, height: 48, fontSize: 24, marginBottom: 20 }}>🏦</div>
            <h2 style={{ fontSize: 28, fontWeight: 800, marginBottom: 6 }}>Welcome back</h2>
            <p style={{ color: "#7A90B0" }}>Sign in to your SecureBankPro account</p>
          </div>
          {err && <div className="alert-strip alert-warn" style={{ marginBottom: 16 }}>⚠️ {err}</div>}
          <div className="form-group">
            <label className="form-label">Email address</label>
            <input className="form-input" type="email" placeholder="you@example.com"
              value={form.email} onChange={e => setForm({ ...form, email: e.target.value })}
              onKeyDown={e => e.key === "Enter" && submit()} />
          </div>
          <div className="form-group">
            <label className="form-label">Password</label>
            <input className="form-input" type="password" placeholder="••••••••"
              value={form.password} onChange={e => setForm({ ...form, password: e.target.value })}
              onKeyDown={e => e.key === "Enter" && submit()} />
          </div>
          <button className="btn btn-primary btn-full" style={{ marginTop: 8, padding: "14px" }}
            onClick={submit} disabled={loading || !form.email || !form.password}>
            {loading ? <Spinner /> : "Sign In →"}
          </button>
          <div className="divider" />
          <p style={{ textAlign: "center", color: "#7A90B0", fontSize: 14 }}>
            New to SecureBankPro?{" "}
            <span style={{ color: "#00D4AA", cursor: "pointer", fontWeight: 600 }} onClick={onSwitch}>Create account</span>
          </p>
        </div>
      </div>
    </div>
  );
}

function RegisterPage({ onSwitch, onSuccess }) {
  const [form, setForm] = useState({ fullName: "", email: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");
  const strength = pwStrength(form.password);
  const strengthLabels = ["", "Weak", "Fair", "Good", "Strong"];
  const strengthColors = ["", "filled-weak", "filled-fair", "filled-strong", "filled-strong"];

  const submit = async () => {
    setErr(""); setLoading(true);
    try {
      await authAPI.register(form);
      onSuccess("Account created! Please sign in.");
      onSwitch();
    } catch (e) { setErr(e.message); }
    setLoading(false);
  };

  return (
    <div className="auth-wrap">
      <HeroPanel />
      <div className="auth-form-panel">
        <div className="auth-form-inner">
          <div style={{ marginBottom: 32 }}>
            <div className="logo-icon" style={{ width: 48, height: 48, fontSize: 24, marginBottom: 20 }}>🏦</div>
            <h2 style={{ fontSize: 28, fontWeight: 800, marginBottom: 6 }}>Create account</h2>
            <p style={{ color: "#7A90B0" }}>Join SecureBankPro — takes 30 seconds</p>
          </div>
          {err && <div className="alert-strip alert-warn">{err}</div>}
          <div className="form-group">
            <label className="form-label">Full name</label>
            <input className="form-input" placeholder="Jane Doe"
              value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })} />
          </div>
          <div className="form-group">
            <label className="form-label">Email address</label>
            <input className="form-input" type="email" placeholder="you@example.com"
              value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />
          </div>
          <div className="form-group">
            <label className="form-label">Password</label>
            <input className="form-input" type="password" placeholder="Min 8 chars, uppercase, digit, symbol"
              value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} />
            {form.password && (
              <>
                <div className="pw-strength">
                  {[1, 2, 3, 4].map(i => (
                    <div key={i} className={`pw-bar ${i <= strength ? strengthColors[strength] : ""}`} />
                  ))}
                </div>
                <div style={{ fontSize: 12, color: strength >= 3 ? "#00D4AA" : strength >= 2 ? "#FFB547" : "#FF4D6D", marginTop: 4, fontWeight: 600 }}>
                  {strengthLabels[strength]}
                </div>
              </>
            )}
            <div className="form-hint">Uppercase • digit • special character • 8+ chars</div>
          </div>
          <button className="btn btn-primary btn-full" style={{ marginTop: 8, padding: "14px" }}
            onClick={submit} disabled={loading || !form.fullName || !form.email || !form.password}>
            {loading ? <Spinner /> : "Create Account →"}
          </button>
          <div className="divider" />
          <p style={{ textAlign: "center", color: "#7A90B0", fontSize: 14 }}>
            Already have an account?{" "}
            <span style={{ color: "#00D4AA", cursor: "pointer", fontWeight: 600 }} onClick={onSwitch}>Sign in</span>
          </p>
        </div>
      </div>
    </div>
  );
}

// ─── SIDEBAR ─────────────────────────────────────────────────────────────────
const navItems = [
  { id: "dashboard", icon: "📊", label: "Dashboard" },
  { id: "accounts", icon: "💳", label: "My Accounts" },
  { id: "create-account", icon: "➕", label: "New Account" },
  { id: "transactions", icon: "⚡", label: "Transactions" },
  { id: "history", icon: "📋", label: "TX History" },
  { id: "admin", icon: "🛡️", label: "Admin Panel" },
];

function Sidebar({ page, onNav, onLogout, userEmail, userRole }) {
  const isAdmin = userRole === "ADMIN" || userEmail === "admin@securebank.com";
  return (
    <div className="sidebar">
      <div className="sidebar-logo">
        <div className="logo-icon">🏦</div>
        <div>
          <div className="logo-text">SecureBankPro</div>
          <div className="logo-sub">Banking Platform</div>
        </div>
      </div>
      <div className="nav-section">
        <div className="nav-label">Main</div>
        {navItems.slice(0, 3).map(n => (
          <div key={n.id} className={`nav-item ${page === n.id ? "active" : ""}`} onClick={() => onNav(n.id)}>
            <span className="nav-icon">{n.icon}</span>{n.label}
          </div>
        ))}
      </div>
      <div className="nav-section">
        <div className="nav-label">Activity</div>
        {navItems.slice(3, 5).map(n => (
          <div key={n.id} className={`nav-item ${page === n.id ? "active" : ""}`} onClick={() => onNav(n.id)}>
            <span className="nav-icon">{n.icon}</span>{n.label}
          </div>
        ))}
      </div>
      {isAdmin && (
        <div className="nav-section">
          <div className="nav-label">Settings</div>
          <div className={`nav-item ${page === "admin" ? "active" : ""}`} onClick={() => onNav("admin")}>
            <span className="nav-icon">🛡️</span>Admin Panel
          </div>
        </div>
      )}
      <div className="sidebar-footer">
        <div style={{ padding: "10px 12px", borderRadius: 10, background: "rgba(0,212,170,0.06)", border: "1px solid rgba(0,212,170,0.15)", marginBottom: 10 }}>
          <div style={{ fontSize: 11, color: "#7A90B0", marginBottom: 2 }}>Signed in as</div>
          <div style={{ fontSize: 13, fontWeight: 600, color: "#E8F0FE", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{userEmail || "User"}</div>
        </div>
        <div className="nav-item" onClick={onLogout}>
          <span className="nav-icon">🚪</span>Sign Out
        </div>
      </div>
    </div>
  );
}

// ─── DASHBOARD ───────────────────────────────────────────────────────────────
function DashboardPage({ accounts, onNav, addToast }) {
  const totalBalance = accounts.reduce((s, a) => s + a.balance, 0);
  const savings = accounts.filter(a => a.accountType === "SAVINGS");
  const current = accounts.filter(a => a.accountType === "CURRENT");

  return (
    <div className="page">
      <div className="page-header">
        <h1>Dashboard</h1>
        <p>Your financial overview at a glance</p>
      </div>
      <div className="page-body">
        <div className="stat-grid">
          {[
            { label: "Total Balance", value: fmt(totalBalance), icon: "💰", sub: "Across all accounts", delay: 0, show: true },
            { label: "Accounts", value: accounts.length, icon: "💳", sub: `${savings.length} savings · ${current.length} current`, delay: 0.08, show: true },
            { label: "Savings Balance", value: fmt(savings.reduce((s, a) => s + a.balance, 0)), icon: "🏦", sub: `${savings.length} account(s)`, delay: 0.12, show: savings.length > 0 },
            { label: "Current Balance", value: fmt(current.reduce((s, a) => s + a.balance, 0)), icon: "⚡", sub: `${current.length} account(s)`, delay: 0.16, show: current.length > 0 },
          ].filter(s => s.show).map((s, i) => (
            <div key={s.label} className="stat-card" style={{ animationDelay: `${s.delay}s` }}>
              <div className="stat-icon">{s.icon}</div>
              <div className="stat-label">{s.label}</div>
              <div className="stat-value" style={{ color: s.label === "Total Balance" ? "#00D4AA" : "#E8F0FE" }}>{s.value}</div>
              <div className="stat-sub">{s.sub}</div>
            </div>
          ))}
        </div>

        <div className="section-heading">Quick Actions</div>
        <div className="action-row" style={{ marginBottom: 28 }}>
          {[
            { icon: "⬆️", label: "Deposit", sub: "Add funds", act: "transactions" },
            { icon: "⬇️", label: "Withdraw", sub: "Take out cash", act: "transactions" },
            { icon: "↔️", label: "Transfer", sub: "Move money", act: "transactions" },
            { icon: "➕", label: "New Account", sub: "Open account", act: "create-account" },
            { icon: "📋", label: "History", sub: "View logs", act: "history" },
          ].map(a => (
            <button key={a.label} className="action-btn" onClick={() => onNav(a.act)}>
              <span className="ab-icon">{a.icon}</span>
              <span className="ab-label">{a.label}</span>
              <span className="ab-sub">{a.sub}</span>
            </button>
          ))}
        </div>

        <div className="section-heading">Your Accounts</div>
        {accounts.length === 0 ? (
          <div className="empty-state">
            <div className="es-icon">💳</div>
            <div className="es-title">No accounts yet</div>
            <p>Open your first account to get started</p>
            <button className="btn btn-primary" style={{ marginTop: 16 }} onClick={() => onNav("create-account")}>Create Account</button>
          </div>
        ) : (
          <div className="accounts-grid">
            {accounts.map((a, i) => (
              <div key={a.accountId} className="account-card" style={{ animationDelay: `${i * 0.08}s` }} onClick={() => onNav("history")}>
                <div className="account-type-badge">{a.accountType === "SAVINGS" ? "🏦" : "⚡"} {a.accountType}</div>
                <div className="account-number">{a.accountNumber}</div>
                <div className="account-balance">{fmt(a.balance)}</div>
                <div className="account-balance-label">Available balance</div>
                <div style={{ marginTop: 16, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                  <div style={{ fontSize: 12, color: "#7A90B0" }}>
                    <span className="active-dot" />
                    {a.active ? "Active" : "Inactive"}
                  </div>
                  <div style={{ fontSize: 12, color: "#7A90B0" }}>ID: {a.accountId}</div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

// ─── ACCOUNTS PAGE ────────────────────────────────────────────────────────────
function AccountsPage({ accounts, loading }) {
  return (
    <div className="page">
      <div className="page-header">
        <h1>My Accounts</h1>
        <p>{accounts.length} account{accounts.length !== 1 ? "s" : ""} found</p>
      </div>
      <div className="page-body">
        {loading ? (
          <div style={{ display: "flex", gap: 16, flexDirection: "column" }}>
            {[1, 2].map(i => <div key={i} className="skeleton" style={{ height: 160 }} />)}
          </div>
        ) : accounts.length === 0 ? (
          <div className="empty-state">
            <div className="es-icon">💳</div>
            <div className="es-title">No accounts</div>
            <p>Create your first account to begin banking</p>
          </div>
        ) : (
          <div className="accounts-grid">
            {accounts.map((a, i) => (
              <div key={a.accountId} className="account-card" style={{ animationDelay: `${i * 0.08}s` }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 16 }}>
                  <div className="account-type-badge">{a.accountType === "SAVINGS" ? "🏦" : "⚡"} {a.accountType}</div>
                  <span className={`tag ${a.active ? "tag-active" : "tag-inactive"}`}>
                    {a.active ? "● Active" : "○ Inactive"}
                  </span>
                </div>
                <div className="account-number">{a.accountNumber}</div>
                <div className="account-balance">{fmt(a.balance)}</div>
                <div className="account-balance-label">Available balance</div>
                <div style={{ marginTop: 20, paddingTop: 16, borderTop: "1px solid rgba(30,58,95,0.5)", display: "flex", justifyContent: "space-between", fontSize: 12, color: "#7A90B0" }}>
                  <span>Account ID: {a.accountId}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

// ─── CREATE ACCOUNT ───────────────────────────────────────────────────────────
function CreateAccountPage({ accounts, userEmail, onRefresh, addToast }) {
  const [form, setForm] = useState({ accountNumber: "", accountType: "SAVINGS", balance: "0" });
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(null);

  // Auto-generate unique account number when accountType changes
  useEffect(() => {
    const prefix = form.accountType === "SAVINGS" ? "SBP-SAV" : "SBP-CUR";
    let newNum;
    let isDuplicate = true;
    let attempts = 0;
    while (isDuplicate && attempts < 100) {
      const randomSuffix = Math.floor(10000000 + Math.random() * 90000000);
      newNum = `${prefix}-${randomSuffix}`;
      isDuplicate = accounts.some(a => a.accountNumber === newNum);
      attempts++;
    }
    setForm(f => ({ ...f, accountNumber: newNum }));
  }, [form.accountType, accounts]);

  const submit = async () => {
    setLoading(true); setSuccess(null);
    try {
      const r = await accountAPI.create({ 
        accountNumber: form.accountNumber, 
        accountType: form.accountType, 
        balance: 0, 
        ownerEmail: userEmail 
      });
      setSuccess(r);
      addToast("success", `Account ${r.accountNumber} created!`);
      onRefresh();
    } catch (e) { addToast("error", e.message); }
    setLoading(false);
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Open New Account</h1>
        <p>Set up a savings or current account instantly</p>
      </div>
      <div className="page-body">
        <div style={{ maxWidth: 520 }}>
          <div className="card card-enter">
            {success && (
              <div className="alert-strip alert-info" style={{ marginBottom: 24 }}>
                ✅ Account <strong>{success.accountNumber}</strong> created with {fmt(success.balance)}
              </div>
            )}
            <div className="form-group">
              <label className="form-label">Account Type</label>
              <select className="form-input" value={form.accountType}
                onChange={e => setForm({ ...form, accountType: e.target.value })}>
                <option value="SAVINGS">🏦 Savings Account</option>
                <option value="CURRENT">⚡ Current Account</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Auto-generated Account Number</label>
              <input className="form-input" value={form.accountNumber} readOnly style={{ opacity: 0.8, cursor: "not-allowed" }} />
              <div className="form-hint">Automatically generated unique identifier</div>
            </div>
            <button className="btn btn-primary btn-full" style={{ marginTop: 8 }}
              onClick={submit} disabled={loading || !form.accountNumber}>
              {loading ? <Spinner /> : "Open Account →"}
            </button>
          </div>
          <div className="alert-strip alert-info" style={{ marginTop: 16 }}>
            ℹ️ Savings accounts earn interest. Current accounts have no transaction limits.
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── TRANSACTIONS PAGE ────────────────────────────────────────────────────────
function TransactionsPage({ accounts, addToast }) {
  const [dialog, setDialog] = useState(null); // "deposit" | "withdraw" | "transfer"
  const [form, setForm] = useState({ accountNumber: "", destinationAccountNumber: "", amount: "" });
  const [loading, setLoading] = useState(false);

  const open = (type) => { setDialog(type); setForm({ accountNumber: "", destinationAccountNumber: "", amount: "" }); };
  const close = () => setDialog(null);

  const submit = async () => {
    setLoading(true);
    try {
      let r;
      if (dialog === "deposit") r = await txAPI.deposit({ accountNumber: form.accountNumber, amount: parseFloat(form.amount) });
      else if (dialog === "withdraw") r = await txAPI.withdraw({ accountNumber: form.accountNumber, amount: parseFloat(form.amount) });
      else r = await txAPI.transfer({ sourceAccountNumber: form.accountNumber, destinationAccountNumber: form.destinationAccountNumber, amount: parseFloat(form.amount) });
      addToast("success", r.message);
      close();
    } catch (e) { addToast("error", e.message); }
    setLoading(false);
  };

  const actions = [
    { type: "deposit", icon: "⬆️", label: "Deposit", sub: "Add funds to account", color: "#00D4AA" },
    { type: "withdraw", icon: "⬇️", label: "Withdraw", sub: "Remove funds from account", color: "#FF4D6D" },
    { type: "transfer", icon: "↔️", label: "Transfer", sub: "Move money between accounts", color: "#0099FF" },
  ];

  return (
    <div className="page">
      <div className="page-header">
        <h1>Transactions</h1>
        <p>Deposit, withdraw, or transfer between accounts</p>
      </div>
      <div className="page-body">
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(260px,1fr))", gap: 20, maxWidth: 900 }}>
          {actions.map((a, i) => (
            <div key={a.type} className="card card-enter" style={{ animationDelay: `${i * 0.1}s`, cursor: "pointer" }}
              onClick={() => open(a.type)}>
              <div style={{ fontSize: 48, marginBottom: 14 }}>{a.icon}</div>
              <div style={{ fontSize: 20, fontWeight: 700, marginBottom: 6, color: a.color }}>{a.label}</div>
              <div style={{ color: "#7A90B0", fontSize: 14, marginBottom: 20 }}>{a.sub}</div>
              <button className="btn btn-secondary btn-sm" style={{ borderColor: a.color, color: a.color }}>
                {a.label} →
              </button>
            </div>
          ))}
        </div>

        {dialog && (
          <Dialog
            title={dialog === "deposit" ? "💚 Deposit Funds" : dialog === "withdraw" ? "🔴 Withdraw Funds" : "🔵 Transfer Funds"}
            sub={dialog === "deposit" ? "Add money to an account" : dialog === "withdraw" ? "Withdraw from your account" : "Move money between accounts"}
            onClose={close}
          >
            <div className="form-group">
              <label className="form-label">{dialog === "transfer" ? "Source Account Number" : "Account Number"}</label>
              <select className="form-input" value={form.accountNumber}
                onChange={e => setForm({ ...form, accountNumber: e.target.value })}>
                <option value="">Select account…</option>
                {accounts.map(a => <option key={a.accountId} value={a.accountNumber}>{a.accountNumber} — {fmt(a.balance)}</option>)}
              </select>
            </div>
            {dialog === "transfer" && (
              <div className="form-group">
                <label className="form-label">Destination Account Number</label>
                <input className="form-input" placeholder="e.g. SBP2001"
                  value={form.destinationAccountNumber}
                  onChange={e => setForm({ ...form, destinationAccountNumber: e.target.value })} />
              </div>
            )}
            <div className="form-group">
              <label className="form-label">Amount (USD)</label>
              <input className="form-input" type="number" min="0.01" step="0.01" placeholder="0.00"
                value={form.amount} onChange={e => setForm({ ...form, amount: e.target.value })} />
            </div>
            {form.amount > 0 && (
              <div className="alert-strip alert-info" style={{ marginBottom: 16 }}>
                ℹ️ You are about to {dialog} <strong>{fmt(form.amount)}</strong>
                {dialog === "transfer" ? ` to ${form.destinationAccountNumber || "destination"}` : ""}
              </div>
            )}
            <div style={{ display: "flex", gap: 10 }}>
              <button className="btn btn-secondary" style={{ flex: 1 }} onClick={close}>Cancel</button>
              <button className={`btn ${dialog === "withdraw" ? "btn-danger" : dialog === "transfer" ? "btn-secondary" : "btn-primary"}`}
                style={{ flex: 2, ...(dialog === "transfer" ? { background: "rgba(0,153,255,0.15)", color: "#0099FF", border: "1px solid rgba(0,153,255,0.3)" } : {}) }}
                onClick={submit}
                disabled={loading || !form.accountNumber || !form.amount || (dialog === "transfer" && !form.destinationAccountNumber)}>
                {loading ? <Spinner /> : `Confirm ${dialog.charAt(0).toUpperCase() + dialog.slice(1)}`}
              </button>
            </div>
          </Dialog>
        )}
      </div>
    </div>
  );
}

// ─── HISTORY PAGE ─────────────────────────────────────────────────────────────
function HistoryPage({ accounts, addToast }) {
  const [selected, setSelected] = useState("");
  const [txs, setTxs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [accountInfo, setAccountInfo] = useState(null);

  const load = useCallback(async (num) => {
    if (!num) return;
    setLoading(true); setTxs([]); setAccountInfo(null);
    try {
      const [info, history] = await Promise.all([accountAPI.get(num), txAPI.history(num)]);
      setAccountInfo(info);
      setTxs(history);
    } catch (e) { addToast("error", e.message); }
    setLoading(false);
  }, [addToast]);

  const handleSelect = (num) => { setSelected(num); load(num); };

  const typeClass = (t) => t === "DEPOSIT" ? "tx-deposit" : t === "WITHDRAW" ? "tx-withdraw" : "tx-transfer";

  return (
    <div className="page">
      <div className="page-header">
        <h1>Transaction History</h1>
        <p>View detailed logs for any account</p>
      </div>
      <div className="page-body">
        <div className="card" style={{ marginBottom: 24, maxWidth: 500 }}>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label">Select Account</label>
            <select className="form-input" value={selected} onChange={e => handleSelect(e.target.value)}>
              <option value="">Choose an account…</option>
              {accounts.map(a => <option key={a.accountId} value={a.accountNumber}>{a.accountNumber} — {a.accountType}</option>)}
            </select>
          </div>
        </div>

        {accountInfo && (
          <div className="quick-strip" style={{ marginBottom: 24 }}>
            {[
              { label: "Account", value: accountInfo.accountNumber, color: "#E8F0FE" },
              { label: "Balance", value: fmt(accountInfo.balance), color: "#00D4AA" },
              { label: "Type", value: accountInfo.accountType, color: "#0099FF" },
              { label: "Status", value: accountInfo.active ? "Active" : "Frozen", color: accountInfo.active ? "#00D4AA" : "#FF4D6D" },
            ].map((q, i) => (
              <div key={i} className="quick-item" style={{ animationDelay: `${i * 0.06}s` }}>
                <div className="qi-label">{q.label}</div>
                <div className="qi-value" style={{ color: q.color, fontSize: q.label === "Account" ? 14 : 22 }}>{q.value}</div>
              </div>
            ))}
          </div>
        )}

        {loading ? (
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            {[1, 2, 3].map(i => <div key={i} className="skeleton" style={{ height: 56 }} />)}
          </div>
        ) : selected && txs.length === 0 && !loading ? (
          <div className="empty-state">
            <div className="es-icon">📭</div>
            <div className="es-title">No transactions yet</div>
            <p>This account has no transaction history</p>
          </div>
        ) : txs.length > 0 ? (
          <div className="table-wrap card-enter">
            <table>
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>From</th>
                  <th>To</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {txs.map(t => (
                  <tr key={t.transactionId}>
                    <td><span className="mono">{t.referenceNumber}</span></td>
                    <td><span className={`tx-type ${typeClass(t.transactionType)}`}>{t.transactionType}</span></td>
                    <td><span className="mono" style={{ fontWeight: 700 }}>{fmt(t.amount)}</span></td>
                    <td><span className="mono">{t.sourceAccount?.accountNumber || "—"}</span></td>
                    <td><span className="mono">{t.destinationAccount?.accountNumber || "—"}</span></td>
                    <td style={{ color: "#7A90B0", fontSize: 13 }}>{fmtDate(t.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : !selected ? (
          <div className="empty-state">
            <div className="es-icon">📋</div>
            <div className="es-title">Select an account</div>
            <p>Choose an account above to view its transaction history</p>
          </div>
        ) : null}
      </div>
    </div>
  );
}

function AdminPage({ accounts, addToast, onRefresh }) {
  const [users, setUsers] = useState([]);
  const [usersLoading, setUsersLoading] = useState(false);
  const [activeTab, setActiveTab] = useState("accounts");
  const [loading, setLoading] = useState({});
  const [confirm, setConfirm] = useState(null);

  const loadUsers = useCallback(async () => {
    setUsersLoading(true);
    try {
      const data = await adminAPI.listUsers();
      setUsers(data);
    } catch (e) {
      addToast("error", e.message);
    }
    setUsersLoading(false);
  }, [addToast]);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  const act = async (key, fn, msg) => {
    setLoading(p => ({ ...p, [key]: true }));
    try {
      const r = await fn();
      addToast("success", r.message || msg);
      await loadUsers();
      if (onRefresh) await onRefresh();
    }
    catch (e) {
      addToast("error", e.message);
    }
    setLoading(p => ({ ...p, [key]: false }));
    setConfirm(null);
  };

  const totalBalance = accounts.reduce((s, a) => s + a.balance, 0);

  return (
    <div className="page">
      <div className="page-header">
        <h1>Admin Control Panel</h1>
        <p>Administrative actions and overview — handle with care</p>
      </div>

      <div className="page-body">
        {/* Stats Row */}
        <div className="stat-grid" style={{ marginBottom: 24 }}>
          <div className="stat-card" style={{ animationDelay: "0s" }}>
            <div className="stat-icon">💰</div>
            <div className="stat-label">Total Combined Balance</div>
            <div className="stat-value" style={{ color: "#00D4AA" }}>{fmt(totalBalance)}</div>
            <div className="stat-sub">Across all user accounts</div>
          </div>
          <div className="stat-card" style={{ animationDelay: "0.08s" }}>
            <div className="stat-icon">💳</div>
            <div className="stat-label">Total Accounts</div>
            <div className="stat-value">{accounts.length}</div>
            <div className="stat-sub">Savings and current accounts</div>
          </div>
          <div className="stat-card" style={{ animationDelay: "0.16s" }}>
            <div className="stat-icon">👥</div>
            <div className="stat-label">Registered Users</div>
            <div className="stat-value">{usersLoading ? "..." : users.length}</div>
            <div className="stat-sub">Customers and administrators</div>
          </div>
        </div>

        {/* Tab Row */}
        <div className="tabs-row">
          <button className={`tab-btn ${activeTab === "accounts" ? "active" : ""}`} onClick={() => setActiveTab("accounts")}>
            💳 Manage Accounts
          </button>
          <button className={`tab-btn ${activeTab === "users" ? "active" : ""}`} onClick={() => setActiveTab("users")}>
            👥 Manage Users
          </button>
        </div>

        {activeTab === "accounts" ? (
          <div className="table-wrap card-enter">
            <table>
              <thead>
                <tr>
                  <th>Account Number</th>
                  <th>Owner Name</th>
                  <th>Account Type</th>
                  <th>Balance</th>
                  <th>Status</th>
                  <th style={{ textAlign: "right" }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {accounts.length === 0 ? (
                  <tr>
                    <td colSpan={6} style={{ textAlign: "center", color: "var(--muted)", padding: 24 }}>
                      No accounts exist in the database.
                    </td>
                  </tr>
                ) : (
                  accounts.map(a => (
                    <tr key={a.accountNumber}>
                      <td className="mono" style={{ fontWeight: 600 }}>{a.accountNumber}</td>
                      <td>{a.ownerName || "N/A"}</td>
                      <td>
                        <span className={`tx-type ${a.accountType === "SAVINGS" ? "tx-deposit" : "tx-transfer"}`}>
                          {a.accountType}
                        </span>
                      </td>
                      <td className="mono" style={{ fontWeight: 600 }}>{fmt(a.balance)}</td>
                      <td>
                        <span className={`tx-type ${a.active ? "tx-deposit" : "tx-withdraw"}`}>
                          {a.active ? "● Active" : "■ Frozen"}
                        </span>
                      </td>
                      <td style={{ textAlign: "right" }}>
                        <div style={{ display: "inline-flex", gap: 8 }}>
                          {a.active ? (
                            <button className="btn btn-warn btn-sm" style={{ padding: "4px 10px" }}
                              onClick={() => setConfirm({
                                key: `freeze-${a.accountNumber}`,
                                label: `Are you sure you want to freeze and deactivate account ${a.accountNumber}? This will block deposits and withdrawals.`,
                                fn: () => adminAPI.freeze(a.accountNumber)
                              })}
                              disabled={loading[`freeze-${a.accountNumber}`]}>
                              🧊 Freeze
                            </button>
                          ) : (
                            <button className="btn btn-primary btn-sm" style={{ padding: "4px 10px", background: "rgba(0,212,170,0.12)", color: "var(--accent)" }}
                              onClick={() => setConfirm({
                                key: `activate-${a.accountNumber}`,
                                label: `Are you sure you want to unblock and activate account ${a.accountNumber}?`,
                                fn: () => adminAPI.unblock(a.accountNumber)
                              })}
                              disabled={loading[`activate-${a.accountNumber}`]}>
                              ⚡ Activate
                            </button>
                          )}
                          <button className="btn btn-danger btn-sm" style={{ padding: "4px 10px" }}
                            onClick={() => setConfirm({
                              key: `del-acc-${a.accountNumber}`,
                              label: `Are you sure you want to permanently delete account ${a.accountNumber}? This cannot be undone.`,
                              fn: () => adminAPI.deleteAccount(a.accountNumber)
                            })}
                            disabled={loading[`del-acc-${a.accountNumber}`]}>
                            🗑️ Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="table-wrap card-enter">
            <table>
              <thead>
                <tr>
                  <th>User ID</th>
                  <th>Full Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Accounts</th>
                  <th style={{ textAlign: "right" }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {usersLoading ? (
                  <tr>
                    <td colSpan={6} style={{ textAlign: "center", color: "var(--muted)", padding: 24 }}>
                      Loading users list...
                    </td>
                  </tr>
                ) : users.length === 0 ? (
                  <tr>
                    <td colSpan={6} style={{ textAlign: "center", color: "var(--muted)", padding: 24 }}>
                      No users found.
                    </td>
                  </tr>
                ) : (
                  users.map(u => (
                    <tr key={u.userId}>
                      <td className="mono">{u.userId}</td>
                      <td style={{ fontWeight: 600 }}>{u.fullName}</td>
                      <td>{u.email}</td>
                      <td>
                        <span className={`tx-type ${u.role === "ADMIN" ? "tx-withdraw" : "tx-deposit"}`}>
                          {u.role}
                        </span>
                      </td>
                      <td>
                        {u.accountNumbers && u.accountNumbers.length > 0 ? (
                          <div style={{ display: "flex", flexWrap: "wrap", gap: 4 }}>
                            {u.accountNumbers.map(n => (
                              <span key={n} className="mono" style={{ background: "rgba(255,255,255,0.06)", padding: "2px 6px", borderRadius: 4, fontSize: 11 }}>
                                {n}
                              </span>
                            ))}
                          </div>
                        ) : (
                          <span style={{ color: "var(--muted)", fontSize: 13 }}>No accounts</span>
                        )}
                      </td>
                      <td style={{ textAlign: "right" }}>
                        <button className="btn btn-danger btn-sm" style={{ padding: "4px 10px" }}
                          disabled={u.email === "admin@securebank.com" || loading[`del-user-${u.userId}`]}
                          onClick={() => setConfirm({
                            key: `del-user-${u.userId}`,
                            label: `Are you sure you want to delete user "${u.fullName}" (${u.email}) and all their associated bank accounts? This action is permanent and irreversible.`,
                            fn: () => adminAPI.deleteUser(u.userId)
                          })}>
                          🗑️ Delete User
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}

        {confirm && (
          <Dialog title="Confirm Admin Action" sub={confirm.label} onClose={() => setConfirm(null)}>
            <div className="alert-strip alert-warn" style={{ marginBottom: 16 }}>
              ⚠️ Administrative overrides bypass normal checks and are permanent.
            </div>
            <div style={{ display: "flex", gap: 10 }}>
              <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setConfirm(null)}>Cancel</button>
              <button className="btn btn-danger" style={{ flex: 1 }}
                onClick={() => act(confirm.key, confirm.fn, "Action completed successfully")}
                disabled={loading[confirm.key]}>
                {loading[confirm.key] ? <Spinner /> : "Confirm Action"}
              </button>
            </div>
          </Dialog>
        )}
      </div>
    </div>
  );
}

// ─── APP ROOT ─────────────────────────────────────────────────────────────────
export default function App() {
  const [auth, setAuth] = useState(() => !!localStorage.getItem("authToken"));
  const [authPage, setAuthPage] = useState("login");
  const [page, setPage] = useState("dashboard");
  const [accounts, setAccounts] = useState([]);
  const [accsLoading, setAccsLoading] = useState(false);
  const [toasts, setToasts] = useState([]);
  const [userEmail, setUserEmail] = useState(() => {
    const email = localStorage.getItem("userEmail");
    if (email) return email;
    const token = localStorage.getItem("authToken");
    if (token) {
      const parsed = parseJwt(token);
      return parsed ? parsed.sub : "";
    }
    return "";
  });
  const [userRole, setUserRole] = useState(() => {
    const role = localStorage.getItem("userRole");
    if (role) return role;
    const token = localStorage.getItem("authToken");
    if (token) {
      const parsed = parseJwt(token);
      return parsed ? parsed.role : "CUSTOMER";
    }
    return "CUSTOMER";
  });
  const [registerMsg, setRegisterMsg] = useState("");

  const addToast = useCallback((type, msg) => {
    const id = Date.now();
    setToasts(t => [...t, { id, type, msg }]);
    setTimeout(() => setToasts(t => t.filter(x => x.id !== id)), 4000);
  }, []);

  const loadAccounts = useCallback(async () => {
    if (!userEmail) return;
    setAccsLoading(true);
    try {
      const data = await accountAPI.list();
      const unique = data.filter((v, i, a) => a.findIndex(t => t.accountNumber === v.accountNumber) === i);
      
      const isAdminUser = userRole === "ADMIN" || userEmail === "admin@securebank.com";
      const filtered = isAdminUser 
        ? unique 
        : unique.filter(acc => acc.ownerEmail === userEmail);
        
      setAccounts(filtered);
    }
    catch (e) { console.error(e); }
    setAccsLoading(false);
  }, [userEmail, userRole]);

  useEffect(() => {
    if (auth) loadAccounts();
  }, [auth, loadAccounts]);

  const handleLogin = () => {
    const token = localStorage.getItem("authToken");
    if (token) {
      const parsed = parseJwt(token);
      if (parsed) {
        if (parsed.sub) {
          localStorage.setItem("userEmail", parsed.sub);
          setUserEmail(parsed.sub);
        }
        if (parsed.role) {
          localStorage.setItem("userRole", parsed.role);
          setUserRole(parsed.role);
        }
      }
    }
    setAuth(true);
  };

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("userEmail");
    localStorage.removeItem("userRole");
    setAuth(false);
    setAuthPage("login");
    setAccounts([]);
    setPage("dashboard");
    setUserRole("CUSTOMER");
  };

  if (!auth) {
    return (
      <>
        <style>{STYLES}</style>
        {authPage === "login"
          ? <LoginPage
              onLogin={handleLogin}
              onSwitch={() => setAuthPage("register")}
              initMsg={registerMsg}
            />
          : <RegisterPage
              onSwitch={() => setAuthPage("login")}
              onSuccess={setRegisterMsg}
            />
        }
        <Toast toasts={toasts} />
      </>
    );
  }

  const isAdmin = userRole === "ADMIN" || userEmail === "admin@securebank.com";

  const renderPage = () => {
    switch (page) {
      case "dashboard":    return <DashboardPage accounts={accounts} onNav={setPage} addToast={addToast} />;
      case "accounts":     return <AccountsPage accounts={accounts} loading={accsLoading} />;
      case "create-account": return <CreateAccountPage accounts={accounts} userEmail={userEmail} onRefresh={loadAccounts} addToast={addToast} />;
      case "transactions": return <TransactionsPage accounts={accounts} addToast={addToast} />;
      case "history":      return <HistoryPage accounts={accounts} addToast={addToast} />;
      case "admin":        return isAdmin ? <AdminPage accounts={accounts} addToast={addToast} onRefresh={loadAccounts} /> : <DashboardPage accounts={accounts} onNav={setPage} addToast={addToast} />;
      default:             return <DashboardPage accounts={accounts} onNav={setPage} addToast={addToast} />;
    }
  };

  return (
    <>
      <style>{STYLES}</style>
      <div className="app-shell">
        <Sidebar page={page} onNav={setPage} onLogout={handleLogout} userEmail={userEmail} userRole={userRole} />
        <div className="main-content">
          {renderPage()}
        </div>
      </div>
      <Toast toasts={toasts} />
    </>
  );
}
