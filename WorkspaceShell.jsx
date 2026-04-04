import React from "react";
import { resolveAssetUrl } from "../api";
import { formatMoney, statusTone } from "../utils";

export function WorkspaceShell({ viewer, label, title, subtitle, navItems, activeTab, onTabChange, onSignOut, children }) {
  return (
    <div className="workspace-shell">
      <aside className="workspace-sidebar">
        <div className="brand-lockup">
          <span className="brand-chip">Laundry Flow</span>
          <h1>Cleaner operations, rebuilt for the web.</h1>
          <p>{label}</p>
        </div>

        <div className="viewer-card">
          <span className="viewer-kicker">Signed In</span>
          <strong>{viewer.displayName}</strong>
          <span>{viewer.customerCode}</span>
          <span>{viewer.phone}</span>
        </div>

        <nav className="nav-stack">
          {navItems.map((item) => (
            <button
              key={item.id}
              type="button"
              className={item.id === activeTab ? "nav-button is-active" : "nav-button"}
              onClick={() => onTabChange(item.id)}
            >
              <span>{item.label}</span>
              <small>{item.hint}</small>
            </button>
          ))}
        </nav>

        <button type="button" className="ghost-button sidebar-logout" onClick={onSignOut}>
          Sign Out
        </button>
      </aside>

      <main className="workspace-main">
        <header className="workspace-header">
          <div>
            <span className="eyebrow">{label}</span>
            <h2>{title}</h2>
            <p>{subtitle}</p>
          </div>
          <div className="header-orb">
            <span>Web remake</span>
            <strong>Spring Boot + React</strong>
          </div>
        </header>

        {children}
      </main>
    </div>
  );
}

export function Panel({ title, subtitle, actions, children, className = "" }) {
  return (
    <section className={`panel ${className}`.trim()}>
      <div className="panel-header">
        <div>
          <h3>{title}</h3>
          {subtitle ? <p>{subtitle}</p> : null}
        </div>
        {actions ? <div className="panel-actions">{actions}</div> : null}
      </div>
      {children}
    </section>
  );
}

export function StatCard({ label, value, tone = "teal" }) {
  return (
    <div className={`stat-card tone-${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

export function TrendBars({ points, currency = false }) {
  if (!points?.length) {
    return <div className="empty-inline">No history yet.</div>;
  }

  const maxValue = Math.max(...points.map((point) => Number(point.value ?? 0)), 1);

  return (
    <div className="trend-bars">
      {points.slice(-7).map((point) => {
        const height = Math.max((Number(point.value ?? 0) / maxValue) * 100, 8);
        return (
          <div key={point.label} className="trend-slot">
            <span className="trend-bar-fill" style={{ height: `${height}%` }} />
            <small>{point.label}</small>
            <strong>{currency ? formatMoney(point.value) : Number(point.value)}</strong>
          </div>
        );
      })}
    </div>
  );
}

export function StatusBadge({ status }) {
  return <span className={statusTone(status)}>{status}</span>;
}

export function AttachmentLink({ url, label = "Open" }) {
  if (!url) {
    return <span className="muted-text">No file</span>;
  }
  return (
    <a className="inline-link" href={resolveAssetUrl(url)} target="_blank" rel="noreferrer">
      {label}
    </a>
  );
}

export function EmptyState({ title, text }) {
  return (
    <div className="empty-state">
      <h4>{title}</h4>
      <p>{text}</p>
    </div>
  );
}
