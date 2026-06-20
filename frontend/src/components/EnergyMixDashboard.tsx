import { useState } from 'react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { fetchEnergyMix } from '../api/energyApi';
import type { DailyEnergyMix } from '../types/energy';

type FuelType = 'wind' | 'solar' | 'hydro' | 'nuclear' | 'biomass' | 'gas' | 'coal' | 'imports' | 'other';

const FUEL_COLORS: Record<FuelType, string> = {
    wind: '#10b981',
    solar: '#fbbf24',
    hydro: '#3b82f6',
    nuclear: '#8b5cf6',
    biomass: '#84cc16',
    gas: '#94a3b8',
    coal: '#334155',
    imports: '#f97316',
    other: '#cbd5e1'
};

const getFuelColor = (fuel: string) => {
    return FUEL_COLORS[fuel as keyof typeof FUEL_COLORS] ?? FUEL_COLORS.other;
};


export const EnergyMixDashboard = () => {
    const [data, setData] = useState<DailyEnergyMix[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);


    const handleFetchData = async () => {
        setLoading(true);
        setError(null);

        try {
            const mixData = await fetchEnergyMix();
            setData(mixData);
        } catch (err: unknown) {
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError('Nie udało się pobrać danych');
            }
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('pl-PL', {
            weekday: 'long',
            day: 'numeric',
            month: 'long'
        });
    };

    return (
        <div className="card">
            <h2 className="card-title">Miks Energetyczny (3 dni)</h2>
            <p className="description">
                Sprawdź, z jakich źródeł pochodzi energia w Wielkiej Brytanii i jaki jest udział energii czystej.
            </p>

            <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
                <button
                    onClick={handleFetchData}
                    disabled={loading}
                    className="btn-submit"
                >
                    {loading ? 'Pobieram dane...' : 'Sprawdź '}
                </button>
            </div>

            {error && <div className="error-message">{error}</div>}

            {data.length > 0 && (
                <div className="charts-grid">
                    {data.map((day, index) => {
                        const chartData = Object.entries(day.sourceAverages).map(([fuel, perc]) => ({
                            name: fuel,
                            value: perc
                        }));

                        return (
                            <div key={index} className="chart-card">
                                <h3 className="chart-day-title">{formatDate(day.date)}</h3>
                                <div className="clean-energy-badge">
                                    Czysta energia: <span>{day.cleanEnergyPercentage}%</span>
                                </div>

                                <div className="chart-wrapper">
                                    <ResponsiveContainer width="100%" height="100%">
                                        <PieChart>
                                            <Pie
                                                data={chartData}
                                                cx="50%"
                                                cy="50%"
                                                innerRadius={60}
                                                outerRadius={80}
                                                paddingAngle={2}
                                                dataKey="value"
                                            >
                                                {chartData.map((entry, i) => (
                                                    <Cell
                                                        key={`cell-${i}`}
                                                        fill={getFuelColor(entry.name)}
                                                    />
                                                ))}
                                            </Pie>
                                            <Tooltip
                                                formatter={(value: unknown) =>
                                                    typeof value === 'number' ? `${value.toFixed(1)}%` : `${value}%`
                                                }
                                            />
                                            <Legend />
                                        </PieChart>
                                    </ResponsiveContainer>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
};