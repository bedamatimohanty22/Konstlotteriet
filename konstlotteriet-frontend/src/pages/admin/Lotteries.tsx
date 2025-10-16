import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, type DrawResult } from '../../services/api';
import type { Lottery, Member } from '../../types';

export const Lotteries: React.FC = () => {
  const [lotteries, setLotteries] = useState<Lottery[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [selectedLottery, setSelectedLottery] = useState<Lottery | null>(null);
  const [drawResult, setDrawResult] = useState<DrawResult | null>(null);
  const [performing, setPerforming] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    loadLotteries();
  }, []);

  const loadLotteries = async () => {
    try {
      const data = await api.getAllLotteries();
      setLotteries(data.sort((a, b) => b.year - a.year));
    } catch (err) {
      setError('Kunde inte ladda utlottningar');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Är du säker på att du vill ta bort denna utlottning?')) {
      return;
    }

    try {
      await api.deleteLottery(id);
      await loadLotteries();
    } catch (err) {
      alert('Kunde inte ta bort utlottning');
      console.error(err);
    }
  };

  const handlePerformDraw = async (lottery: Lottery) => {
    if (!window.confirm(
      `Är du säker på att du vill genomföra dragningen för ${lottery.year}?\n\n` +
      'Detta kan inte ångras och kommer att:\n' +
      '- Tilldela vinnare till alla konstverk\n' +
      '- Markera utlottningen som avslutad\n' +
      '- Uppdatera medlemmarnas senaste vinst'
    )) {
      return;
    }

    try {
      setPerforming(true);
      setError('');
      const result = await api.performDraw(lottery.id!);
      setDrawResult(result);
      await loadLotteries();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Kunde inte genomföra dragning';
      setError(errorMessage);
      alert(errorMessage);
      console.error(err);
    } finally {
      setPerforming(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PLANNED':
        return 'bg-yellow-100 text-yellow-800';
      case 'ACTIVE':
        return 'bg-green-100 text-green-800';
      case 'COMPLETED':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'PLANNED':
        return 'Planerad';
      case 'ACTIVE':
        return 'Aktiv';
      case 'COMPLETED':
        return 'Avslutad';
      default:
        return status;
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
              <h1 className="ml-4 text-xl font-bold text-gray-900">Utlottningar</h1>
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
            + Skapa utlottning
          </button>
        </div>

        {showForm && (
          <LotteryForm
            onSuccess={() => {
              setShowForm(false);
              loadLotteries();
            }}
            onCancel={() => setShowForm(false)}
          />
        )}

        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {lotteries.map((lottery) => (
            <div
              key={lottery.id}
              className="bg-white overflow-hidden shadow rounded-lg hover:shadow-md transition-shadow"
            >
              <div className="p-5">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-2xl font-bold text-gray-900">{lottery.year}</h3>
                  <span
                    className={`px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(
                      lottery.status
                    )}`}
                  >
                    {getStatusText(lottery.status)}
                  </span>
                </div>

                <div className="space-y-2 text-sm text-gray-600">
                  <p>
                    <span className="font-medium">Dragningsdatum:</span>{' '}
                    {new Date(lottery.drawDate).toLocaleDateString('sv-SE')}
                  </p>
                </div>

                <div className="mt-4 space-y-2">
                  <div className="flex justify-between">
                    <button
                      onClick={() => setSelectedLottery(lottery)}
                      className="text-indigo-600 hover:text-indigo-800 text-sm font-medium"
                    >
                      Hantera deltagare
                    </button>
                    {lottery.status === 'PLANNED' && (
                      <button
                        onClick={() => handleDelete(lottery.id!)}
                        className="text-red-600 hover:text-red-800 text-sm"
                      >
                        Ta bort
                      </button>
                    )}
                  </div>
                  {lottery.status === 'ACTIVE' && (
                    <button
                      onClick={() => handlePerformDraw(lottery)}
                      disabled={performing}
                      className="w-full px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50 text-sm font-medium"
                    >
                      {performing ? 'Genomför dragning...' : '🎲 Genomför dragning'}
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        {lotteries.length === 0 && (
          <div className="text-center py-12 bg-white rounded-lg shadow">
            <p className="text-gray-500">Inga utlottningar hittades</p>
          </div>
        )}

        {selectedLottery && (
          <ParticipantModal
            lottery={selectedLottery}
            onClose={() => setSelectedLottery(null)}
          />
        )}

        {drawResult && (
          <DrawResultModal
            result={drawResult}
            onClose={() => setDrawResult(null)}
          />
        )}
      </div>
    </div>
  );
};

interface LotteryFormProps {
  onSuccess: () => void;
  onCancel: () => void;
}

const LotteryForm: React.FC<LotteryFormProps> = ({ onSuccess, onCancel }) => {
  const [formData, setFormData] = useState<Lottery>({
    year: new Date().getFullYear(),
    drawDate: new Date().toISOString().split('T')[0],
    status: 'PLANNED',
  });
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      await api.createLottery(formData);
      onSuccess();
    } catch (err) {
      alert('Kunde inte skapa utlottning');
      console.error(err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mb-6 bg-white shadow sm:rounded-lg">
      <div className="px-4 py-5 sm:p-6">
        <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">
          Skapa ny utlottning
        </h3>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label className="block text-sm font-medium text-gray-700">År</label>
              <input
                type="number"
                required
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.year}
                onChange={(e) =>
                  setFormData({ ...formData, year: parseInt(e.target.value) })
                }
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Dragningsdatum
              </label>
              <input
                type="date"
                required
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.drawDate}
                onChange={(e) => setFormData({ ...formData, drawDate: e.target.value })}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Status</label>
              <select
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.status}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    status: e.target.value as 'PLANNED' | 'ACTIVE' | 'COMPLETED',
                  })
                }
              >
                <option value="PLANNED">Planerad</option>
                <option value="ACTIVE">Aktiv</option>
                <option value="COMPLETED">Avslutad</option>
              </select>
            </div>
          </div>
          <div className="flex justify-end space-x-3">
            <button
              type="button"
              onClick={onCancel}
              className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
            >
              Avbryt
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50"
            >
              {submitting ? 'Skapar...' : 'Skapa'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

interface ParticipantModalProps {
  lottery: Lottery;
  onClose: () => void;
}

const ParticipantModal: React.FC<ParticipantModalProps> = ({ lottery, onClose }) => {
  const [members, setMembers] = useState<Member[]>([]);
  const [selectedMember, setSelectedMember] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadMembers();
  }, []);

  const loadMembers = async () => {
    try {
      const data = await api.getAllMembers();
      setMembers(data.filter((m) => m.active));
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddParticipant = async () => {
    if (!selectedMember) return;

    try {
      await api.addParticipant({
        lotteryId: lottery.id!,
        memberId: selectedMember,
        numberOfTickets: 3, // Will be calculated by backend
        hasWon: false,
      });
      alert('Deltagare tillagd!');
      setSelectedMember(null);
    } catch (err) {
      alert('Kunde inte lägga till deltagare');
      console.error(err);
    }
  };

  return (
    <div className="fixed z-10 inset-0 overflow-y-auto">
      <div className="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"></div>

        <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">
              Hantera deltagare - {lottery.year}
            </h3>

            {loading ? (
              <p>Laddar medlemmar...</p>
            ) : (
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Välj medlem
                  </label>
                  <select
                    className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                    value={selectedMember || ''}
                    onChange={(e) => setSelectedMember(Number(e.target.value))}
                  >
                    <option value="">Välj medlem...</option>
                    {members.map((member) => (
                      <option key={member.id} value={member.id}>
                        {member.firstName} {member.lastName}
                      </option>
                    ))}
                  </select>
                </div>

                <button
                  onClick={handleAddParticipant}
                  disabled={!selectedMember}
                  className="w-full px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:opacity-50"
                >
                  Lägg till deltagare
                </button>
              </div>
            )}
          </div>

          <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button
              type="button"
              onClick={onClose}
              className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
            >
              Stäng
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

interface DrawResultModalProps {
  result: DrawResult;
  onClose: () => void;
}

const DrawResultModal: React.FC<DrawResultModalProps> = ({ result, onClose }) => {
  return (
    <div className="fixed z-10 inset-0 overflow-y-auto">
      <div className="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"></div>

        <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-4xl sm:w-full">
          <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div className="sm:flex sm:items-start">
              <div className="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-green-100 sm:mx-0 sm:h-10 sm:w-10">
                <span className="text-2xl">🎉</span>
              </div>
              <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left flex-1">
                <h3 className="text-lg leading-6 font-medium text-gray-900">
                  Dragning genomförd - {result.year}
                </h3>
                <div className="mt-4">
                  <div className="bg-gray-50 rounded-lg p-4 mb-4">
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
                      <div>
                        <div className="text-2xl font-bold text-indigo-600">{result.totalParticipants}</div>
                        <div className="text-sm text-gray-600">Deltagare</div>
                      </div>
                      <div>
                        <div className="text-2xl font-bold text-green-600">{result.totalWinners}</div>
                        <div className="text-sm text-gray-600">Vinnare</div>
                      </div>
                      <div>
                        <div className="text-2xl font-bold text-purple-600">{result.totalArtworks}</div>
                        <div className="text-sm text-gray-600">Konstverk</div>
                      </div>
                      <div>
                        <div className="text-2xl font-bold text-blue-600">{result.assignedArtworks}</div>
                        <div className="text-sm text-gray-600">Tilldelade</div>
                      </div>
                    </div>
                  </div>

                  <h4 className="font-semibold text-gray-900 mb-3">Vinnare:</h4>
                  <div className="max-h-96 overflow-y-auto space-y-2">
                    {result.winners.map((winner, index) => (
                      <div key={winner.memberId} className="border rounded-lg p-3 hover:bg-gray-50">
                        <div className="flex items-start justify-between">
                          <div className="flex items-center space-x-3">
                            <span className="flex-shrink-0 w-8 h-8 bg-indigo-100 text-indigo-800 rounded-full flex items-center justify-center font-bold text-sm">
                              {index + 1}
                            </span>
                            <div>
                              <div className="font-semibold text-gray-900">{winner.memberName}</div>
                              <div className="text-sm text-gray-600">
                                {winner.artworkTitle} - {winner.artworkArtist}
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button
              type="button"
              onClick={onClose}
              className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 sm:ml-3 sm:w-auto sm:text-sm"
            >
              Stäng
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
