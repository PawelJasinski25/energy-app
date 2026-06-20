export interface GenerationMix {
    fuel: string;
    perc: number;
}

export interface DailyEnergyMix {
    date: string;
    cleanEnergyPercentage: number;
    sourceAverages: Record<string, number>;
}

export interface ChargingWindow {
    start: string;
    end: string;
    cleanEnergyPercentage: number;
}