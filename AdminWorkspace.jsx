import React, { startTransition, useDeferredValue, useEffect, useState } from "react";
import { api } from "../api";
import { formatDate, formatDateTime, formatMoney, matchesSearch } from "../utils";
import { AttachmentLink, EmptyState, Panel, StatCard, StatusBadge, TrendBars, WorkspaceShell } from "./WorkspaceShell";

const defaultServiceForm = {
  id: null,
  serviceCode: "",
  clothType: "",
  serviceType: "",
  pricePerItem: "",
  premiumPerDay: ""
};

export function AdminWorkspace({ viewer, token, onSignOut }) {
  const [tab, setTab] = useState("overview");
  const [dashboard, setDashboard] = useState(null);
  const [services, setServices] = useState([]);
  const [orders, setOrders] = useState([]);
  const [complaints, setComplaints] = useState([]);
  const [serviceForm, setServiceForm] = useState(defaultServiceForm);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [serviceSearch, setServiceSearch] = useState("");
  const [orderSearch, setOrderSearch] = useState("");
  const [complaintSearch, setComplaintSearch] = useState("");

  const deferredServiceSearch = useDeferredValue(serviceSearch);
  const deferredOrderSearch = useDeferredValue(orderSearch);
  const deferredComplaintSearch = useDeferredValue(complaintSearch);

  useEffect(() => {
    loadWorkspace();
  }, [token]);

  async function loadWorkspace() {
    setBusy(true);
    setError("");

    try {
      const [dashboardResponse, servicesResponse, ordersResponse, complaintsResponse] = await Promise.all([
        api.getDashboard(token),
        api.getServices(),
        api.getOrders(token),
        api.getComplaints(token)
      ]);

      startTransition(() => {
        setDashboard(dashboardResponse);
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

  async function handleSaveService(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setMessage("");

    const payload = {
      serviceCode: serviceForm.serviceCode,
      clothType: serviceForm.clothType,
      serviceType: serviceForm.serviceType,
      pricePerItem: Number(serviceForm.pricePerItem),
      premiumPerDay: Number(serviceForm.premiumPerDay)
    };

    try {
      if (serviceForm.id) {
        await api.updateService(token, serviceForm.id, payload);
        setMessage("Service updated.");
      } else {
        await api.createService(token, payload);
        setMessage("Service created.");
      }
      setServiceForm(defaultServiceForm);
      await loadWorkspace();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleDeleteService(id) {
    if (!window.confirm("Delete this service?")) {
      return;
    }

    setBusy(true);
    setError("");
    setMessage("");

    try {
      await api.deleteService(token, id);
      setServiceForm(defaultServiceForm);
      setMessage("Service deleted.");
      await loadWorkspace();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleResolveComplaint(ticketCode) {
    setBusy(true);
    setError("");
    setMessage("");

    try {
      await api.resolveComplaint(token, ticketCode);
      setMessage(`Complaint ${ticketCode} resolved.`);
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
    matchesSearch([order.orderCode, order.customerCode, order.customerName, order.serviceType, order.status], deferredOrderSearch)
  );
  const filteredComplaints = complaints.filter((complaint) =>
    matchesSearch([complaint.ticketCode, complaint.customerCode, complaint.customerName, complaint.status], deferredComplaintSearch)
  );

  return (
    <WorkspaceShell
      viewer={viewer}
      label="Admin Operations"
      title="A single control room for laundry operations"
      subtitle="Manage the service catalog, track live orders, and clear complaint queues from one place."
      navItems={[
        { id: "overview", label: "Overview", hint: "KPIs + charts" },
        { id: "services", label: "Services", hint: "Catalog CRUD" },
        { id: "orders", label: "Orders", hint: "Live and past orders" },
        { id: "complaints", label: "Complaints", hint: "Resolve tickets" }
      ]}
      activeTab={tab}
      onTabChange={(nextTab) => startTransition(() => setTab(nextTab))}
      onSignOut={onSignOut}
    >
      <div className="banner-stack">
        {busy ? <div className="banner banner-info">Refreshing admin workspace...</div> : null}
        {error ? <div className="banner banner-error">{error}</div> : null}
        {message ? <div className="banner banner-success">{message}</div> : null}
      </div>

      {tab === "overview" ? (
        <div className="content-stack">
          <div className="stats-grid">
            <StatCard label="Total Customers" value={dashboard ? dashboard.totalCustomers : 0} tone="teal" />
            <StatCard label="Total Orders" value={dashboard ? dashboard.totalOrders : 0} tone="amber" />
            <StatCard label="Today's Income" value={formatMoney(dashboard?.todaysIncome)} tone="coral" />
            <StatCard label="Open Complaints" value={dashboard ? dashboard.openComplaints : 0} tone="slate" />
          </div>

          <div className="two-up-grid">
            <Panel title="Customer Growth" subtitle="Recent signups across the seeded and live database.">
              <TrendBars points={dashboard?.signupsTrend ?? []} />
            </Panel>
            <Panel title="Order Volume" subtitle="Grouped by order date.">
              <TrendBars points={dashboard?.orderTrend ?? []} />
            </Panel>
          </div>

          <Panel title="Revenue Trend" subtitle="Income grouped by order date.">
            <TrendBars points={dashboard?.revenueTrend ?? []} currency />
          </Panel>

          <div className="two-up-grid">
            <Panel title="Recent Orders" subtitle="Newest orders across all customers.">
              {dashboard?.recentOrders?.length ? (
                <div className="table-wrap compact-table">
                  <table>
                    <thead>
                      <tr>
                        <th>Order</th>
                        <th>Customer</th>
                        <th>Delivery</th>
                        <th>Bill</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {dashboard.recentOrders.map((order) => (
                        <tr key={order.orderCode}>
                          <td>{order.orderCode}</td>
                          <td>{order.customerName}</td>
                          <td>{formatDate(order.deliveryDate)}</td>
                          <td>{formatMoney(order.bill)}</td>
                          <td><StatusBadge status={order.status} /></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <EmptyState title="No orders yet" text="Orders will appear here once customers start placing them." />
              )}
            </Panel>

            <Panel title="Recent Complaints" subtitle="Newest tickets from the customer side.">
              {dashboard?.recentComplaints?.length ? (
                <div className="list-stack">
                  {dashboard.recentComplaints.map((complaint) => (
                    <article key={complaint.ticketCode} className="list-card">
                      <div>
                        <strong>{complaint.ticketCode}</strong>
                        <p>{complaint.customerName}</p>
                      </div>
                      <StatusBadge status={complaint.status} />
                    </article>
                  ))}
                </div>
              ) : (
                <EmptyState title="No complaints yet" text="Resolved and open tickets will show up here." />
              )}
            </Panel>
          </div>
        </div>
      ) : null}

      {tab === "services" ? (
        <div className="two-up-grid align-start">
          <Panel
            title={serviceForm.id ? "Update Service" : "Add Service"}
            subtitle="Keep the cloth types, service types, price, and premium rates current."
          >
            <form className="form-grid" onSubmit={handleSaveService}>
              <label>
                <span>Service Code</span>
                <input
                  value={serviceForm.serviceCode}
                  onChange={(event) => setServiceForm((current) => ({ ...current, serviceCode: event.target.value }))}
                  placeholder="Optional custom code"
                />
              </label>
              <label>
                <span>Cloth Type</span>
                <input
                  value={serviceForm.clothType}
                  onChange={(event) => setServiceForm((current) => ({ ...current, clothType: event.target.value }))}
                  placeholder="Cotton"
                />
              </label>
              <label>
                <span>Service Type</span>
                <input
                  value={serviceForm.serviceType}
                  onChange={(event) => setServiceForm((current) => ({ ...current, serviceType: event.target.value }))}
                  placeholder="Washing and Ironing"
                />
              </label>
              <label>
                <span>Price Per Item</span>
                <input
                  type="number"
                  min="1"
                  value={serviceForm.pricePerItem}
                  onChange={(event) => setServiceForm((current) => ({ ...current, pricePerItem: event.target.value }))}
                  placeholder="120"
                />
              </label>
              <label>
                <span>Premium Per Day</span>
                <input
                  type="number"
                  min="0"
                  value={serviceForm.premiumPerDay}
                  onChange={(event) => setServiceForm((current) => ({ ...current, premiumPerDay: event.target.value }))}
                  placeholder="35"
                />
              </label>
              <div className="inline-action-row">
                <button className="primary-button" type="submit" disabled={busy}>
                  {serviceForm.id ? "Save Changes" : "Add Service"}
                </button>
                <button className="ghost-button" type="button" onClick={() => setServiceForm(defaultServiceForm)}>
                  Clear Form
                </button>
              </div>
            </form>
          </Panel>

          <Panel
            title="Service Catalog"
            subtitle="Search and edit any service row."
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
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Code</th>
                      <th>Cloth</th>
                      <th>Service</th>
                      <th>Price</th>
                      <th>Premium</th>
                      <th />
                    </tr>
                  </thead>
                  <tbody>
                    {filteredServices.map((service) => (
                      <tr key={service.id}>
                        <td>{service.serviceCode}</td>
                        <td>{service.clothType}</td>
                        <td>{service.serviceType}</td>
                        <td>{formatMoney(service.pricePerItem)}</td>
                        <td>{formatMoney(service.premiumPerDay)}</td>
                        <td>
                          <div className="table-actions">
                            <button
                              type="button"
                              className="ghost-button small"
                              onClick={() => setServiceForm({
                                id: service.id,
                                serviceCode: service.serviceCode,
                                clothType: service.clothType,
                                serviceType: service.serviceType,
                                pricePerItem: service.pricePerItem,
                                premiumPerDay: service.premiumPerDay
                              })}
                            >
                              Edit
                            </button>
                            <button
                              type="button"
                              className="ghost-button small danger"
                              onClick={() => handleDeleteService(service.id)}
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <EmptyState title="No matching services" text="Try a different search or add a new service." />
            )}
          </Panel>
        </div>
      ) : null}

      {tab === "orders" ? (
        <Panel
          title="All Orders"
          subtitle="Monitor every order across customers and track delivery status."
          actions={
            <input
              className="search-input"
              value={orderSearch}
              onChange={(event) => setOrderSearch(event.target.value)}
              placeholder="Search orders"
            />
          }
        >
          {filteredOrders.length ? (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Order</th>
                    <th>Customer</th>
                    <th>Service</th>
                    <th>Order Date</th>
                    <th>Delivery</th>
                    <th>Bill</th>
                    <th>Image</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredOrders.map((order) => (
                    <tr key={order.orderCode}>
                      <td>{order.orderCode}</td>
                      <td>
                        <strong>{order.customerName}</strong>
                        <span className="cell-subtext">{order.customerCode}</span>
                      </td>
                      <td>{order.serviceType}</td>
                      <td>{formatDate(order.orderDate)}</td>
                      <td>{formatDate(order.deliveryDate)}</td>
                      <td>{formatMoney(order.bill)}</td>
                      <td><AttachmentLink url={order.imageUrl} label="View" /></td>
                      <td><StatusBadge status={order.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <EmptyState title="No orders found" text="Try a broader query or wait for new activity." />
          )}
        </Panel>
      ) : null}

      {tab === "complaints" ? (
        <Panel
          title="Complaint Queue"
          subtitle="Customer issues and ticket attachments in one stream."
          actions={
            <input
              className="search-input"
              value={complaintSearch}
              onChange={(event) => setComplaintSearch(event.target.value)}
              placeholder="Search tickets"
            />
          }
        >
          {filteredComplaints.length ? (
            <div className="list-stack">
              {filteredComplaints.map((complaint) => (
                <article key={complaint.ticketCode} className="complaint-card">
                  <div className="complaint-header-row">
                    <div>
                      <span className="eyebrow">{complaint.ticketCode}</span>
                      <h4>{complaint.customerName}</h4>
                      <p>{complaint.customerCode} · {complaint.phoneNumber}</p>
                    </div>
                    <StatusBadge status={complaint.status} />
                  </div>
                  <p className="complaint-body">{complaint.description}</p>
                  <div className="complaint-footer-row">
                    <span>{formatDateTime(complaint.createdAt)}</span>
                    <AttachmentLink url={complaint.attachmentUrl} label="Attachment" />
                    {complaint.status === "OPEN" ? (
                      <button
                        type="button"
                        className="primary-button small"
                        onClick={() => handleResolveComplaint(complaint.ticketCode)}
                      >
                        Resolve
                      </button>
                    ) : null}
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <EmptyState title="No complaint matches" text="Adjust the search to surface the right tickets." />
          )}
        </Panel>
      ) : null}
    </WorkspaceShell>
  );
}
