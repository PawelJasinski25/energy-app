import { useState } from 'react';
import './App.css';
import { ChargingOptimizer } from './components/ChargingOptimizer';
import { EnergyMixDashboard } from './components/EnergyMixDashboard';

type Tab = 'mix' | 'optimizer';

function App() {
    const [activeTab, setActiveTab] = useState<Tab>('mix');

    return (
        <div className="app-container">
            <header className="header">
                <h1>Energia UK</h1>
            </header>

            <nav className="tabs">
                <button
                    className={`tab-button ${activeTab === 'mix' ? 'active' : ''}`}
                    onClick={() => setActiveTab('mix')}
                >
                    Miks Energetyczny
                </button>
                <button
                    className={`tab-button ${activeTab === 'optimizer' ? 'active' : ''}`}
                    onClick={() => setActiveTab('optimizer')}
                >
                    Optymalne Ładowanie
                </button>
            </nav>

            <main className="tab-content">
                {activeTab === 'mix' && <EnergyMixDashboard />}
                {activeTab === 'optimizer' && <ChargingOptimizer />}
            </main>
        </div>
    );
}

export default App;