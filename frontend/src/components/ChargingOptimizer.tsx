import { useState } from 'react';
import { fetchOptimalWindow } from '../api/energyApi';
import type { ChargingWindow } from '../types/energy';

export const ChargingOptimizer = () => {
    const [hours, setHours] = useState<number>(1);
    const [result, setResult] = useState<ChargingWindow | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    const handleOptimize = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setResult(null);

        try {
            const data = await fetchOptimalWindow(hours);
            setResult(data);
        } catch (err: unknown) {
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError('Wystąpił nieoczekiwany błąd');
            }
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleString('pl-PL', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    };

    return (
        <div className="card">
            <h2 className="card-title">Wyznacz optymalne okno ładowania</h2>
            <p className="description">
                Podaj czas ładowania, a system wskaże przedział w ciągu kolejnych dwóch dni z najwyższym udziałem czystej energii.
            </p>

            <form onSubmit={handleOptimize} className="optimizer-form">
                <div className="form-group">
                    <label htmlFor="hours" className="form-label">
                        Czas ładowania:
                    </label>
                    <select
                        id="hours"
                        value={hours}
                        onChange={(e) => setHours(Number(e.target.value))}
                        className="form-select"
                    >
                        <option value={1}>1 godzina</option>
                        <option value={2}>2 godziny</option>
                        <option value={3}>3 godziny</option>
                        <option value={4}>4 godziny</option>
                        <option value={5}>5 godzin</option>
                        <option value={6}>6 godzin</option>
                    </select>
                </div>

                <button
                    type="submit"
                    disabled={loading}
                    className="btn-submit"
                >
                    {loading ? 'Szukam...' : 'Wyznacz'}
                </button>
            </form>

            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}

            {result && (
                <div className="result-box">
                    <h3 className="result-title">Najlepszy moment na ładowanie:</h3>
                    <p className="result-text"><strong>Start:</strong> {formatDate(result.start)}</p>
                    <p className="result-text"><strong>Koniec:</strong> {formatDate(result.end)}</p>
                    <p className="result-highlight">
                        <strong>Udział czystej energii:</strong> <span className="text-success">{result.cleanEnergyPercentage}%</span>
                    </p>
                </div>
            )}
        </div>
    );
};