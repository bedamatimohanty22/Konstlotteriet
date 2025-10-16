import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../services/api';
import type { RankingList, Lottery } from '../../types';

export const Rankings: React.FC = () => {
  const [rankingLists, setRankingLists] = useState<RankingList[]>([]);
  const [lotteries, setLotteries] = useState<Lottery[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const lotteriesData = await api.getAllLotteries();
      setLotteries(lotteriesData.filter((l) => l.status !== 'COMPLETED'));

      // Load ranking lists for each lottery
      const lists: RankingList[] = [];
      for (const lottery of lotteriesData) {
        try {
          const list = await api.getRankingListByLottery(lottery.id!);
          lists.push(list);
        } catch (err) {
          // No ranking list exists for this lottery
        }
      }
      setRankingLists(lists);
    } catch (err) {
      setError('Kunde inte ladda rankinglistor');
      console.error(err);
    } finally {
      setLoading(false);
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
              <h1 className="ml-4 text-xl font-bold text-gray-900">
                Mina Rankinglistor
              </h1>
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

        <div className="bg-white shadow sm:rounded-lg mb-6">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900">
              Om Rankinglistor
            </h3>
            <div className="mt-2 max-w-xl text-sm text-gray-500">
              <p>
                Här kan du skapa och hantera dina rankinglistor för kommande utlottningar.
                Din rankinglista anger i vilken ordning du föredrar konstverken vid
                dragningen.
              </p>
              <p className="mt-2">
                <strong>Tips:</strong> Placera de konstverk du helst vill vinna högst upp i
                listan!
              </p>
            </div>
          </div>
        </div>

        {lotteries.length > 0 ? (
          <div className="space-y-6">
            {lotteries.map((lottery) => {
              const existingList = rankingLists.find((l) => l.lotteryId === lottery.id);
              return (
                <div
                  key={lottery.id}
                  className="bg-white shadow overflow-hidden sm:rounded-lg"
                >
                  <div className="px-4 py-5 sm:px-6 flex justify-between items-center">
                    <div>
                      <h3 className="text-lg leading-6 font-medium text-gray-900">
                        Utlottning {lottery.year}
                      </h3>
                      <p className="mt-1 max-w-2xl text-sm text-gray-500">
                        Dragning: {new Date(lottery.drawDate).toLocaleDateString('sv-SE')}
                      </p>
                    </div>
                    {existingList ? (
                      <span className="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                        ✓ Lista skapad
                      </span>
                    ) : (
                      <span className="px-2 py-1 text-xs font-semibold rounded-full bg-yellow-100 text-yellow-800">
                        Ingen lista
                      </span>
                    )}
                  </div>
                  {existingList ? (
                    <div className="border-t border-gray-200 px-4 py-5 sm:px-6">
                      <p className="text-sm text-gray-600">
                        Du har skapat en rankinglista med{' '}
                        <strong>{existingList.items?.length || 0}</strong> konstverk.
                      </p>
                      {existingList.submittedAt ? (
                        <div className="mt-2">
                          <span className="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                            ✓ Inskickad {new Date(existingList.submittedAt).toLocaleDateString('sv-SE')}
                          </span>
                          <button
                            onClick={() => navigate(`/member/rankings/edit/${lottery.id}`)}
                            className="ml-3 text-indigo-600 hover:text-indigo-800 text-sm font-medium"
                          >
                            Visa lista →
                          </button>
                        </div>
                      ) : (
                        <button
                          onClick={() => navigate(`/member/rankings/edit/${lottery.id}`)}
                          className="mt-2 text-indigo-600 hover:text-indigo-800 text-sm font-medium"
                        >
                          Redigera lista →
                        </button>
                      )}
                    </div>
                  ) : (
                    <div className="border-t border-gray-200 px-4 py-5 sm:px-6">
                      <p className="text-sm text-gray-600 mb-3">
                        Du har inte skapat en rankinglista för denna utlottning än.
                      </p>
                      <button
                        onClick={() => navigate(`/member/rankings/edit/${lottery.id}`)}
                        className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 text-sm"
                      >
                        Skapa rankinglista
                      </button>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        ) : (
          <div className="bg-white shadow sm:rounded-lg">
            <div className="px-4 py-5 sm:p-6 text-center">
              <p className="text-gray-500">
                Det finns inga aktiva utlottningar för tillfället.
              </p>
            </div>
          </div>
        )}

        {rankingLists.length === 0 && lotteries.length > 0 && (
          <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
            <p className="text-sm text-blue-800">
              💡 <strong>Kom igång:</strong> Skapa din första rankinglista genom att
              klicka på "Skapa rankinglista" ovan.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};
