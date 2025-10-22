import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../services/api';
import type { Artwork, Lottery } from '../../types';

export const Artworks: React.FC = () => {
  const [artworks, setArtworks] = useState<Artwork[]>([]);
  const [lotteries, setLotteries] = useState<Lottery[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [selectedLottery, setSelectedLottery] = useState<number | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    if (selectedLottery) {
      loadArtworksByLottery(selectedLottery);
    } else {
      loadArtworks();
    }
  }, [selectedLottery]);

  const loadData = async () => {
    try {
      const [artworksData, lotteriesData] = await Promise.all([
        api.getAllArtworks(),
        api.getAllLotteries(),
      ]);
      setArtworks(artworksData);
      setLotteries(lotteriesData);
    } catch (err) {
      setError('Kunde inte ladda data');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadArtworks = async () => {
    try {
      const data = await api.getAllArtworks();
      setArtworks(data);
    } catch (err) {
      console.error(err);
    }
  };

  const loadArtworksByLottery = async (lotteryId: number) => {
    try {
      const data = await api.getArtworksByLottery(lotteryId);
      setArtworks(data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Är du säker på att du vill ta bort detta konstverk?')) {
      return;
    }

    try {
      await api.deleteArtwork(id);
      await loadArtworks();
    } catch (err) {
      alert('Kunde inte ta bort konstverk');
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
              <h1 className="ml-4 text-xl font-bold text-gray-900">Konstverk</h1>
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

        <div className="mb-4 flex justify-between items-center">
          <div className="flex space-x-4">
            <button
              onClick={() => setShowForm(true)}
              className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
            >
              + Lägg till konstverk
            </button>

            <select
              value={selectedLottery || ''}
              onChange={(e) => setSelectedLottery(e.target.value ? Number(e.target.value) : null)}
              className="px-4 py-2 border border-gray-300 rounded-md"
            >
              <option value="">Alla konstverk</option>
              {lotteries.map((lottery) => (
                <option key={lottery.id} value={lottery.id}>
                  {lottery.year} - {lottery.status}
                </option>
              ))}
            </select>
          </div>
        </div>

        {showForm && (
          <ArtworkForm
            onSuccess={() => {
              setShowForm(false);
              loadArtworks();
            }}
            onCancel={() => setShowForm(false)}
          />
        )}

        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {artworks.map((artwork) => (
            <div
              key={artwork.id}
              className="bg-white overflow-hidden shadow rounded-lg hover:shadow-md transition-shadow"
            >
              <div className="p-5">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-medium text-gray-900">{artwork.title}</h3>
                  <span className="px-2 py-1 text-xs font-semibold rounded-full bg-indigo-100 text-indigo-800">
                    {artwork.lotteryYear}-{artwork.yearId}
                  </span>
                </div>

                <div className="space-y-2 text-sm text-gray-600">
                  <p>
                    <span className="font-medium">Konstnär:</span> {artwork.artist}
                  </p>
                  <p>
                    <span className="font-medium">Priskategori:</span> {artwork.priceCategory}
                  </p>
                  <p>
                    <span className="font-medium">Inköpsår:</span> {artwork.purchaseYear}
                  </p>
                  <p>
                    <span className="font-medium">Lotteriår:</span> {artwork.lotteryYear}
                  </p>
                  {artwork.technique && (
                    <p>
                      <span className="font-medium">Teknik:</span> {artwork.technique}
                    </p>
                  )}
                  {artwork.size && (
                    <p>
                      <span className="font-medium">Storlek:</span> {artwork.size}
                    </p>
                  )}
                </div>

                <div className="mt-4 flex justify-end">
                  <button
                    onClick={() => handleDelete(artwork.id!)}
                    className="text-red-600 hover:text-red-800 text-sm"
                  >
                    Ta bort
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>

        {artworks.length === 0 && (
          <div className="text-center py-12 bg-white rounded-lg shadow">
            <p className="text-gray-500">Inga konstverk hittades</p>
          </div>
        )}
      </div>
    </div>
  );
};

interface ArtworkFormProps {
  onSuccess: () => void;
  onCancel: () => void;
}

const ArtworkForm: React.FC<ArtworkFormProps> = ({ onSuccess, onCancel }) => {
  const [formData, setFormData] = useState<Artwork>({
    title: '',
    artist: '',
    priceCategory: '',
    technique: '',
    size: '',
    purchaseYear: new Date().getFullYear(),
    lotteryYear: new Date().getFullYear(),
  });
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      await api.createArtwork(formData);
      onSuccess();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error';
      alert('Kunde inte skapa konstverk: ' + errorMessage);
      console.error('Full error:', err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mb-6 bg-white shadow sm:rounded-lg">
      <div className="px-4 py-5 sm:p-6">
        <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">
          Lägg till nytt konstverk
        </h3>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label className="block text-sm font-medium text-gray-700">Titel *</label>
              <input
                type="text"
                required
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Konstnär *</label>
              <input
                type="text"
                required
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.artist}
                onChange={(e) => setFormData({ ...formData, artist: e.target.value })}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Priskategori *</label>
              <input
                type="text"
                required
                placeholder="t.ex. A, B, C"
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.priceCategory}
                onChange={(e) => setFormData({ ...formData, priceCategory: e.target.value })}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Teknik</label>
              <input
                type="text"
                placeholder="t.ex. Olja på duk"
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.technique || ''}
                onChange={(e) => setFormData({ ...formData, technique: e.target.value })}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Storlek</label>
              <input
                type="text"
                placeholder="t.ex. 50x70 cm"
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.size || ''}
                onChange={(e) => setFormData({ ...formData, size: e.target.value })}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Inköpsår *</label>
              <input
                type="number"
                required
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.purchaseYear}
                onChange={(e) =>
                  setFormData({ ...formData, purchaseYear: parseInt(e.target.value) })
                }
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Lotteriår *</label>
              <input
                type="number"
                required
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={formData.lotteryYear}
                onChange={(e) =>
                  setFormData({ ...formData, lotteryYear: parseInt(e.target.value) })
                }
              />
            </div>
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
