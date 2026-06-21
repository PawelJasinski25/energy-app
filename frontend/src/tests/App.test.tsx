import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import App from '../App.tsx';

describe('App Component', () => {
    it('renders header correctly', () => {
        render(<App />);
        expect(screen.getByText('Energia UK')).toBeInTheDocument();
    });

    it('switches tabs correctly', () => {
        render(<App />);

        expect(screen.getByText(/Sprawdź, z jakich źródeł pochodzi energia/)).toBeInTheDocument();

        const optimizerTab = screen.getByText('Optymalne Ładowanie');
        fireEvent.click(optimizerTab);
        expect(screen.getByText('Wyznacz optymalne okno ładowania')).toBeInTheDocument();
    });
});