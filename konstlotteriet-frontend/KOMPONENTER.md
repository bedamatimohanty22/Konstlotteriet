# Konstlotteriet Frontend - Komponentöversikt

## 🎯 Skapade Komponenter

### 🔐 **Authentication**

#### `Login.tsx`
- Inloggningsformulär med email och lösenord
- Error handling med svenska felmeddelanden
- Automatisk redirect till dashboard efter lyckad inloggning
- Visar standard admin credentials för testning

#### `AuthContext.tsx`
- React Context för autentiseringshantering
- Hooks: `useAuth()` för att komma åt user, login, logout
- Persistent state via localStorage
- Role checks: `isAdmin`, `isMember`

#### `PrivateRoute.tsx`
- Route guard komponent
- Skyddar routes baserat på autentisering
- Optional `requireAdmin` prop för admin-endast routes
- Automatisk redirect till /login om ej autentiserad

---

### 📊 **Admin Components**

#### `Dashboard.tsx`
- Huvuddashboard med navigation cards
- Dynamisk innehåll baserat på användarroll
- Admin ser: Medlemmar, Konstverk, Utlottningar
- Members ser: Min Profil, Rankinglistor

#### `Members.tsx` (`/admin/members`)
**Funktioner:**
- ✅ Lista alla medlemmar med komplett information
- ✅ Sök och filtrera (visar medlemsår och senaste vinst)
- ✅ Lägg till ny medlem via inline-formulär
- ✅ Deaktivera medlem (soft delete)
- ✅ Visar aktiv/inaktiv status med färgkodning
- ✅ Responsive grid layout

**Formulär inkluderar:**
- Förnamn, Efternamn
- E-post, Telefonnummer
- Medlem sedan år

#### `Artworks.tsx` (`/admin/artworks`)
**Funktioner:**
- ✅ Grid-vy med alla konstverk
- ✅ Filter per utlottning (dropdown)
- ✅ Lägg till nytt konstverk
- ✅ Visar: Titel, Konstnär, År, Värde, Beskrivning
- ✅ YearId visas som badge (t.ex. "2024-1")
- ✅ Ta bort konstverk

**Formulär inkluderar:**
- Titel, Konstnär, År
- Uppskattat värde (kr)
- Koppling till utlottning (optional)
- Beskrivning (textarea)

#### `Lotteries.tsx` (`/admin/lotteries`)
**Funktioner:**
- ✅ Grid-vy med alla utlottningar
- ✅ Status badges (Planerad/Aktiv/Avslutad) med färgkodning
- ✅ Skapa ny utlottning
- ✅ Hantera deltagare via modal
- ✅ Lägg till medlemmar som deltagare
- ✅ Ta bort planerade utlottningar
- ✅ Sorterad efter år (senaste först)

**Status färger:**
- 🟡 PLANNED - Gul
- 🟢 ACTIVE - Grön
- ⚫ COMPLETED - Grå

**Deltagare Modal:**
- Välj medlem från dropdown
- Automatisk beräkning av antal lotter (backend)
- Feedback vid tillägg

---

### 👤 **Member Portal Components**

#### `Profile.tsx` (`/member/profile`)
**Funktioner:**
- ✅ Visa fullständig medlemsprofil
- ✅ Personlig information (namn, email, telefon)
- ✅ Medlemskapsinformation (medlem sedan)
- ✅ Senaste vinst (om applicerbart)
- ✅ Status badge (Aktiv/Inaktiv)
- ✅ Information om lottery tickets regler
- ✅ Länk till rankinglistor

**Information visar:**
- Namn
- E-postadress
- Telefonnummer
- Medlem sedan (år)
- Senaste vinst (grön highlight om vunnet)
- Aktiv status

#### `Rankings.tsx` (`/member/rankings`)
**Funktioner:**
- ✅ Lista alla aktiva utlottningar
- ✅ Status per utlottning (lista skapad/ej skapad)
- ✅ Information om rankinglistor
- ✅ Framtida funktionalitet: Skapa/redigera listor
- ✅ Instruktioner för medlemmar
- ✅ Visar dragningsdatum per utlottning

**Features:**
- Filtrera ut avslutade utlottningar
- Visar antal konstverk i lista (om skapad)
- Placeholder för "Skapa rankinglista"-funktion
- Tips och instruktioner

---

## 🎨 **Styling & Design**

### Tailwind CSS
- Utility-first approach
- Responsive breakpoints: `sm:`, `md:`, `lg:`
- Färgschema: Indigo för primär, grå för neutral

### Färgkoder i systemet

