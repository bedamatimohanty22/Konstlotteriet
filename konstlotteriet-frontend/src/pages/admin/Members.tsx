import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../services/api';
import type { Member } from '../../types';

export const Members: React.FC = () => {
  const [members, setMembers] = useState<Member[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [showUserModal, setShowUserModal] = useState(false);
  const [selectedMember, setSelectedMember] = useState<Member | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    loadMembers();
  }, []);

  const loadMembers = async () => {
    try {
      const data = await api.getAllMembers();
      setMembers(data);
    } catch (err) {
      setError('Kunde inte ladda medlemmar');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Är du säker på att du vill deaktivera denna medlem?')) {
      return;
    }

    try {
      await api.deactivateMember(id);
      await loadMembers();
    } catch (err) {
      alert('Kunde inte deaktivera medlem');
      console.error(err);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-lg">Laddar...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <button
                onClick={() => navigate('/dashboard')}
                className="text-indigo-600 hover:text-indigo-800"
              >
                ← Tillbaka
              </button>
              <h1 className="ml-4 text-xl font-bold text-gray-900">Medlemmar</h1>
            </div>
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        {error && (
          <div className="mb-4 bg-red-50 p-4 rounded-md">
            <p className="text-red-800">{error}</p>
          </div>
        )}

        <div className="mb-4">
          <button
            onClick={() => setShowForm(true)}
            className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
          >
            + Lägg till medlem
          </button>
        </div>

        {showForm && (
          <MemberForm
            onSuccess={() => {
              setShowForm(false);
              loadMembers();
            }}
            onCancel={() => setShowForm(false)}
          />
        )}

        {showUserModal && selectedMember && (
          <UserAccountModal
            member={selectedMember}
            onSuccess={() => {
              setShowUserModal(false);
              setSelectedMember(null);
            }}
            onCancel={() => {
              setShowUserModal(false);
              setSelectedMember(null);
            }}
          />
        )}

        <div className="bg-white shadow overflow-hidden sm:rounded-md">
          <ul className="divide-y divide-gray-200">
            {members.map((member) => (
              <li key={member.id}>
                <div className="px-4 py-4 sm:px-6 hover:bg-gray-50">
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <p className="text-sm font-medium text-indigo-600">
                        {member.firstName} {member.lastName}
                      </p>
                      <div className="mt-2 flex items-center text-sm text-gray-500">
                        <span className="truncate">{member.email}</span>
                        <span className="mx-2">•</span>
                        <span>{member.phoneNumber}</span>
                        <span className="mx-2">•</span>
                        <span>Medlem sedan {member.membershipStartYear}</span>
                        {member.lastWinYear && (
                          <>
                            <span className="mx-2">•</span>
                            <span className="text-green-600">Vann {member.lastWinYear}</span>
                          </>
                        )}
                      </div>
                    </div>
                    <div className="flex space-x-2">
                      <span
                        className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          member.active
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}
                      >
                        {member.active ? 'Aktiv' : 'Inaktiv'}
                      </span>
                      <button
                        onClick={() => {
                          setSelectedMember(member);
                          setShowUserModal(true);
                        }}
                        className="text-indigo-600 hover:text-indigo-800 text-sm"
                      >
                        Skapa inloggning
                      </button>
                      {member.active && (
                        <button
                          onClick={() => handleDelete(member.id!)}
                          className="text-red-600 hover:text-red-800 text-sm"
                        >
                          Deaktivera
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </div>

        {members.length === 0 && (
          <div className="text-center py-12 bg-white rounded-lg shadow">
            <p className="text-gray-500">Inga medlemmar hittades</p>
          </div>
        )}
      </div>
    </div>
  );
};

interface MemberFormProps {
  onSuccess: () => void;
  onCancel: () => void;
}

const MemberForm: React.FC<MemberFormProps> = ({ onSuccess, onCancel }) => {
  const [formData, setFormData] = useState<Member>({
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    membershipStartYear: new Date().getFullYear(),
    active: true,
  });
  const [password, setPassword] = useState('');
  const [createUserAccount, setCreateUserAccount] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      // First create the member
      const newMember = await api.createMember(formData);

      // Then create user account if requested
      if (createUserAccount && password) {
        try {
          await api.register({
            email: formData.email,
            password: password,
            role: 'MEMBER',
            memberId: newMember.id!
          });
        } catch (userErr) {
          alert('Medlem skapad men kunde inte skapa inloggning: ' + userErr);
          console.error(userErr);
        }
      }

      onSuccess();
    } catch (err) {
      alert('Kunde inte skapa medlem');
      console.error(err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mb-6 bg-white shadow sm:rounded-lg">
      <div className="px-4 py-5 sm:p-6">
        <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">
          Lägg till ny medlem
        </h3>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Förnamn
              </label>
              <input
                type="text"
                required
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.firstName}
                onChange={(e) =>
                  setFormData({ ...formData, firstName: e.target.value })
                }
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Efternamn
              </label>
              <input
                type="text"
                required
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.lastName}
                onChange={(e) =>
                  setFormData({ ...formData, lastName: e.target.value })
                }
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                E-post
              </label>
              <input
                type="email"
                required
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.email}
                onChange={(e) =>
                  setFormData({ ...formData, email: e.target.value })
                }
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Telefonnummer
              </label>
              <input
                type="tel"
                required
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.phoneNumber}
                onChange={(e) =>
                  setFormData({ ...formData, phoneNumber: e.target.value })
                }
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Medlem sedan år
              </label>
              <input
                type="number"
                required
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.membershipStartYear}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    membershipStartYear: parseInt(e.target.value),
                  })
                }
              />
            </div>
          </div>

          <div className="border-t border-gray-200 pt-4">
            <div className="flex items-center mb-4">
              <input
                type="checkbox"
                id="createUserAccount"
                checked={createUserAccount}
                onChange={(e) => setCreateUserAccount(e.target.checked)}
                className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
              />
              <label htmlFor="createUserAccount" className="ml-2 block text-sm text-gray-900">
                Skapa inloggningskonto för medlemmen
              </label>
            </div>

            {createUserAccount && (
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Lösenord
                </label>
                <input
                  type="password"
                  required={createUserAccount}
                  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Minst 6 tecken"
                  minLength={6}
                />
                <p className="mt-1 text-sm text-gray-500">
                  Medlemmen kommer kunna logga in med sin e-postadress och detta lösenord
                </p>
              </div>
            )}
          </div>

          <div className="flex justify-end space-x-3">
            <button
              type="button"
              onClick={onCancel}
              className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
            >
              Avbryt
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
            >
              {submitting ? 'Sparar...' : 'Spara'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

interface UserAccountModalProps {
  member: Member;
  onSuccess: () => void;
  onCancel: () => void;
}

const UserAccountModal: React.FC<UserAccountModalProps> = ({ member, onSuccess, onCancel }) => {
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      await api.register({
        email: member.email,
        password: password,
        role: 'MEMBER',
        memberId: member.id!
      });
      alert('Inloggningskonto skapat! Användaren kan nu logga in med sin e-postadress och lösenord.');
      onSuccess();
    } catch (err: any) {
      const errorMessage = err.message || 'Kunde inte skapa inloggning';
      alert(errorMessage);
      console.error(err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full">
        <div className="px-4 py-5 sm:p-6">
          <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">
            Skapa inloggning för {member.firstName} {member.lastName}
          </h3>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">
                E-postadress
              </label>
              <input
                type="email"
                disabled
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 bg-gray-100 text-gray-500 sm:text-sm"
                value={member.email}
              />
              <p className="mt-1 text-sm text-gray-500">
                Medlemmen kommer logga in med denna e-postadress
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">
                Lösenord
              </label>
              <input
                type="password"
                required
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Minst 6 tecken"
                minLength={6}
                autoFocus
              />
            </div>

            <div className="bg-blue-50 p-3 rounded-md">
              <p className="text-sm text-blue-800">
                Ett inloggningskonto med rollen MEMBER kommer att skapas. Användaren kan sedan logga in på systemet.
              </p>
            </div>

            <div className="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                onClick={onCancel}
                className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
              >
                Avbryt
              </button>
              <button
                type="submit"
                disabled={submitting}
                className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
              >
                {submitting ? 'Skapar...' : 'Skapa inloggning'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
