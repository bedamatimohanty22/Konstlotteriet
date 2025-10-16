import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { Login } from './components/Login';
import { Dashboard } from './components/Dashboard';
import { PrivateRoute } from './components/PrivateRoute';
import { Members } from './pages/admin/Members';
import { Artworks } from './pages/admin/Artworks';
import { Lotteries } from './pages/admin/Lotteries';
import { Profile } from './pages/member/Profile';
import { Rankings } from './pages/member/Rankings';
import { RankingEditor } from './pages/member/RankingEditor';

import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/dashboard"
            element={
              <PrivateRoute>
                <Dashboard />
              </PrivateRoute>
            }
          />
          <Route
            path="/admin/members"
            element={
              <PrivateRoute requireAdmin>
                <Members />
              </PrivateRoute>
            }
          />
          <Route
            path="/admin/artworks"
            element={
              <PrivateRoute requireAdmin>
                <Artworks />
              </PrivateRoute>
            }
          />
          <Route
            path="/admin/lotteries"
            element={
              <PrivateRoute requireAdmin>
                <Lotteries />
              </PrivateRoute>
            }
          />
          <Route
            path="/member/profile"
            element={
              <PrivateRoute>
                <Profile />
              </PrivateRoute>
            }
          />
          <Route
            path="/member/rankings"
            element={
              <PrivateRoute>
                <Rankings />
              </PrivateRoute>
            }
          />
          <Route
            path="/member/rankings/edit/:lotteryId"
            element={
              <PrivateRoute>
                <RankingEditor />
              </PrivateRoute>
            }
          />
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
