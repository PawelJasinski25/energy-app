import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ChargingOptimizer } from "../components/ChargingOptimizer.tsx";


const mockFetchOptimalWindow = vi.fn();

vi.mock('../api/energyApi', () => ({
    fetchOptimalWindow: (...args: any[]) => mockFetchOptimalWindow(...args)
}));

describe('ChargingOptimizer', () => {
    beforeEach(() => {
        mockFetchOptimalWindow.mockReset();
    });

    it('renders the form correctly', () => {
        render(<ChargingOptimizer />);
        expect(screen.getByText('Wyznacz optymalne okno ładowania')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Wyznacz/i })).toBeInTheDocument();
    });

    it('displays loading state and fetches data on submit', async () => {
        const mockResponse = {
            start: '2026-06-21T10:00:00Z',
            end: '2026-06-21T13:00:00Z',
            cleanEnergyPercentage: 85
        };

        mockFetchOptimalWindow.mockResolvedValueOnce(mockResponse);

        render(<ChargingOptimizer />);

        const button = screen.getByRole('button', { name: /Wyznacz/i });
        fireEvent.click(button);

        expect(screen.getByText('Szukam...')).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText('Najlepszy moment na ładowanie:')).toBeInTheDocument();
            expect(screen.getByText('85%')).toBeInTheDocument();
        });
    });

    it('displays error message on api failure', async () => {
        mockFetchOptimalWindow.mockRejectedValueOnce(new Error('Błąd serwera'));

        render(<ChargingOptimizer />);

        fireEvent.click(screen.getByRole('button', { name: /Wyznacz/i }));

        await waitFor(() => {
            expect(screen.getByText('Błąd serwera')).toBeInTheDocument();
        });
    });
});