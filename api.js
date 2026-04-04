const API_BASE_URL = import.meta.env?.VITE_API_BASE_URL ?? "";

export function resolveAssetUrl(url) {
  if (!url) {
    return "";
  }
  return url.startsWith("/") ? `${API_BASE_URL}${url}` : url;
}

async function request(path, { method = "GET", body, token, isFormData = false } = {}) {
  const headers = {};

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  if (!isFormData) {
    headers["Content-Type"] = "application/json";
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: isFormData ? body : body ? JSON.stringify(body) : undefined
  });

  const contentType = response.headers.get("content-type") || "";
  const payload = contentType.includes("application/json") ? await response.json() : await response.text();

  if (!response.ok) {
    const message = typeof payload === "object" && payload?.message
      ? payload.message
      : typeof payload === "string" && payload
        ? payload
        : "Request failed.";
    throw new Error(message);
  }

  return payload;
}

export const api = {
  login(payload) {
    return request("/api/auth/login", { method: "POST", body: payload });
  },
  signup(payload) {
    return request("/api/auth/signup", { method: "POST", body: payload });
  },
  me(token) {
    return request("/api/auth/me", { token });
  },
  logout(token) {
    return request("/api/auth/logout", { method: "POST", token });
  },
  requestOrderOtp(token) {
    return request("/api/auth/request-order-otp", { method: "POST", token });
  },
  requestPasswordResetOtp(payload) {
    return request("/api/auth/request-password-reset-otp", { method: "POST", body: payload });
  },
  resetPassword(payload) {
    return request("/api/auth/reset-password", { method: "POST", body: payload });
  },
  getDashboard(token) {
    return request("/api/dashboard", { token });
  },
  getServices() {
    return request("/api/services");
  },
  createService(token, payload) {
    return request("/api/services", { method: "POST", token, body: payload });
  },
  updateService(token, id, payload) {
    return request(`/api/services/${id}`, { method: "PUT", token, body: payload });
  },
  deleteService(token, id) {
    return request(`/api/services/${id}`, { method: "DELETE", token });
  },
  getOrders(token) {
    return request("/api/orders", { token });
  },
  createOrder(token, payload) {
    return request("/api/orders", { method: "POST", token, body: payload });
  },
  markReceived(token, orderCode) {
    return request(`/api/orders/${orderCode}/receive`, { method: "POST", token });
  },
  getComplaints(token) {
    return request("/api/complaints", { token });
  },
  createComplaint(token, payload) {
    return request("/api/complaints", { method: "POST", token, body: payload });
  },
  resolveComplaint(token, ticketCode) {
    return request(`/api/complaints/${ticketCode}/resolve`, { method: "POST", token });
  },
  uploadImage(token, file) {
    const formData = new FormData();
    formData.append("file", file);
    return request("/api/uploads/image", {
      method: "POST",
      token,
      body: formData,
      isFormData: true
    });
  }
};
