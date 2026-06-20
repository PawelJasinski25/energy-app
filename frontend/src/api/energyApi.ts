import type {ChargingWindow, DailyEnergyMix} from '../types/energy';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const fetchOptimalWindow = async (hours: number): Promise<ChargingWindow> => {
    const response = await fetch(`${API_BASE_URL}/api/energy/optimal-window?hours=${hours}`);

    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Wystąpił błąd serwera');
    }

    return response.json();
};

export const fetchEnergyMix = async (): Promise<DailyEnergyMix[]> => {
    const response = await fetch(`${API_BASE_URL}/api/energy/mix`);

    if (!response.ok) {
        throw new Error('Wystąpił błąd podczas pobierania miksu energetycznego');
    }

    return response.json();
};