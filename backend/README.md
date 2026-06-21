# Energy App - Backend

Ta część aplikacji odpowiada za komunikację z zewnętrznym API Carbon Intensity, przetwarzanie danych oraz logikę biznesową.

## Wykorzystane technologie
* **Język:** Java 21
* **Framework:** Spring Boot 
* **Testy:** JUnit 5 & Mockito

## Dostępne Endpointy API
* `GET /api/energy/mix`  
  Pobiera dane o miksie energetycznym z 3 dni. Grupuje interwały półgodzinne według daty, oblicza średnie wartości dla każdego dnia oraz procentowy udział czystej energii.

* `GET /api/energy/optimal-window?hours={1-6}`  
  Przyjmuje długość okna ładowania w pełnych godzinach. Zwraca datę i godzinę rozpoczęcia, zakończenia oraz średni procent czystej energii w najbardziej optymalnym przedziale z najbliższych dwóch dni.

  Backend jest dostępny pod adresem: https://energy-app-backend-d9v5.onrender.com

  >  Aplikacja jest hostowana na darmowym planie platformy Render. W związku z tym instancje usypiają się po 15 minutach nieaktywności. **Pierwsze wysłane zapytanie do backendu może potrwać około 50-60 sekund**. Każde kolejne zapytanie zostanie obsłużone natychmiastowo.

## Uruchomienie testów jednostkowych
```bash
./mvnw.cmd test
```
---
*Pełna instrukcja uruchomienia całej aplikacji (zarówno frontendu, jak i backendu) za pomocą Dockera znajduje się w [głównym pliku README projektu](https://github.com/PawelJasinski25/energy-app/blob/main/README.md).*
