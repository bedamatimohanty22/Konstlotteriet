import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../../services/api';
import type { RankingList, Artwork, Lottery } from '../../types';

export const RankingEditor: React.FC = () => {
  const { lotteryId } = useParams<{ lotteryId: string }>();
  const navigate = useNavigate();

  const [lottery, setLottery] = useState<Lottery | null>(null);
  const [rankingList, setRankingList] = useState<RankingList | null>(null);
  const [availableArtworks, setAvailableArtworks] = useState<Artwork[]>([]);
  const [rankedArtworks, setRankedArtworks] = useState<(Artwork & { rank: number })[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [draggedIndex, setDraggedIndex] = useState<number | null>(null);

  useEffect(() => {
    loadData();
  }, [lotteryId]);

  const loadData = async () => {
    if (!lotteryId) return;

    try {
      setLoading(true);
      setError('');

      // Load lottery details
      const lotteryData = await api.getLotteryById(parseInt(lotteryId));
      setLottery(lotteryData);

      // Try to load existing ranking list
      let existingList: RankingList | null = null;
      try {
        existingList = await api.getRankingListByLottery(parseInt(lotteryId));
        setRankingList(existingList);
      } catch (err) {
        // No existing list - will create one
        console.log('No existing ranking list found');
      }

      // Load available artworks for this lottery year
      const artworks = await api.getAvailableArtworksForLottery(lotteryData.year);
      setAvailableArtworks(artworks);

      // If there's an existing ranking list, populate ranked artworks
      if (existingList && existingList.items && existingList.items.length > 0) {
        const ranked = existingList.items
          .map((item) => {
            const artwork = artworks.find((a) => a.id === item.artworkId);
            return artwork ? { ...artwork, rank: item.rank } : null;
          })
          .filter((a): a is Artwork & { rank: number } => a !== null)
          .sort((a, b) => a.rank - b.rank);
        setRankedArtworks(ranked);
      }
    } catch (err) {
      setError('Kunde inte ladda data: ' + (err as Error).message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const addArtwork = (artwork: Artwork) => {
    const nextRank = rankedArtworks.length + 1;
    setRankedArtworks([...rankedArtworks, { ...artwork, rank: nextRank }]);
  };

  const removeArtwork = (artworkId: number) => {
    const filtered = rankedArtworks.filter((a) => a.id !== artworkId);
    // Re-rank remaining items
    const reranked = filtered.map((artwork, index) => ({
      ...artwork,
      rank: index + 1,
    }));
    setRankedArtworks(reranked);
  };

  const handleDragStart = (index: number) => {
    setDraggedIndex(index);
  };

  const handleDragOver = (e: React.DragEvent, index: number) => {
    e.preventDefault();
    if (draggedIndex === null || draggedIndex === index) return;

    const items = [...rankedArtworks];
    const draggedItem = items[draggedIndex];
    items.splice(draggedIndex, 1);
    items.splice(index, 0, draggedItem);

    // Re-rank
    const reranked = items.map((artwork, idx) => ({
      ...artwork,
      rank: idx + 1,
    }));

    setRankedArtworks(reranked);
    setDraggedIndex(index);
  };

  const handleDragEnd = () => {
    setDraggedIndex(null);
  };

  const moveUp = (index: number) => {
    if (index === 0) return;
    const items = [...rankedArtworks];
    [items[index - 1], items[index]] = [items[index], items[index - 1]];
    const reranked = items.map((artwork, idx) => ({ ...artwork, rank: idx + 1 }));
    setRankedArtworks(reranked);
  };

  const moveDown = (index: number) => {
    if (index === rankedArtworks.length - 1) return;
    const items = [...rankedArtworks];
    [items[index], items[index + 1]] = [items[index + 1], items[index]];
    const reranked = items.map((artwork, idx) => ({ ...artwork, rank: idx + 1 }));
    setRankedArtworks(reranked);
  };

  const saveDraft = async () => {
    if (!lotteryId) return;

    try {
      setSaving(true);
      setError('');
      setSuccess('');

      // Create ranking list if it doesn't exist
      let listId = rankingList?.id;
      if (!listId) {
        const created = await api.createRankingListForLottery(parseInt(lotteryId));
        listId = created.id!;
        setRankingList(created);
      }

      // Update with current rankings
      const items = rankedArtworks.map((artwork) => ({
        artworkId: artwork.id!,
        rank: artwork.rank,
      }));

      await api.updateRankingList(listId, items);
      setSuccess('Rankinglista sparad som utkast');

      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError('Kunde inte spara: ' + (err as Error).message);
    } finally {
      setSaving(false);
    }
  };

  const submitList = async () => {
    if (!rankingList?.id) {
      setError('Du måste först spara listan innan du kan skicka in den');
      return;
    }

    if (rankedArtworks.length === 0) {
      setError('Du måste lägga till minst ett konstverk i rankinglistan');
      return;
    }

    try {
      setSaving(true);
      setError('');

      // First save current state
      const items = rankedArtworks.map((artwork) => ({
        artworkId: artwork.id!,
        rank: artwork.rank,
      }));
      await api.updateRankingList(rankingList.id, items);

      // Then submit
      await api.submitRankingList(rankingList.id);

      setSuccess('Rankinglista inskickad!');
      setTimeout(() => navigate('/member/rankings'), 2000);
    } catch (err) {
      setError('Kunde inte skicka in: ' + (err as Error).message);
    } finally {
      setSaving(false);
    }
  };

  const unrankedArtworks = availableArtworks.filter(
    (artwork) => !rankedArtworks.some((r) => r.id === artwork.id)
  );

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
          <div className="flex justify-between h-16 items-center">
            <div className="flex items-center">
              <button
                onClick={() => navigate('/member/rankings')}
                className="text-indigo-600 hover:text-indigo-800"
              >
                ← Tillbaka
              </button>
              <h1 className="ml-4 text-xl font-bold text-gray-900">
                Rankinglista - {lottery?.year}
              </h1>
            </div>
            <div className="flex space-x-3">
              <button
                onClick={saveDraft}
                disabled={saving || rankedArtworks.length === 0}
                className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {saving ? 'Sparar...' : 'Spara utkast'}
              </button>
              <button
                onClick={submitList}
                disabled={saving || rankedArtworks.length === 0}
                className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Skicka in
              </button>
            </div>
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        {error && (
          <div className="mb-4 bg-red-50 border border-red-200 p-4 rounded-md">
            <p className="text-red-800">{error}</p>
          </div>
        )}

        {success && (
          <div className="mb-4 bg-green-50 border border-green-200 p-4 rounded-md">
            <p className="text-green-800">{success}</p>
          </div>
        )}

        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
          <h3 className="font-semibold text-blue-900 mb-2">📋 Instruktioner</h3>
          <ul className="text-sm text-blue-800 space-y-1">
            <li>• Lägg till konstverk från listan till höger</li>
            <li>• Dra och släpp för att ändra ordning (eller använd pilknapparna)</li>
            <li>• Konstverk högst upp har högsta prioritet</li>
            <li>• Spara som utkast när du vill ta en paus</li>
            <li>• Skicka in när du är klar (kan inte ändras efteråt)</li>
          </ul>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Ranked Artworks (Left) */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4">
              Din Rankinglista ({rankedArtworks.length})
            </h2>

            {rankedArtworks.length === 0 ? (
              <div className="text-center py-12 text-gray-500">
                <p>Ingen konstverk har lagts till än.</p>
                <p className="text-sm mt-2">
                  Klicka på "Lägg till" för ett konstverk till höger.
                </p>
              </div>
            ) : (
              <div className="space-y-2">
                {rankedArtworks.map((artwork, index) => (
                  <div
                    key={artwork.id}
                    draggable
                    onDragStart={() => handleDragStart(index)}
                    onDragOver={(e) => handleDragOver(e, index)}
                    onDragEnd={handleDragEnd}
                    className={`border rounded-lg p-4 cursor-move hover:shadow-md transition-shadow ${
                      draggedIndex === index ? 'opacity-50' : ''
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-3">
                        <span className="flex-shrink-0 w-8 h-8 bg-indigo-100 text-indigo-800 rounded-full flex items-center justify-center font-bold">
                          {index + 1}
                        </span>
                        <div>
                          <h3 className="font-semibold text-gray-900">{artwork.title}</h3>
                          <p className="text-sm text-gray-600">{artwork.artist}</p>
                        </div>
                      </div>
                      <div className="flex items-center space-x-2">
                        <button
                          onClick={() => moveUp(index)}
                          disabled={index === 0}
                          className="p-1 text-gray-600 hover:text-gray-900 disabled:opacity-30"
                          title="Flytta upp"
                        >
                          ↑
                        </button>
                        <button
                          onClick={() => moveDown(index)}
                          disabled={index === rankedArtworks.length - 1}
                          className="p-1 text-gray-600 hover:text-gray-900 disabled:opacity-30"
                          title="Flytta ner"
                        >
                          ↓
                        </button>
                        <button
                          onClick={() => removeArtwork(artwork.id!)}
                          className="ml-2 p-1 text-red-600 hover:text-red-800"
                          title="Ta bort"
                        >
                          ✕
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Available Artworks (Right) */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4">
              Tillgängliga Konstverk ({unrankedArtworks.length})
            </h2>

            {unrankedArtworks.length === 0 ? (
              <div className="text-center py-12 text-gray-500">
                <p>Alla konstverk har lagts till i rankinglistan!</p>
              </div>
            ) : (
              <div className="space-y-2 max-h-[600px] overflow-y-auto">
                {unrankedArtworks.map((artwork) => (
                  <div
                    key={artwork.id}
                    className="border rounded-lg p-4 hover:border-indigo-300 transition-colors"
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <h3 className="font-semibold text-gray-900">{artwork.title}</h3>
                        <p className="text-sm text-gray-600 mb-1">{artwork.artist}</p>
                        {artwork.description && (
                          <p className="text-xs text-gray-500 line-clamp-2">
                            {artwork.description}
                          </p>
                        )}
                        <div className="mt-2 flex items-center space-x-3 text-xs text-gray-500">
                          <span>År: {artwork.year}</span>
                          {artwork.estimatedValue && (
                            <span>Värde: {artwork.estimatedValue} kr</span>
                          )}
                        </div>
                      </div>
                      <button
                        onClick={() => addArtwork(artwork)}
                        className="ml-3 px-3 py-1 bg-indigo-600 text-white text-sm rounded hover:bg-indigo-700 flex-shrink-0"
                      >
                        Lägg till
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
