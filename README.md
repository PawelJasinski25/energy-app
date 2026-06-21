# Energy App

Aplikacja webowa stworzona w ramach zadania rekrutacyjnego. Służy do monitorowania aktualnego oraz prognozowanego miksu energetycznego Wielkiej Brytanii, a także do wyznaczania optymalnego okna czasowego do ładowania pojazdów elektrycznych, maksymalizując udział czystej energii.

Aplikacja jest dostępna pod adresem: https://energy-app-gojm.onrender.com
>  Aplikacja jest hostowana na darmowym planie platformy Render. W związku z tym instancje usypiają się po 15 minutach nieaktywności. **Pierwsze załadowanie frontendu oraz pierwsze wysłane zapytanie do backendu może potrwać około 50-60 sekund**. Każde kolejne zapytanie zostanie obsłużone natychmiastowo.

## Wykorzystane technologie
**Backend:**
* **Język:** Java 21
* **Framework:** Spring Boot 
* **Testy:** JUnit 5 & Mockito

**Frontend:**
* **Język:** TypeScript
* **Biblioteka:** React
* **Testy:** Vitest & React Testing Library

## Uruchomienie lokalne aplikacji
### Wymagania wstępne
Upewnij się, że masz zainstalowane:
* [Docker Desktop](https://www.docker.com/products/docker-desktop/)

### Kolejne kroki
1. Sklonuj repozytorium:
   ```bash
   git clone https://github.com/PawelJasinski25/energy-app.git
   cd energy-app

2. Uruchom aplikację:
   ```bash
   docker compose up -d --build
3. Backend będzie znajdował się pod adresem : http://localhost:8080/
4. Frontend będzie znajdował się pod adresem : http://localhost:5173/  

##  Dostępne Endpointy API (Backend)

* `GET /api/energy/mix`  
  Pobiera dane o miksie energetycznym z 3 dni. Grupuje interwały półgodzinne według daty, oblicza średnie wartości dla każdego dnia oraz procentowy udział czystej energii.

* `GET /api/energy/optimal-window?hours={1-6}`  
  Przyjmuje długość okna ładowania w pełnych godzinach. Zwraca datę i godzinę rozpoczęcia, zakończenia oraz średni procent czystej energii w najbardziej optymalnym przedziale z najbliższych dwóch dni.

  Backend jest dostępny pod adresem: https://energy-app-backend-d9v5.onrender.com

## Uruchomienie testów jednostkowych

**Backend:**
```bash
cd backend
./mvnw.cmd test
```

**Frontend:**
```bash
cd frontend
npm install
npm run test
```