```css
/* Status Badges */
.active → bg-green-100 text-green-800
.inactive → bg-red-100 text-red-800
.planned → bg-yellow-100 text-yellow-800
.active-lottery → bg-green-100 text-green-800
.completed → bg-gray-100 text-gray-800

/* Buttons */
.primary → bg-indigo-600 hover:bg-indigo-700
.danger → text-red-600 hover:text-red-800
.link → text-indigo-600 hover:text-indigo-800
```

### Layout Patterns

**Card Layout:**
```tsx
<div className="bg-white shadow overflow-hidden sm:rounded-lg">
  // Content
</div>
```

**Grid Layout:**
```tsx
<div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
  // Cards
</div>
```

---

## 🔄 **Data Flow**

### API Integration
Alla komponenter använder `api.ts` service:

```typescript
// Hämta data
const members = await api.getAllMembers();

// Skapa data
const newMember = await api.createMember(data);

// Ta bort data
await api.deactivateMember(id);
```

### State Management
- **Local State**: `useState` för form data och UI state
- **Global State**: `AuthContext` för user/auth state
- **No Redux**: Håller det enkelt med Context API

---

## 📋 **Routes Översikt**

| Route | Component | Require Auth | Require Admin | Beskrivning |
|-------|-----------|--------------|---------------|-------------|
| `/login` | Login | ❌ | ❌ | Inloggningssida |
| `/dashboard` | Dashboard | ✅ | ❌ | Huvuddashboard |
| `/admin/members` | Members | ✅ | ✅ | Medlemshantering |
| `/admin/artworks` | Artworks | ✅ | ✅ | Konstverkshantering |
| `/admin/lotteries` | Lotteries | ✅ | ✅ | Utlottningshantering |
| `/member/profile` | Profile | ✅ | ❌ | Medlemsprofil |
| `/member/rankings` | Rankings | ✅ | ❌ | Rankinglistor |

---

## ⚙️ **Configuration**

### Environment Variables

`.env` fil:
```
VITE_API_URL=http://localhost:8080/api
```

### Tailwind Config

`tailwind.config.js`:
```javascript
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
```

---

## 🚀 **Användning**

### Starta Utvecklingsserver

```bash
npm run dev
```

Öppna: `http://localhost:5173`

### Login Credentials

**Admin:**
- Email: `admin@konstlotteriet.se`
- Password: `admin123`

### Test Workflow

1. **Logga in** som admin
2. **Skapa medlemmar** via Members-sidan
3. **Lägg till konstverk** via Artworks-sidan
4. **Skapa utlottning** via Lotteries-sidan
5. **Lägg till deltagare** i utlottning
6. **Logga in som medlem** (efter att ha skapat user för medlem)
7. **Visa profil** och hantera rankinglistor

---

## 🔮 **Framtida Förbättringar**

### Prioritet 1 - Funktionalitet
- [ ] Fullständig ranking list editor (drag & drop)
- [ ] Lottery drawing algorithm implementation
- [ ] Winner selection och notification
- [ ] Export funktionalitet (PDF rapporter)
- [ ] Email integration för notifications

### Prioritet 2 - UX
- [ ] Loading skeletons istället för "Laddar..."
- [ ] Toast notifications (react-hot-toast)
- [ ] Confirmation modals (sweetalert2)
- [ ] Pagination för stora listor
- [ ] Advanced search och filter

### Prioritet 3 - Tech Debt
- [ ] React Query för data fetching och caching
- [ ] Form validation med Zod + React Hook Form
- [ ] Error boundaries
- [ ] Unit tests med Vitest
- [ ] E2E tests med Playwright

### Prioritet 4 - Polish
- [ ] Dark mode toggle
- [ ] Animations (framer-motion)
- [ ] Image upload för konstverk
- [ ] PDF generation för lotteries
- [ ] Analytics dashboard

---

## 📦 **Dependencies**

```json
{
  "dependencies": {
    "react": "^18.x",
    "react-dom": "^18.x",
    "react-router-dom": "^6.x",
    "@tanstack/react-query": "^5.x",
    "axios": "^1.x"
  },
  "devDependencies": {
    "typescript": "^5.x",
    "vite": "^6.x",
    "tailwindcss": "^3.x",
    "postcss": "^8.x",
    "autoprefixer": "^10.x"
  }
}
```

---

## 🎓 **Kodexempel**

### Skapa ny komponent

```typescript
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../services/api';

export const MyComponent: React.FC = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const result = await api.getData();
      setData(result);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Content */}
    </div>
  );
};
```

---

**Skapad**: 2025-10-10
**Version**: 1.0.0
**Utvecklare**: Claude Code
