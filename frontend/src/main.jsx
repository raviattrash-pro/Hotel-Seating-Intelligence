import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import {
  BarChart3,
  CalendarClock,
  CheckCircle2,
  Cigarette,
  ClipboardList,
  Coffee,
  CreditCard,
  ImageUp,
  IndianRupee,
  Layers,
  LockKeyhole,
  Minus,
  MapPinned,
  Moon,
  Plus,
  QrCode,
  Save,
  ShieldCheck,
  Sofa,
  Upload,
  Users,
  Utensils,
  Wine,
} from 'lucide-react';
import './styles.css';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
const today = new Date().toISOString().slice(0, 10);
const zones = ['FAMILY', 'GENERAL', 'SMOKING', 'BAR', 'OUTDOOR', 'PRIVATE'];
const zoneMeta = {
  FAMILY: { label: 'Family', icon: Users, color: '#12a26c' },
  GENERAL: { label: 'General', icon: Utensils, color: '#3977ff' },
  SMOKING: { label: 'Smoking', icon: Cigarette, color: '#a2632b' },
  BAR: { label: 'Bar', icon: Wine, color: '#d44287' },
  OUTDOOR: { label: 'Outdoor', icon: Coffee, color: '#7a9f2e' },
  PRIVATE: { label: 'Private', icon: Moon, color: '#7d62ff' },
};

