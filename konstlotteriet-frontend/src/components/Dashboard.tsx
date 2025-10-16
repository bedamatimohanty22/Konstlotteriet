import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const Dashboard: React.FC = () => {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <h1 className="text-xl font-bold text-gray-900">Konstlotteriet</h1>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-gray-700">
                {user?.email} ({user?.role})
              </span>
              <button
                onClick={handleLogout}
                className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 rounded-md"
              >
                Logga ut
              </button>
            </div>
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">Dashboard</h2>

          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {isAdmin && (
              <>
                <DashboardCard
                  title="Medlemmar"
                  description="Hantera medlemmar och användare"
                  onClick={() => navigate('/admin/members')}
                  icon="👥"
                />
                <DashboardCard
                  title="Konstverk"
                  description="Administrera konstverksbibliotek"
                  onClick={() => navigate('/admin/artworks')}
                  icon="🎨"
                />
                <DashboardCard
                  title="Utlottningar"
                  description="Skapa och hantera utlottningar"
                  onClick={() => navigate('/admin/lotteries')}
                  icon="🎲"
                />
              </>
            )}

            {!isAdmin && (
              <>
                <DashboardCard
                  title="Min Profil"
                  description="Visa och uppdatera din profil"
                  onClick={() => navigate('/member/profile')}
                  icon="👤"
                />
                <DashboardCard
                  title="Rankinglistor"
                  description="Hantera dina konstverksönskemål"
                  onClick={() => navigate('/member/rankings')}
                  icon="⭐"
                />
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

interface DashboardCardProps {
  title: string;
  description: string;
  onClick: () => void;
  icon: string;
}

const DashboardCard: React.FC<DashboardCardProps> = ({
  title,
  description,
  onClick,
  icon,
}) => {
  return (
    <button
      onClick={onClick}
      className="bg-white overflow-hidden shadow rounded-lg hover:shadow-md transition-shadow text-left"
    >
      <div className="p-5">
        <div className="flex items-center">
          <div className="flex-shrink-0">
            <span className="text-4xl">{icon}</span>
          </div>
          <div className="ml-5 w-0 flex-1">
            <dl>
              <dt className="text-sm font-medium text-gray-500 truncate">{title}</dt>
              <dd className="mt-1 text-sm text-gray-900">{description}</dd>
            </dl>
          </div>
        </div>
      </div>
    </button>
  );
};
