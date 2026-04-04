import React, { useEffect, useState } from "react";
import { api } from "../api";

const defaultLogin = { customerCode: "", password: "" };
const defaultSignup = { displayName: "", phone: "", password: "", securityKey: "" };
const defaultReset = { customerCode: "", securityKey: "", otpCode: "", newPassword: "" };

export function AuthScreen({ onAuthSuccess, initialError = "" }) {
  const [mode, setMode] = useState("login");
  const [loginForm, setLoginForm] = useState(defaultLogin);
  const [signupForm, setSignupForm] = useState(defaultSignup);
  const [resetForm, setResetForm] = useState(defaultReset);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(initialError);
  const [message, setMessage] = useState("");
  const [otpPreview, setOtpPreview] = useState(null);

  useEffect(() => {
    setError(initialError);
  }, [initialError]);

  async function handleLogin(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setMessage("");

    try {
      const response = await api.login(loginForm);
      onAuthSuccess(response);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleSignup(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setMessage("");

    try {
      const response = await api.signup(signupForm);
      onAuthSuccess(response);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleRequestOtp() {
    setBusy(true);
    setError("");
    setMessage("");

    try {
      const response = await api.requestPasswordResetOtp({
        customerCode: resetForm.customerCode,
        securityKey: resetForm.securityKey
      });
      setOtpPreview(response);
      setMessage(response.message);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleResetPassword(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setMessage("");

    try {
      await api.resetPassword(resetForm);
      setMode("login");
      setLoginForm({ customerCode: resetForm.customerCode, password: "" });
      setResetForm(defaultReset);
      setOtpPreview(null);
      setMessage("Password changed. Sign in with the new password.");
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-shell">
      <section className="auth-hero">
        <div className="floating-blur blur-one" />
        <div className="floating-blur blur-two" />
        <span className="eyebrow">Laundry Management Remade</span>
        <h1>From a JavaFX desktop app to a full web workflow.</h1>
        <p>
          The legacy flows are preserved here: account signup, admin dashboard, service management,
          order placement with OTP confirmation, order tracking, and complaint resolution.
        </p>

        <div className="hero-grid">
          <article className="hero-tile">
            <span>Admin Demo</span>
            <strong>`ADMIN` / `admin123`</strong>
            <p>Use this to view the dashboard, manage services, inspect orders, and resolve complaints.</p>
          </article>
          <article className="hero-tile">
            <span>Customer Demo</span>
            <strong>`U-100001` / `Welcome@1`</strong>
            <p>Use this to place orders, request OTPs, mark deliveries as received, and lodge complaints.</p>
          </article>
          <article className="hero-tile">
            <span>Dev OTP Mode</span>
            <strong>Codes are shown on screen</strong>
            <p>The old SMS dependency was replaced with a safe in-app OTP simulation for local development.</p>
          </article>
        </div>
      </section>

      <section className="auth-panel">
        <div className="mode-switch">
          {[
            ["login", "Login"],
            ["signup", "Sign Up"],
            ["reset", "Reset Password"]
          ].map(([value, label]) => (
            <button
              key={value}
              type="button"
              className={mode === value ? "mode-button is-active" : "mode-button"}
              onClick={() => {
                setMode(value);
                setError("");
                setMessage("");
              }}
            >
              {label}
            </button>
          ))}
        </div>

        {error ? <div className="banner banner-error">{error}</div> : null}
        {message ? <div className="banner banner-success">{message}</div> : null}
        {otpPreview ? (
          <div className="banner banner-info">
            Dev OTP: <strong>{otpPreview.otpCode}</strong>
          </div>
        ) : null}

        {mode === "login" ? (
          <form className="form-grid" onSubmit={handleLogin}>
            <label>
              <span>Customer ID</span>
              <input
                value={loginForm.customerCode}
                onChange={(event) => setLoginForm((current) => ({ ...current, customerCode: event.target.value }))}
                placeholder="ADMIN or U-100001"
              />
            </label>
            <label>
              <span>Password</span>
              <input
                type="password"
                value={loginForm.password}
                onChange={(event) => setLoginForm((current) => ({ ...current, password: event.target.value }))}
                placeholder="Enter your password"
              />
            </label>
            <button className="primary-button" type="submit" disabled={busy}>
              {busy ? "Signing in..." : "Enter Workspace"}
            </button>
          </form>
        ) : null}

        {mode === "signup" ? (
          <form className="form-grid" onSubmit={handleSignup}>
            <label>
              <span>Name</span>
              <input
                value={signupForm.displayName}
                onChange={(event) => setSignupForm((current) => ({ ...current, displayName: event.target.value }))}
                placeholder="Your full name"
              />
            </label>
            <label>
              <span>Phone</span>
              <input
                value={signupForm.phone}
                onChange={(event) => setSignupForm((current) => ({ ...current, phone: event.target.value }))}
                placeholder="10 digit phone number"
              />
            </label>
            <label>
              <span>Password</span>
              <input
                type="password"
                value={signupForm.password}
                onChange={(event) => setSignupForm((current) => ({ ...current, password: event.target.value }))}
                placeholder="Use upper, lower, digit, special"
              />
            </label>
            <label>
              <span>Security Key</span>
              <input
                value={signupForm.securityKey}
                onChange={(event) => setSignupForm((current) => ({ ...current, securityKey: event.target.value }))}
                placeholder="A memorable backup answer"
              />
            </label>
            <button className="primary-button" type="submit" disabled={busy}>
              {busy ? "Creating account..." : "Create Account"}
            </button>
          </form>
        ) : null}

        {mode === "reset" ? (
          <form className="form-grid" onSubmit={handleResetPassword}>
            <label>
              <span>Customer ID</span>
              <input
                value={resetForm.customerCode}
                onChange={(event) => setResetForm((current) => ({ ...current, customerCode: event.target.value }))}
                placeholder="U-100001"
              />
            </label>
            <label>
              <span>Security Key</span>
              <input
                value={resetForm.securityKey}
                onChange={(event) => setResetForm((current) => ({ ...current, securityKey: event.target.value }))}
                placeholder="Your stored key"
              />
            </label>
            <div className="inline-action-row">
              <button className="secondary-button" type="button" onClick={handleRequestOtp} disabled={busy}>
                {busy ? "Working..." : "Request OTP"}
              </button>
              <small>Request the OTP first, then finish the reset below.</small>
            </div>
            <label>
              <span>OTP Code</span>
              <input
                value={resetForm.otpCode}
                onChange={(event) => setResetForm((current) => ({ ...current, otpCode: event.target.value }))}
                placeholder="6 digit OTP"
              />
            </label>
            <label>
              <span>New Password</span>
              <input
                type="password"
                value={resetForm.newPassword}
                onChange={(event) => setResetForm((current) => ({ ...current, newPassword: event.target.value }))}
                placeholder="New secure password"
              />
            </label>
            <button className="primary-button" type="submit" disabled={busy}>
              {busy ? "Updating password..." : "Reset Password"}
            </button>
          </form>
        ) : null}
      </section>
    </div>
  );
}