function App() {
  const [config, setConfig] = useState(null);
  const [floors, setFloors] = useState([]);
  const [tables, setTables] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [dashboard, setDashboard] = useState(null);
  const [selectedFloorId, setSelectedFloorId] = useState(null);
  const [selectedZone, setSelectedZone] = useState('ALL');
  const [selectedTable, setSelectedTable] = useState(null);
  const [message, setMessage] = useState('');
  const [welcomeBooking, setWelcomeBooking] = useState(null);
  const [adminOpen, setAdminOpen] = useState(false);
  const [adminUnlocked, setAdminUnlocked] = useState(localStorage.getItem('hotel-admin-auth') === 'true');
  const [adminId, setAdminId] = useState('admin');
  const [adminPassword, setAdminPassword] = useState('');
  const [seatToMoveId, setSeatToMoveId] = useState('');
  const [newFloorName, setNewFloorName] = useState('');
  const [newTable, setNewTable] = useState({ label: '', capacity: 4, zone: 'GENERAL' });
  const [layoutRequest, setLayoutRequest] = useState({ tableCount: 12, chairCount: 4 });
  const [bookingForm, setBookingForm] = useState({
    guestName: '',
    phone: '',
    partySize: 4,
    bookingDate: today,
    bookingTime: '19:00',
    preferredZone: 'FAMILY',
    paymentTransactionId: '',
    paymentScreenshotUrl: '',
  });

  const activeFloor = floors.find((floor) => floor.id === Number(selectedFloorId)) || floors[0];

  useEffect(() => {
    refreshCore();
  }, []);

  useEffect(() => {
    if (activeFloor?.id) {
      refreshFloor(activeFloor.id);
    }
  }, [activeFloor?.id, bookingForm.bookingDate]);

  useEffect(() => {
    if (config?.hotelName) {
      document.title = config.hotelName;
    }
  }, [config?.hotelName]);

  useEffect(() => {
    const lastBookingId = Number(localStorage.getItem('hotel-last-booking-id'));
    if (!lastBookingId) return;
    const verifiedBooking = bookings.find((booking) => booking.id === lastBookingId && booking.paymentVerified);
    if (verifiedBooking) {
      setWelcomeBooking(verifiedBooking);
    }
  }, [bookings]);

  async function api(path, options) {
    const response = await fetch(`${API_BASE}${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });
    if (!response.ok) {
      throw new Error(await response.text());
    }
    return response.json();
  }

  async function refreshCore() {
    try {
      const [configData, floorData, dashboardData] = await Promise.all([
        api('/config'),
        api('/floors'),
        api(`/dashboard?date=${today}`),
      ]);
      setConfig(configData);
      setFloors(floorData);
      setDashboard(dashboardData);
      if (!selectedFloorId && floorData.length > 0) {
        setSelectedFloorId(floorData[0].id);
      }
    } catch (error) {
      setMessage('Backend is not reachable. Start Spring Boot on port 8080.');
    }
  }

  async function refreshFloor(floorId) {
    const [tableData, bookingData] = await Promise.all([
      api(`/tables?floorId=${floorId}`),
      api(`/bookings?date=${bookingForm.bookingDate}`),
    ]);
    setTables(tableData);
    setBookings(bookingData);
  }

  const bookedTableIds = useMemo(
    () =>
      new Set(
        bookings
          .filter((booking) => ['PAYMENT_PENDING', 'RESERVED', 'SEATED', 'WAITLISTED'].includes(booking.status))
          .map((booking) => booking.diningTable?.id)
          .filter(Boolean),
      ),
    [bookings],
  );

  const bookingByTableId = useMemo(() => {
    const map = new Map();
    bookings
      .filter((booking) => booking.diningTable?.id)
      .forEach((booking) => map.set(booking.diningTable.id, booking));
    return map;
  }, [bookings]);

  const visibleTables = useMemo(() => {
    const activeTables = tables.filter((table) => table.status !== 'BLOCKED');
    if (selectedZone === 'ALL') return activeTables;
    if (selectedZone === 'BOOKED') return activeTables.filter((table) => bookedTableIds.has(table.id));
    return activeTables.filter((table) => table.zone === selectedZone);
  }, [tables, selectedZone, bookedTableIds]);

  function tableState(table) {
    if (selectedTable?.id === table.id) return 'selected';
    if (bookingByTableId.get(table.id)?.status === 'PAYMENT_PENDING') return 'pending';
    if (bookedTableIds.has(table.id)) return 'booked';
    return table.status?.toLowerCase() || 'available';
  }

  function coordinate(table, key) {
    if (key === 'x') {
      return table.xPercent ?? table.xpercent ?? 50;
    }
    return table.yPercent ?? table.ypercent ?? 50;
  }

  function tableSize(table, key) {
    if (key === 'width') {
      return table.displayWidth ?? 58;
    }
    return table.displayHeight ?? 48;
  }

  function selectTable(table) {
    if (table.status === 'BLOCKED') {
      setMessage('This table is disabled by admin. Please choose another seat.');
      return;
    }
    if (bookedTableIds.has(table.id)) {
      setMessage('This table is already booked for the selected date. Please choose another seat.');
      return;
    }
    setSelectedTable(table);
    setBookingForm((form) => ({
      ...form,
      preferredZone: table.zone,
      partySize: Math.min(Number(form.partySize) || 1, table.capacity),
    }));
    setMessage(`Selected ${table.label} on ${activeFloor?.name || 'this floor'}.`);
  }

  async function submitBooking(event) {
    event.preventDefault();
    if (!selectedTable) {
      setMessage('Please select a seat/table from the floor plan first.');
      return;
    }
    if (!bookingForm.paymentTransactionId || !bookingForm.paymentScreenshotUrl) {
      setMessage('Please enter transaction ID and upload payment screenshot for the INR 50 booking amount.');
      return;
    }
    const created = await api('/bookings', {
      method: 'POST',
      body: JSON.stringify({
        ...bookingForm,
        partySize: Number(bookingForm.partySize),
        source: 'ONLINE',
        requestedTableId: selectedTable.id,
      }),
    });
    setMessage(
      created.status === 'WAITLISTED'
        ? 'Payment proof received. Booking is waitlisted because this slot is very busy.'
        : `Payment proof received. ${selectedTable.label} is waiting for admin verification.`,
    );
    localStorage.setItem('hotel-last-booking-id', String(created.id));
    setWelcomeBooking(null);
    setSelectedTable(null);
    setBookingForm((form) => ({
      ...form,
      guestName: '',
      phone: '',
      paymentTransactionId: '',
      paymentScreenshotUrl: '',
    }));
    refreshFloor(activeFloor.id);
  }

  async function verifyPayment(id) {
    const verified = await api(`/bookings/${id}/verify-payment`, { method: 'POST' });
    setWelcomeBooking(verified);
    localStorage.setItem('hotel-last-booking-id', String(verified.id));
    setMessage('Payment verified. Welcome card is ready for the customer.');
    refreshFloor(activeFloor.id);
    refreshCore();
  }

  async function saveConfig(event) {
    event.preventDefault();
    const updated = await api('/config', {
      method: 'PUT',
      body: JSON.stringify(config),
    });
    setConfig(updated);
    setMessage('Admin settings saved.');
  }

  async function saveFloor(floor) {
    const updated = await api(`/floors/${floor.id}`, {
      method: 'PUT',
      body: JSON.stringify(floor),
    });
    setFloors((items) => items.map((item) => (item.id === updated.id ? updated : item)));
    setMessage(`${updated.name} floor plan saved.`);
  }

  async function createFloor() {
    if (!newFloorName.trim()) {
      setMessage('Enter a floor name first.');
      return;
    }
    const created = await api('/floors', {
      method: 'POST',
      body: JSON.stringify({ name: newFloorName.trim(), imageUrl: '', active: true }),
    });
    setFloors((items) => [...items, created]);
    setSelectedFloorId(created.id);
    setNewFloorName('');
    setMessage(`${created.name} added. Upload its floor plan image now.`);
  }

  async function moveSeatOnPlan(event) {
    if (!adminUnlocked || !seatToMoveId) {
      return;
    }
    const table = tables.find((item) => item.id === Number(seatToMoveId));
    if (!table) {
      setMessage('That table is no longer available. Floor tables refreshed.');
      refreshFloor(activeFloor.id);
      return;
    }
    const bounds = event.currentTarget.getBoundingClientRect();
    const xPercent = Math.round(((event.clientX - bounds.left) / bounds.width) * 100);
    const yPercent = Math.round(((event.clientY - bounds.top) / bounds.height) * 100);
    try {
      const updated = await api(`/tables/${table.id}`, {
        method: 'PUT',
        body: JSON.stringify({
          label: table.label,
          capacity: table.capacity,
          zone: table.zone,
          status: table.status,
          floorPlan: { id: activeFloor.id },
          xPercent: Math.max(0, Math.min(100, xPercent)),
          yPercent: Math.max(0, Math.min(100, yPercent)),
          displayWidth: tableSize(table, 'width'),
          displayHeight: tableSize(table, 'height'),
          combinable: table.combinable,
        }),
      });
      setTables((items) => items.map((item) => (item.id === updated.id ? updated : item)));
      setMessage(`Moved ${updated.label}. Select another table or click again to adjust.`);
    } catch (error) {
      setSeatToMoveId('');
      setMessage('This table changed on the server. I refreshed the floor list; select the table again.');
      refreshFloor(activeFloor.id);
    }
  }

  async function addTable() {
    if (!activeFloor) return;
    const label = newTable.label.trim() || `T${tables.length + 1}`;
    const created = await api('/tables', {
      method: 'POST',
      body: JSON.stringify({
        label,
        capacity: Number(newTable.capacity),
        zone: newTable.zone,
        status: 'AVAILABLE',
        floorPlan: { id: activeFloor.id },
        xPercent: 50,
        yPercent: 50,
        displayWidth: 58,
        displayHeight: 48,
        combinable: true,
      }),
    });
    setTables((items) => [...items, created]);
    setSeatToMoveId(String(created.id));
    setNewTable({ label: '', capacity: 4, zone: 'GENERAL' });
    setMessage(`${created.label} added. Click exact position on the floor plan to place it.`);
  }

  async function removeTable(id) {
    if (!id) {
      setMessage('Select a table before removing.');
      return;
    }
    await api(`/tables/${id}`, { method: 'DELETE' });
    setTables((items) => items.map((table) => (table.id === Number(id) ? { ...table, status: 'BLOCKED' } : table)));
    setSeatToMoveId('');
    setMessage('Table removed from this floor.');
  }

  async function generateLayout() {
    if (!activeFloor) return;
    const generated = await api(`/floors/${activeFloor.id}/generate-layout`, {
      method: 'POST',
      body: JSON.stringify({
        tableCount: Number(layoutRequest.tableCount),
        chairCount: Number(layoutRequest.chairCount),
      }),
    });
    setTables(generated);
    setSelectedTable(null);
    setMessage('Default systematic table layout generated. You can still move each table to exact position.');
  }

  async function updateTableSize(id, field, value) {
    const table = tables.find((item) => item.id === Number(id));
    if (!table || !activeFloor) return;
    const nextValue = Math.max(field === 'displayWidth' ? 34 : 34, Math.min(field === 'displayWidth' ? 140 : 120, Number(value)));
    const updated = await api(`/tables/${table.id}`, {
      method: 'PUT',
      body: JSON.stringify({
        label: table.label,
        capacity: table.capacity,
        zone: table.zone,
        status: table.status,
        floorPlan: { id: activeFloor.id },
        xPercent: coordinate(table, 'x'),
        yPercent: coordinate(table, 'y'),
        displayWidth: field === 'displayWidth' ? nextValue : tableSize(table, 'width'),
        displayHeight: field === 'displayHeight' ? nextValue : tableSize(table, 'height'),
        combinable: table.combinable,
      }),
    });
    setTables((items) => items.map((item) => (item.id === updated.id ? updated : item)));
    setMessage(`Resized ${updated.label}.`);
  }

  return (
    <main>
      <header className="hero">
        <div>
          <span className="pill">Smart restaurant seating</span>
          <h1>{config?.hotelName || 'Hotel Seating Intelligence'}</h1>
          <p>Pick your seat, pay INR 50 by UPI, upload proof, and your booking is held.</p>
        </div>
        <button className="admin-trigger" type="button" onClick={() => setAdminOpen((value) => !value)}>
          <ShieldCheck size={18} />
          Admin setup
        </button>
      </header>

      {message && <div className="notice">{message}</div>}
      {welcomeBooking?.paymentVerified && (
        <WelcomeCard booking={welcomeBooking} config={config} onClose={() => setWelcomeBooking(null)} />
      )}

      <section className="booking-shell">
        <div className="plan-card">
          <div className="card-title">
            <div>
              <p className="eyebrow">Choose floor and seat</p>
              <h2>{activeFloor?.name || 'Floor plan'}</h2>
            </div>
            <FloorTabs floors={floors} selectedFloorId={activeFloor?.id} onSelect={setSelectedFloorId} />
          </div>
          <ZoneLegend selectedZone={selectedZone} onSelect={setSelectedZone} />
          <FloorPlan
            floor={activeFloor}
            tables={visibleTables}
            tableState={tableState}
            selectedTable={selectedTable}
            onSelect={selectTable}
            onMoveSeat={moveSeatOnPlan}
            movingSeat={Boolean(adminUnlocked && seatToMoveId)}
            coordinate={coordinate}
            tableSize={tableSize}
          />
        </div>

        <aside className="glass-panel">
          <BookingForm
            form={bookingForm}
            setForm={setBookingForm}
            selectedTable={selectedTable}
            config={config}
            onSubmit={submitBooking}
          />
        </aside>
      </section>

      {adminOpen && (
        <AdminPanel
          adminUnlocked={adminUnlocked}
          adminId={adminId}
          setAdminId={setAdminId}
          adminPassword={adminPassword}
          setAdminPassword={setAdminPassword}
          unlock={() => {
            if (adminId === 'admin' && adminPassword === 'password') {
              localStorage.setItem('hotel-admin-auth', 'true');
              setAdminUnlocked(true);
              setMessage('Admin setup unlocked.');
            } else {
              setMessage('Wrong admin login. Use admin / password.');
            }
          }}
          logout={() => {
            localStorage.removeItem('hotel-admin-auth');
            setAdminUnlocked(false);
            setAdminPassword('');
            setSeatToMoveId('');
            setMessage('Admin logged out.');
          }}
          config={config}
          setConfig={setConfig}
          saveConfig={saveConfig}
          floors={floors}
          activeFloor={activeFloor}
          setFloors={setFloors}
          saveFloor={saveFloor}
          newFloorName={newFloorName}
          setNewFloorName={setNewFloorName}
          createFloor={createFloor}
          tables={tables}
          newTable={newTable}
          setNewTable={setNewTable}
          addTable={addTable}
          removeTable={removeTable}
          layoutRequest={layoutRequest}
          setLayoutRequest={setLayoutRequest}
          generateLayout={generateLayout}
          bookings={bookings}
          dashboard={dashboard}
          verifyPayment={verifyPayment}
          seatToMoveId={seatToMoveId}
          setSeatToMoveId={setSeatToMoveId}
          updateTableSize={updateTableSize}
          setMessage={setMessage}
        />
      )}
    </main>
  );
}

function Metric({ icon: Icon, label, value }) {
  return (
    <article className="metric-card">
      <Icon size={20} />
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}

function FloorTabs({ floors, selectedFloorId, onSelect }) {
  return (
    <div className="floor-tabs">
      {floors.map((floor) => (
        <button
          key={floor.id}
          className={floor.id === selectedFloorId ? 'active' : ''}
          type="button"
          onClick={() => onSelect(floor.id)}
        >
          <Layers size={15} />
          {floor.name}
        </button>
      ))}
    </div>
  );
}

function WelcomeCard({ booking, config, onClose }) {
  const table = booking.diningTable;
  const zone = table?.zone || booking.preferredZone;

  return (
    <section className="welcome-card">
      <button type="button" className="welcome-close" onClick={onClose} aria-label="Close welcome card">
        x
      </button>
      <div className="welcome-topline">
        <span>Payment verified</span>
        <strong>Booking #{booking.id}</strong>
      </div>
      <div className="welcome-title">
        <CheckCircle2 size={34} />
        <div>
          <p className="eyebrow">Welcome to</p>
          <h2>{config?.hotelName || 'Hotel Seating Intelligence'}</h2>
        </div>
      </div>
      <p className="welcome-message">
        Dear {booking.guestName}, your table is confirmed. Please show this greeting card at reception when you arrive.
      </p>
      <div className="welcome-details">
        <span>Name</span>
        <strong>{booking.guestName}</strong>
        <span>Mobile</span>
        <strong>{booking.phone}</strong>
        <span>Table</span>
        <strong>{table?.label || 'Waitlist'} | {table?.capacity || booking.partySize} seats</strong>
        <span>Section</span>
        <strong>{zoneMeta[zone]?.label || zone}</strong>
        <span>Date and time</span>
        <strong>{booking.bookingDate} at {booking.bookingTime}</strong>
        <span>Guests</span>
        <strong>{booking.partySize}</strong>
        <span>Payment</span>
        <strong>INR {booking.depositAmountInr || config?.bookingDepositInr || 50} verified</strong>
        <span>Transaction ID</span>
        <strong>{booking.paymentTransactionId}</strong>
      </div>
      <div className="welcome-footer">
        <span>Refundable booking amount will be returned when the guest arrives at the hotel.</span>
        <button type="button" onClick={() => window.print()}>
          Print card
        </button>
      </div>
    </section>
  );
}

function ZoneLegend({ selectedZone, onSelect }) {
  return (
    <div className="legend">
      <button type="button" className={selectedZone === 'ALL' ? 'active' : ''} onClick={() => onSelect('ALL')}>
        All
      </button>
      {zones.map((zone) => {
        const Icon = zoneMeta[zone].icon;
        return (
          <button
            key={zone}
            type="button"
            className={selectedZone === zone ? 'active' : ''}
            style={{ '--zone-color': zoneMeta[zone].color }}
            onClick={() => onSelect(zone)}
          >
            <Icon size={14} />
            {zoneMeta[zone].label}
          </button>
        );
      })}
      <button
        type="button"
        className={`booked-sample ${selectedZone === 'BOOKED' ? 'active' : ''}`}
        onClick={() => onSelect('BOOKED')}
      >
        Booked
      </button>
    </div>
  );
}

function FloorPlan({ floor, tables, tableState, selectedTable, onSelect, onMoveSeat, movingSeat, coordinate, tableSize }) {
  return (
    <div className={`floor-stage ${movingSeat ? 'moving' : ''}`} onClick={onMoveSeat}>
      {floor?.imageUrl ? (
        <img className="floor-bg" src={floor.imageUrl} alt={`${floor.name} uploaded plan`} />
      ) : (
        <div className="floor-placeholder">
          <MapPinned size={42} />
          <strong>No floor image uploaded</strong>
          <span>Admin can upload a floor plan; seats will remain clickable above it.</span>
        </div>
      )}
      <div className="soft-grid" />
      {tables.map((table) => {
        const state = tableState(table);
        return (
          <button
            key={table.id}
            type="button"
            className={`seat ${state}`}
            style={{
              left: `${coordinate(table, 'x')}%`,
              top: `${coordinate(table, 'y')}%`,
              width: `${tableSize(table, 'width')}px`,
              height: `${tableSize(table, 'height')}px`,
              '--seat-color': zoneMeta[table.zone]?.color || '#3977ff',
            }}
            onClick={(event) => {
              event.stopPropagation();
              onSelect(table);
            }}
            title={`${table.label} | ${table.capacity} seats | ${zoneMeta[table.zone]?.label}`}
          >
            <span>{table.label}</span>
            <small>{table.capacity} seats</small>
          </button>
        );
      })}
      {selectedTable && <div className="selection-chip">Selected {selectedTable.label}</div>}
    </div>
  );
}

function BookingForm({ form, setForm, selectedTable, config, onSubmit }) {
  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function uploadPayment(file) {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => update('paymentScreenshotUrl', reader.result);
    reader.readAsDataURL(file);
  }

  return (
    <form className="booking-form" onSubmit={onSubmit}>
      <div className="form-heading">
        <p className="eyebrow">Reserve seat</p>
        <h2>{selectedTable ? `Table ${selectedTable.label}` : 'Select a seat'}</h2>
        {selectedTable && (
          <span>
            {selectedTable.capacity} seats | {zoneMeta[selectedTable.zone]?.label}
          </span>
        )}
      </div>

      <div className="two-col">
        <label>
          Date
          <input type="date" value={form.bookingDate} onChange={(event) => update('bookingDate', event.target.value)} />
        </label>
        <label>
          Time
          <input type="time" value={form.bookingTime} onChange={(event) => update('bookingTime', event.target.value)} />
        </label>
      </div>
      <div className="two-col">
        <label>
          Guests
          <input
            type="number"
            min="1"
            max={selectedTable?.capacity || 20}
            value={form.partySize}
            onChange={(event) => update('partySize', event.target.value)}
            required
          />
        </label>
        <label>
          Mobile
          <input value={form.phone} onChange={(event) => update('phone', event.target.value)} required />
        </label>
      </div>
      <label>
        Name
        <input value={form.guestName} onChange={(event) => update('guestName', event.target.value)} required />
      </label>

      <section className="payment-box">
        <div>
          <p className="eyebrow">Pay to reserve</p>
          <h3>INR {config?.bookingDepositInr || 50}</h3>
          <span>Refundable at hotel arrival.</span>
        </div>
        {config?.qrCodeImageUrl ? (
          <img src={config.qrCodeImageUrl} alt="UPI QR code" />
        ) : (
          <div className="qr-empty">
            <QrCode size={42} />
          </div>
        )}
        <strong>UPI: {config?.upiId || 'hotel@upi'}</strong>
      </section>

      <label>
        UPI transaction ID
        <input
          value={form.paymentTransactionId}
          onChange={(event) => update('paymentTransactionId', event.target.value)}
          required
        />
      </label>
      <label className="upload-control">
        Upload payment screenshot
        <input type="file" accept="image/*" onChange={(event) => uploadPayment(event.target.files?.[0])} />
        <span>{form.paymentScreenshotUrl ? 'Screenshot attached' : 'Attach screenshot after payment'}</span>
      </label>
      <button className="primary-button" type="submit">
        <CheckCircle2 size={18} />
        Confirm booking
      </button>
    </form>
  );
}

function AdminPanel(props) {
  const {
    adminUnlocked,
    adminId,
    setAdminId,
    adminPassword,
    setAdminPassword,
    unlock,
    logout,
    config,
    setConfig,
    saveConfig,
    floors,
    activeFloor,
    setFloors,
    saveFloor,
    newFloorName,
    setNewFloorName,
    createFloor,
    tables,
    newTable,
    setNewTable,
    addTable,
    removeTable,
    layoutRequest,
    setLayoutRequest,
    generateLayout,
    bookings,
    dashboard,
    verifyPayment,
    seatToMoveId,
    setSeatToMoveId,
    updateTableSize,
    setMessage,
  } = props;
  const selectedMoveTable = tables.find((table) => table.id === Number(seatToMoveId));

  function updateConfig(field, value) {
    setConfig((current) => ({ ...current, [field]: value }));
  }

  function updateFloor(field, value) {
    setFloors((items) => items.map((floor) => (floor.id === activeFloor.id ? { ...floor, [field]: value } : floor)));
  }

  function uploadImage(file, callback) {
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      setMessage('Please upload an image file.');
      return;
    }
    const reader = new FileReader();
    reader.onload = () => callback(reader.result);
    reader.readAsDataURL(file);
  }

  if (!adminUnlocked) {
    return (
      <section className="admin-panel locked">
        <LockKeyhole size={22} />
        <h2>Admin setup</h2>
        <p>Unlock to manage hotel name, floors, QR code, UPI ID, and seat placement.</p>
        <div className="inline-login">
          <input
            placeholder="Admin ID"
            value={adminId}
            onChange={(event) => setAdminId(event.target.value)}
          />
          <input
            type="password"
            placeholder="Admin password"
            value={adminPassword}
            onChange={(event) => setAdminPassword(event.target.value)}
          />
          <button type="button" onClick={unlock}>
            Unlock
          </button>
        </div>
      </section>
    );
  }

  return (
    <section className="admin-panel">
      <div className="card-title">
        <div>
          <p className="eyebrow">Admin setup</p>
          <h2>Brand, floors, payment and seat layout</h2>
        </div>
        <button className="secondary-button" type="button" onClick={logout}>
          <LockKeyhole size={17} />
          Admin logout
        </button>
      </div>

      <form className="admin-grid" onSubmit={saveConfig}>
        <label>
          Hotel / app name
          <input value={config?.hotelName || ''} onChange={(event) => updateConfig('hotelName', event.target.value)} />
        </label>
        <label>
          UPI ID
          <input value={config?.upiId || ''} onChange={(event) => updateConfig('upiId', event.target.value)} />
        </label>
        <label className="upload-control">
          Upload UPI QR code
          <input
            type="file"
            accept="image/*"
            onChange={(event) => uploadImage(event.target.files?.[0], (value) => updateConfig('qrCodeImageUrl', value))}
          />
          <span>{config?.qrCodeImageUrl ? 'QR code attached' : 'Upload QR for customer payment'}</span>
        </label>
        <label>
          Booking fee INR
          <input
            type="number"
            value={config?.bookingDepositInr || 50}
            onChange={(event) => updateConfig('bookingDepositInr', Number(event.target.value))}
          />
        </label>
        <button className="secondary-button" type="submit">
          <Save size={17} />
          Save hotel settings
        </button>
      </form>

      <div className="floor-admin">
        <div className="inline-login">
          <input
            placeholder="New floor name"
            value={newFloorName}
            onChange={(event) => setNewFloorName(event.target.value)}
          />
          <button type="button" onClick={createFloor}>
            <Plus size={17} />
            Add floor
          </button>
        </div>
        <div className="floor-admin-list">
          {floors.map((floor) => (
            <span key={floor.id} className={floor.id === activeFloor?.id ? 'active' : ''}>
              {floor.name}
            </span>
          ))}
        </div>
        {activeFloor && (
          <div className="admin-grid">
            <label>
              Current floor name
              <input value={activeFloor.name} onChange={(event) => updateFloor('name', event.target.value)} />
            </label>
            <label className="upload-control">
              Upload plan for this floor
              <input
                type="file"
                accept="image/*"
                onChange={(event) => uploadImage(event.target.files?.[0], (value) => updateFloor('imageUrl', value))}
              />
              <span>{activeFloor.imageUrl ? 'Floor image attached' : 'Upload this floor plan'}</span>
            </label>
            <button className="secondary-button" type="button" onClick={() => saveFloor(activeFloor)}>
              <ImageUp size={17} />
              Save floor plan
            </button>
          </div>
        )}
      </div>

      <div className="seat-mover">
        <p className="eyebrow">Seat placement</p>
        <h3>Tables and exact placement</h3>
        <p className="floor-context">
          Editing floor: <strong>{activeFloor?.name || 'No floor selected'}</strong>
        </p>
        <p>Add/remove tables, select one, then click its real position on the floor plan above.</p>
        <div className="table-admin-grid">
          <input
            placeholder="Table label"
            value={newTable.label}
            onChange={(event) => setNewTable((current) => ({ ...current, label: event.target.value }))}
          />
          <input
            type="number"
            min="1"
            placeholder="Chairs"
            value={newTable.capacity}
            onChange={(event) => setNewTable((current) => ({ ...current, capacity: event.target.value }))}
          />
          <select
            value={newTable.zone}
            onChange={(event) => setNewTable((current) => ({ ...current, zone: event.target.value }))}
          >
            {zones.map((zone) => (
              <option key={zone} value={zone}>
                {zoneMeta[zone].label}
              </option>
            ))}
          </select>
          <button type="button" onClick={addTable}>
            <Plus size={16} />
            Add table
          </button>
        </div>
        <select value={seatToMoveId} onChange={(event) => setSeatToMoveId(event.target.value)}>
          <option value="">Select table to move</option>
          {tables
            .filter((table) => table.status !== 'BLOCKED')
            .map((table) => (
            <option key={table.id} value={table.id}>
              {table.label} | {table.capacity} seats | {zoneMeta[table.zone]?.label}
            </option>
          ))}
        </select>
        {selectedMoveTable && (
          <div className="resize-tools">
            <label>
              Table width
              <input
                type="range"
                min="34"
                max="140"
                value={selectedMoveTable.displayWidth ?? 58}
                onChange={(event) => updateTableSize(selectedMoveTable.id, 'displayWidth', event.target.value)}
              />
              <span>{selectedMoveTable.displayWidth ?? 58}px</span>
            </label>
            <label>
              Table height
              <input
                type="range"
                min="34"
                max="120"
                value={selectedMoveTable.displayHeight ?? 48}
                onChange={(event) => updateTableSize(selectedMoveTable.id, 'displayHeight', event.target.value)}
              />
              <span>{selectedMoveTable.displayHeight ?? 48}px</span>
            </label>
          </div>
        )}
        <button type="button" className="danger-button" onClick={() => removeTable(seatToMoveId)}>
          <Minus size={16} />
          Remove selected table
        </button>
      </div>

      <div className="seat-mover">
        <p className="eyebrow">Default layout</p>
        <h3>No floor plan? Generate seating automatically</h3>
        <p className="floor-context">
          Layout will be created for: <strong>{activeFloor?.name || 'No floor selected'}</strong>
        </p>
        <p>Enter table count and chairs per table. The app creates a clean grid arrangement customers can book immediately.</p>
        <div className="table-admin-grid compact">
          <input
            type="number"
            min="1"
            value={layoutRequest.tableCount}
            onChange={(event) => setLayoutRequest((current) => ({ ...current, tableCount: event.target.value }))}
            aria-label="Table count"
          />
          <input
            type="number"
            min="1"
            value={layoutRequest.chairCount}
            onChange={(event) => setLayoutRequest((current) => ({ ...current, chairCount: event.target.value }))}
            aria-label="Chair count"
          />
          <button type="button" onClick={generateLayout}>
            Generate layout for {activeFloor?.name || 'floor'}
          </button>
        </div>
      </div>

      <AdminDashboard bookings={bookings} dashboard={dashboard} verifyPayment={verifyPayment} />
    </section>
  );
}

function AdminDashboard({ bookings, dashboard, verifyPayment }) {
  const pendingPayments = bookings.filter((booking) => booking.status === 'PAYMENT_PENDING');
  const confirmed = bookings.filter((booking) => ['RESERVED', 'SEATED'].includes(booking.status));
  const peakSlot = [...(dashboard?.slots || [])].sort((a, b) => b.utilizationPercent - a.utilizationPercent)[0];
  const vacantSlots = (dashboard?.slots || []).filter((slot) => slot.offerRecommended);
  const pendingAmount = pendingPayments.reduce((sum, booking) => sum + (booking.depositAmountInr || 0), 0);
  const zoneEntries = Object.entries(dashboard?.zoneMix || {});

  return (
    <div className="admin-dashboard">
      <div className="admin-metrics">
        <Metric icon={Sofa} label="Total seats" value={dashboard?.totalSeats ?? '--'} />
        <Metric icon={ClipboardList} label="Booked seats" value={dashboard?.todayReservedSeats ?? '--'} />
        <Metric icon={BarChart3} label="Predicted use" value={`${dashboard?.predictedUtilizationPercent ?? '--'}%`} />
        <Metric icon={CreditCard} label="Verify payments" value={pendingPayments.length} />
      </div>

      <div className="decision-grid">
        <article>
          <span>Peak pressure</span>
          <strong>{peakSlot ? `${peakSlot.time} | ${peakSlot.utilizationPercent}%` : '--'}</strong>
        </article>
        <article>
          <span>Offer windows</span>
          <strong>{vacantSlots.length ? vacantSlots.map((slot) => slot.time).join(', ') : 'None'}</strong>
        </article>
        <article>
          <span>Pending deposit</span>
          <strong>INR {pendingAmount}</strong>
        </article>
        <article>
          <span>Waitlist</span>
          <strong>{dashboard?.waitlistedBookings ?? 0}</strong>
        </article>
      </div>

      <div className="queue-card">
        <p className="eyebrow">Queueing theory and prediction</p>
        <h3>Optimization signal</h3>
        <p>
          Queue pressure, utilization, overbooking risk, and off-peak offer suggestions are calculated from booking load,
          seat capacity, and previous booking history.
        </p>
        <div className="slot-list">
          {(dashboard?.slots || []).map((slot) => (
            <span key={slot.time}>
              {slot.time}: {slot.utilizationPercent}% use | pressure {slot.queuePressure}
            </span>
          ))}
        </div>
        <div className="recommendation-list">
          {(dashboard?.recommendations || []).map((item) => (
            <p key={item}>{item}</p>
          ))}
        </div>
        <div className="zone-demand">
          {zoneEntries.map(([zone, count]) => (
            <span key={zone}>
              {zoneMeta[zone]?.label || zone}: {count}
            </span>
          ))}
        </div>
      </div>

      <div className="payment-review">
        <p className="eyebrow">Payment verification</p>
        <h3>Pending customer proofs</h3>
        {pendingPayments.length === 0 && <p>No pending payment proofs.</p>}
        {pendingPayments.map((booking) => (
          <article key={booking.id} className="booking-review">
            <div>
              <strong>{booking.diningTable?.label || 'Waitlist'} | {booking.guestName}</strong>
              <span>{booking.bookingTime} | {booking.partySize} guests | TXN {booking.paymentTransactionId}</span>
            </div>
            {booking.paymentScreenshotUrl && <img src={booking.paymentScreenshotUrl} alt="Payment screenshot" />}
            <button type="button" onClick={() => verifyPayment(booking.id)}>
              Verify payment
            </button>
          </article>
        ))}
      </div>

      <div className="payment-review">
        <p className="eyebrow">Confirmed seats</p>
        <h3>Today booking status</h3>
        {confirmed.map((booking) => (
          <article key={booking.id} className="booking-review compact-review">
            <div>
              <strong>{booking.diningTable?.label} | {booking.guestName}</strong>
              <span>{booking.status} | {booking.phone}</span>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}

createRoot(document.getElementById('root')).render(<App />);
