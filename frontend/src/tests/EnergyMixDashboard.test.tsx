import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { EnergyMixDashboard } from '../components/EnergyMixDashboard.tsx';

const mockFetchEnergyMix = vi.fn();
vi.mock('../api/energyApi', () => ({
    fetchEnergyMix: (...args: any[]) => mockFetchEnergyMix(...args)
}));


vi.mock('recharts', () => ({
    ResponsiveContainer: ({ children }: any) => <div>{children}</div>,
    PieChart: ({ children }: any) => <div>{children}</div>,
    Pie: () => <div data-testid="pie-mock"></div>,
    Tooltip: () => null,
    Legend: () => null,
    Cell: () => null,
}));

describe('EnergyMixDashboard', () => {
    beforeEach(() => {
        mockFetchEnergyMix.mockReset();
    });

    it('renders initial state correctly', () => {
        render(<EnergyMixDashboard />);
        expect(screen.getByText('Miks Energetyczny (3 dni)')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Sprawdź/i })).toBeInTheDocument();
    });

    it('fetches and displays mix data on button click', async () => {
        const mockData = [
            {
                date: '2026-06-21T00:00:00Z',
                cleanEnergyPercentage: 65.5,
                sourceAverages: { wind: 40, solar: 20, coal: 30, other: 10 }
            }
        ];

        mockFetchEnergyMix.mockResolvedValueOnce(mockData);

        render(<EnergyMixDashboard />);

        const button = screen.getByRole('button', { name: /Sprawdź/i });
        fireEvent.click(button);

        expect(screen.getByText('Pobieram dane...')).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText('65.5%')).toBeInTheDocument();
            expect(screen.getByTestId('pie-mock')).toBeInTheDocument();
        });
    });

    it('displays error message on api failure', async () => {
        mockFetchEnergyMix.mockRejectedValueOnce(new Error('Błąd pobierania miksu'));

        render(<EnergyMixDashboard />);

        fireEvent.click(screen.getByRole('button', { name: /Sprawdź/i }));

        await waitFor(() => {
            expect(screen.getByText('Błąd pobierania miksu')).toBeInTheDocument();
        });
    });
});