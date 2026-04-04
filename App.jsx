import React, { startTransition, useEffect, useState } from "react";
import { api } from "./api";
import { AuthScreen } from "./components/AuthScreen";
import { AdminWorkspace } from "./components/AdminWorkspace";
import { CustomerWorkspace } from "./components/CustomerWorkspace";

const TOKEN_KEY = "laundry-flow-token";

export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY) || "");
  const [viewer, setViewer] = useState(null);
  const [booting, setBooting] = useState(Boolean(token));
  const [bootError, setBootError] = useState("");

  useEffect(() => {
    let cancelled = false;

    if (!token) {
      setViewer(null);
      setBooting(false);
      return undefined;
    }

    setBooting(true);
    api.me(token)
      .then((user) => {
        if (cancelled) {
          return;
        }
        startTransition(() => {
          setViewer(user);
          setBootError("");
        });
      })
      .catch((requestError) => {
        if (cancelled) {
          return;
        }
        localStorage.removeItem(TOKEN_KEY);
        setToken("");
        setViewer(null);
        setBootError(requestError.message);
      })
      .finally(() => {
        if (!cancelled) {
          setBooting(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [token]);

  function handleAuthSuccess(response) {
    localStorage.setItem(TOKEN_KEY, response.token);
    startTransition(() => {
      setToken(response.token);
      setViewer(response.user);
      setBootError("");
    });
  }

  async function handleSignOut() {
    try {
      if (token) {
        await api.logout(token);
      }
    } catch {
      // Ignore logout failures during local sign-out.
    }

    localStorage.removeItem(TOKEN_KEY);
    setToken("");
    setViewer(null);
  }

  if (booting) {
    return (
      <div className="splash-screen">
        <div className="splash-card">
          <span className="eyebrow">Booting Workspace</span>
          <h1>Loading Laundry Flow</h1>
          <p>Rehydrating your session and preparing the new web dashboard.</p>
        </div>
      </div>
    );
  }

  if (!viewer) {
    return <AuthScreen onAuthSuccess={handleAuthSuccess} initialError={bootError} />;
  }

  if (viewer.role === "ADMIN") {
    return <AdminWorkspace viewer={viewer} token={token} onSignOut={handleSignOut} />;
  }

  return <CustomerWorkspace viewer={viewer} token={token} onSignOut={handleSignOut} />;
}
