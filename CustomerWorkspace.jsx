import React, { startTransition, useDeferredValue, useEffect, useState } from "react";
import { api } from "../api";
import { computeBill, formatDate, formatDateTime, formatMoney, matchesSearch } from "../utils";
import { AttachmentLink, EmptyState, Panel, StatCard, StatusBadge, WorkspaceShell } from "./WorkspaceShell";

const defaultOrderForm = {
  serviceId: "",
  quantity: 1,
  deliveryDate: "",
  imageUrl: "",
  otpCode: ""
};

const defaultComplaintForm = {
  description: "",
  attachmentUrl: ""
};

export function CustomerWorkspace({ viewer, token, onSignOut }) {
  const [tab, setTab] = useState("place-order");
  const [services, setServices] = useState([]);
  const [orders, setOrders] = useState([]);
  const [complaints, setComplaints] = useState([]);
  const [orderForm, setOrderForm] = useState(defaultOrderForm);
  const [complaintForm, setComplaintForm] = useState(defaultComplaintForm);
  const [otpPreview, setOtpPreview] = useState(null);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [serviceSearch, setServiceSearch] = useState("");
  const [orderSearch, setOrderSearch] = useState("");

  const deferredServiceSearch = useDeferredValue(serviceSearch);
  const deferredOrderSearch = useDeferredValue(orderSearch);
  const selectedService = services.find((service) => Number(service.id) === Number(orderForm.serviceId));
  const billPreview = computeBill(selectedService, orderForm.quantity, orderForm.deliveryDate);

  useEffect(() => {
    loadWorkspace();
  }, [token]);

  async function loadWorkspace() {
    setBusy(true);
    setError("");

    try {
      const [servicesResponse, ordersResponse, complaintsResponse] = await Promise.all([
        api.getServices(),
        api.getOrders(token),
        api.getComplaints(token)
      ]);

      startTransition(() => {
        setServices(servicesResponse);
        setOrders(ordersResponse);
        setComplaints(complaintsResponse);
      });
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function requestOtp() {
    setBusy(true);
    setError("");
    setMessage("");

    try {
      const response = await api.requestOrderOtp(token);
      setOtpPreview(response);
      setMessage(response.message);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleOrderImageUpload(event) {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }

    setBusy(true);
    setError("");
    setMessage("");

    try {
      const response = await api.uploadImage(token, file);
      setOrderForm((current) => ({ ...current, imageUrl: response.url }));
      setMessage("Order image uploaded.");
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleComplaintImageUpload(event) {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }

    setBusy(true);
    setError("");
    setMessage("");

    try {
      const response = await api.uploadImage(token, file);
      setComplaintForm((current) => ({ ...current, attachmentUrl: response.url }));
      setMessage("Complaint attachment uploaded.");
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function handlePlaceOrder(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setMessage("");

    try {
      await api.createOrder(token, {
        serviceId: Number(orderForm.serviceId),
        quantity: Number(orderForm.quantity),
        deliveryDate: orderForm.deliveryDate,
        imageUrl: orderForm.imageUrl,
        otpCode: orderForm.otpCode
      });
      setOrderForm(defaultOrderForm);
      setOtpPreview(null);
      setMessage("Order placed successfully.");
      await loadWorkspace();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleMarkReceived(orderCode) {
    setBusy(true);
    setError("");
    setMessage("");

    try {
      await api.markReceived(token, orderCode);
      setMessage(`Order ${orderCode} marked as received.`);
      await loadWorkspace();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleComplaintSubmit(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setMessage("");

    try {
      await api.createComplaint(token, complaintForm);
      setComplaintForm(defaultComplaintForm);
      setMessage("Complaint lodged successfully.");
      await loadWorkspace();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  const filteredServices = services.filter((service) =>
    matchesSearch([service.serviceCode, service.clothType, service.serviceType], deferredServiceSearch)
  );
  const filteredOrders = orders.filter((order) =>
    matchesSearch([order.orderCode, order.serviceType, order.status, order.clothType], deferredOrderSearch)
  );

  return (
    <WorkspaceShell
      viewer={viewer}
      label="Customer Workspace"
      title="Book, track, and follow up without leaving the browser"
      subtitle="This replaces the desktop customer panes with a web flow that feels lighter and easier to use."
      navItems={[
        { id: "place-order", label: "Place Order", hint: "OTP + billing" },
        { id: "history", label: "Order History", hint: "Track deliveries" },
        { id: "services", label: "Services", hint: "Browse pricing" },
        { id: "complaints", label: "Complaints", hint: "Lodge tickets" }
      ]}
      activeTab={tab}
      onTabChange={(nextTab) => startTransition(() => setTab(nextTab))}
      onSignOut={onSignOut}
    >
      <div className="banner-stack">
        {busy ? <div className="banner banner-info">Refreshing customer workspace...</div> : null}
        {error ? <div className="banner banner-error">{error}</div> : null}
        {message ? <div className="banner banner-success">{message}</div> : null}
        {otpPreview ? (
          <div className="banner banner-info">
            Order OTP for local dev: <strong>{otpPreview.otpCode}</strong>
          </div>
        ) : null}
      </div>

      <div className="stats-grid">
        <StatCard label="Active Orders" value={orders.filter((order) => order.status === "ACTIVE").length} tone="teal" />
        <StatCard label="Open Complaints" value={complaints.filter((complaint) => complaint.status === "OPEN").length} tone="amber" />
        <StatCard label="Estimated Bill" value={formatMoney(billPreview)} tone="coral" />
        <StatCard label="Catalog Size" value={services.length} tone="slate" />
      </div>

      {tab === "place-order" ? (
        <div className="two-up-grid align-start">
          <Panel title="New Order" subtitle="Choose a service, set the delivery date, upload a garment photo if needed, then confirm with OTP.">
            <form className="form-grid" onSubmit={handlePlaceOrder}>
              <label>
                <span>Service</span>
                <select
                  value={orderForm.serviceId}
                  onChange={(event) => setOrderForm((current) => ({ ...current, serviceId: event.target.value }))}
                >
                  <option value="">Select a service</option>
                  {services.map((service) => (
                    <option key={service.id} value={service.id}>
                      {service.clothType} · {service.serviceType}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                <span>Quantity</span>
                <input
                  type="number"
                  min="1"
                  value={orderForm.quantity}
                  onChange={(event) => setOrderForm((current) => ({ ...current, quantity: event.target.value }))}
                />
              </label>
              <label>
                <span>Delivery Date</span>
                <input
                  type="date"
                  value={orderForm.deliveryDate}
                  onChange={(event) => setOrderForm((current) => ({ ...current, deliveryDate: event.target.value }))}
                />
              </label>
              <label>
                <span>Garment Image</span>
                <input type="file" accept="image/*" onChange={handleOrderImageUpload} />
              </label>
              <div className="inline-action-row">
                <button className="secondary-button" type="button" onClick={requestOtp} disabled={busy}>
                  Request OTP
                </button>
                <small>OTP is simulated on screen in this local build.</small>
              </div>
              <label>
                <span>OTP Code</span>
                <input
                  value={orderForm.otpCode}
                  onChange={(event) => setOrderForm((current) => ({ ...current, otpCode: event.target.value }))}
                  placeholder="Enter the OTP"
                />
              </label>
              <button className="primary-button" type="submit" disabled={busy}>
                Place Order
              </button>
            </form>
          </Panel>

          <Panel title="Live Bill Preview" subtitle="The bill mirrors the old premium rule: closer delivery dates add urgency charges for the first five days.">
            <div className="summary-stack">
              <div className="summary-row">
                <span>Selected Service</span>
                <strong>{selectedService ? `${selectedService.clothType} · ${selectedService.serviceType}` : "Choose a service"}</strong>
              </div>
              <div className="summary-row">
                <span>Price Per Item</span>
                <strong>{selectedService ? formatMoney(selectedService.pricePerItem) : "--"}</strong>
              </div>
              <div className="summary-row">
                <span>Premium Per Day</span>
                <strong>{selectedService ? formatMoney(selectedService.premiumPerDay) : "--"}</strong>
              </div>
              <div className="summary-row highlight-row">
                <span>Estimated Total</span>
                <strong>{formatMoney(billPreview)}</strong>
              </div>
              <div className="summary-row">
                <span>Uploaded Image</span>
                <AttachmentLink url={orderForm.imageUrl} label="Preview" />
              </div>
            </div>
          </Panel>
        </div>
      ) : null}

      {tab === "history" ? (
        <Panel
          title="Order History"
          subtitle="Track live orders and acknowledge delivery when garments are received."
          actions={
            <input
              className="search-input"
              value={orderSearch}
              onChange={(event) => setOrderSearch(event.target.value)}
              placeholder="Search your orders"
            />
          }
        >
          {filteredOrders.length ? (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Order</th>
                    <th>Cloth</th>
                    <th>Service</th>
                    <th>Order Date</th>
                    <th>Delivery</th>
                    <th>Bill</th>
                    <th>Image</th>
                    <th>Status</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {filteredOrders.map((order) => (
                    <tr key={order.orderCode}>
                      <td>{order.orderCode}</td>
                      <td>{order.clothType}</td>
                      <td>{order.serviceType}</td>
                      <td>{formatDate(order.orderDate)}</td>
                      <td>{formatDate(order.deliveryDate)}</td>
                      <td>{formatMoney(order.bill)}</td>
                      <td><AttachmentLink url={order.imageUrl} label="View" /></td>
                      <td><StatusBadge status={order.status} /></td>
                      <td>
                        {order.status === "ACTIVE" ? (
                          <button type="button" className="ghost-button small" onClick={() => handleMarkReceived(order.orderCode)}>
                            Mark Received
                          </button>
                        ) : null}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <EmptyState title="No matching orders" text="Once you place an order, it will appear here." />
          )}
        </Panel>
      ) : null}

      {tab === "services" ? (
        <Panel
          title="Available Services"
          subtitle="Browse the live catalog pulled from the admin-managed service table."
          actions={
            <input
              className="search-input"
              value={serviceSearch}
              onChange={(event) => setServiceSearch(event.target.value)}
              placeholder="Search services"
            />
          }
        >
          {filteredServices.length ? (
            <div className="service-card-grid">
              {filteredServices.map((service) => (
                <article key={service.id} className="service-card">
                  <span className="eyebrow">{service.serviceCode}</span>
                  <h4>{service.clothType}</h4>
                  <p>{service.serviceType}</p>
                  <div className="price-pair">
                    <span>{formatMoney(service.pricePerItem)} per item</span>
                    <span>{formatMoney(service.premiumPerDay)} premium per day</span>
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <EmptyState title="No services found" text="Try a different keyword to browse the catalog." />
          )}
        </Panel>
      ) : null}

      {tab === "complaints" ? (
        <div className="two-up-grid align-start">
          <Panel title="Lodge Complaint" subtitle="Describe the issue, attach supporting imagery, and submit a ticket for the admin queue.">
            <form className="form-grid" onSubmit={handleComplaintSubmit}>
              <label>
                <span>Description</span>
                <textarea
                  rows="8"
                  value={complaintForm.description}
                  onChange={(event) => setComplaintForm((current) => ({ ...current, description: event.target.value }))}
                  placeholder="Explain the issue clearly"
                />
              </label>
              <label>
                <span>Attachment</span>
                <input type="file" accept="image/*" onChange={handleComplaintImageUpload} />
              </label>
              <div className="summary-row">
                <span>Uploaded File</span>
                <AttachmentLink url={complaintForm.attachmentUrl} label="Preview" />
              </div>
              <button className="primary-button" type="submit" disabled={busy}>
                Submit Complaint
              </button>
            </form>
          </Panel>

          <Panel title="Your Tickets" subtitle="Recent complaint history and resolution timestamps.">
            {complaints.length ? (
              <div className="list-stack">
                {complaints.map((complaint) => (
                  <article key={complaint.ticketCode} className="complaint-card">
                    <div className="complaint-header-row">
                      <div>
                        <span className="eyebrow">{complaint.ticketCode}</span>
                        <h4>{complaint.status === "OPEN" ? "Awaiting resolution" : "Resolved ticket"}</h4>
                      </div>
                      <StatusBadge status={complaint.status} />
                    </div>
                    <p className="complaint-body">{complaint.description}</p>
                    <div className="complaint-footer-row">
                      <span>{formatDateTime(complaint.createdAt)}</span>
                      <AttachmentLink url={complaint.attachmentUrl} label="Attachment" />
                    </div>
                  </article>
                ))}
              </div>
            ) : (
              <EmptyState title="No complaints yet" text="Your support tickets will show up here after submission." />
            )}
          </Panel>
        </div>
      ) : null}
    </WorkspaceShell>
  );
}
