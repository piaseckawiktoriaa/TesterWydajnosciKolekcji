import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CollectionsPerformanceTester {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        long czasWyszukiwania;
        long czasUsuwania;

        System.out.println("Witaj w moim programie!!!");
        System.out.println("Wybierz rodzaj danych wejściowych. " +
                " Moze to być integer, double, obiekty klasy Person, obiekty klasy MyColor, obiekty klasy Ksiązka lub obiekty klasy Komputer. ");

        TypDanych wybranyTyp = wybierzEnum(scanner, TypDanych.class, "Wybierz rodzaj danych wejściowych:");
        System.out.println("Wybrany rodzaj typu danych:  " + wybranyTyp);

        System.out.println();
        System.out.println("Teraz wybierz liczbę generowanyh elementów." +
                " Mozesz wybrać 100, 500, 1000, 10000 lub wpisać dowolną liczbę.");
        Liczba wybranaLiczba = wybierzEnum(scanner, Liczba.class, "Wybierz liczbę elementów:");

        int liczbaWlasna;

        if (wybranaLiczba == Liczba.WLASNA) {
            System.out.println("Podaj własną liczbę: ");
            liczbaWlasna = scanner.nextInt();
            scanner.nextLine();
        } else {
            liczbaWlasna = wybranaLiczba.getLiczba();
        }

        System.out.println("Wybrana liczba generowanych elementów:  " + liczbaWlasna);

        System.out.println();
        System.out.println("Teraz wybierz rodzaj kolekcji do zbadania. " +
                " Mozesz wybrać listę - ArrayList lub LinkedList albo zbiór - HashSet lub TreeSet");

        Kolekcja wybranaKolekcja = wybierzEnum(scanner, Kolekcja.class, "Wybierz rodzaj kolekcji:");
        System.out.println("Wybrany rodzaj kolekcji do zbadania:  " + wybranaKolekcja);

        System.out.println();
        Prezentacja prezentacja = wybierzEnum(scanner, Prezentacja.class, "Wybierz formę prezentacji wyników:");
        System.out.println("Wybrana forma prezentacji: " + prezentacja);

        GeneratorDanych<?> generator = wybierzGenerator(wybranyTyp);
        List<?> dane = generator.generuj(liczbaWlasna);

        System.out.println();
        System.out.println("Ile razy powtórzyć test dodawania?");
        int liczbaPowtorzen = scanner.nextInt();
        scanner.nextLine();
        long sumaCzasow = 0;

        for (int i = 1; i <= liczbaPowtorzen; i++) {
            Collection<Object> kolekcja = utworzPustaKolekcje(wybranaKolekcja);
            Object element = dane.get(i % dane.size());
            long start = System.nanoTime();
            kolekcja.add(element);
            long end = System.nanoTime();
            long czasDodawania = end - start;
            sumaCzasow += czasDodawania;
            System.out.printf("Powtórzenie %d — czas: %.4f ms, dodany element: %s%n", i, czasDodawania / 1_000_000.0, element);

            double sredniCzas = sumaCzasow / (double) liczbaPowtorzen / 1_000_000.0;
            System.out.printf("Średni czas dodawania: %.4f ms%n", sredniCzas);
        }

        double sredniCzas = sumaCzasow / (double) liczbaPowtorzen / 1_000_000.0;
        System.out.printf("Średni czas dodawania: %.4f ms%n", sredniCzas);

        Collection<Object> kolekcja = utworzPustaKolekcje(wybranaKolekcja);

        long start = System.nanoTime();
        for (Object element : dane) {
            kolekcja.add(element);
        }

        long end = System.nanoTime();
        long czasDodawania = end - start;

        System.out.println();
        System.out.println("Dane zostały dodane do kolekcji.");
        System.out.println("Liczba elementów: " + liczbaWlasna);
        System.out.println("Czas dodawania: " + (czasDodawania / 1_000_000.0) + " ms");

        System.out.println();
        System.out.println("Przykładowe dane:");
        dane.stream().limit(5).forEach(System.out::println);

        System.out.println();
        System.out.println("Wprowadź wartość zgodnie z formatem dla typu: " + wybranyTyp);
        wypiszPrzykladFormatu(wybranyTyp);
        System.out.print("Wartość: ");

        String wartoscStr = scanner.nextLine();
        Optional<Object> wartoscOpt = parsowanie(wartoscStr, wybranyTyp);

        if (wartoscOpt.isPresent()) {
            Object wartosc = wartoscOpt.get();
            long startSzukanie = System.nanoTime();
            boolean znaleziono = kolekcja.contains(wartosc);
            long endSzukanie = System.nanoTime();
            czasWyszukiwania = endSzukanie - startSzukanie;
            System.out.println("Czy element istnieje? " + (znaleziono ? "Tak" : "Nie"));
            System.out.printf("Czas wyszukiwania (contains): %.4f ms%n", czasWyszukiwania / 1_000_000.0);
        } else {
            System.out.println("Błąd: niepoprawna wartość do wyszukiwania.");
        }
        if (kolekcja instanceof List<?>) {
            List<?> lista = (List<?>) kolekcja;

            System.out.println();
            System.out.println("Test: odczyt elementu po indeksie");

            System.out.println("Podaj indeks odczytu (od 0 do " + (lista.size() - 1) + "): ");
            int indeks = scanner.nextInt();
            scanner.nextLine();

            if (indeks < 0 || indeks >= lista.size()) {
                System.out.println("Błąd: podany indeks jest poza zakresem listy.");
            } else {
                long startOdczyt = System.nanoTime();
                Object element = lista.get(indeks);
                long endOdczyt = System.nanoTime();

                System.out.println("Odczytany element: " + element.toString());
                System.out.println("Czas odczytu: " + (endOdczyt - startOdczyt) / 1_000_000.0 + " ms");
            }
        } else {
            System.out.println();
            System.out.println("Ten typ kolekcji nie wspiera odczytu po indeksie. Wciśnij enter.");
            scanner.nextLine();
        }

        Optional<Object> doUsunieciaOpt;
        System.out.println("\nWprowadź wartość do usunięcia zgodnie z formatem dla typu: " + wybranyTyp);
        wypiszPrzykladFormatu(wybranyTyp);
        System.out.print("Wartość do usunięcia: ");
        String doUsunieciaStr = scanner.nextLine();
        doUsunieciaOpt = parsowanie(doUsunieciaStr, wybranyTyp);

        if (doUsunieciaOpt.isPresent()) {
            Object doUsuniecia = doUsunieciaOpt.get();
            boolean czyZawiera = kolekcja.contains(doUsuniecia);
            System.out.println("Czy kolekcja zawiera element do usunięcia? " + (czyZawiera ? "Tak" : "Nie"));

            long startUsuwanie = System.nanoTime();
            int licznik = 0;
            while (kolekcja.remove(doUsuniecia)) {
                licznik++;
            }
            long endUsuwanie = System.nanoTime();
            czasUsuwania = endUsuwanie - startUsuwanie;

            System.out.println("Usunięto wystąpień: " + licznik);
            System.out.printf("Czas usuwania (remove): %.4f ms%n", czasUsuwania / 1_000_000.0);
        } else {
            System.out.println("Nie można kontynuować — błąd w danych do usunięcia.");
            return;
        }

        Object szukana = wartoscOpt.orElse(dane.get(0));
        Object doUsuniecia = doUsunieciaOpt.orElse(dane.get(1));

        Test tester = new DomyslnyTester();
        TestWynik wynik = tester.wykonajTest(
                wybranyTyp,
                wybranaKolekcja,
                dane,
                liczbaPowtorzen,
                szukana,
                doUsuniecia
        );
        if (prezentacja == Prezentacja.CSV || prezentacja == Prezentacja.OBIE) {
            try (FileWriter writer = new FileWriter("wyniki_testu.csv", true)) {
                writer.append(naglowekCsv());
                writer.append(String.format("%s;%s;%d;%.4f;%.4f;%.4f\n",
                        wynik.typDanych,
                        wynik.typKolekcji,
                        wynik.liczbaElementow,
                        wynik.czasDodawania,
                        wynik.czasWyszukiwania,
                        wynik.czasUsuwania));
                System.out.println("Wyniki zostały zapisane do pliku wyniki_testu.csv");
            } catch (IOException e) {
                System.err.println("Błąd podczas zapisu do pliku CSV: " + e.getMessage());
            }
        }
        if (prezentacja == Prezentacja.KONSOLA || prezentacja == Prezentacja.OBIE) {
            System.out.println("Wynik testu automatycznego:");
            System.out.println(wynik);
        }

        scanner.close();
    }

    public static Object parsujWartosc(String input, TypDanych typ) {
        return switch (typ) {
            case INTEGER -> Integer.parseInt(input);
            case DOUBLE -> Double.parseDouble(input);
            case OBIEKTY_KLASY_PERSON -> {
                String[] parts = input.split(";");
                if (parts.length != 2)
                    throw new IllegalArgumentException("Nieprawidłowy format! Oczekiwano: Imię;Rok");
                yield new Person(Integer.parseInt(parts[1]), parts[0]);
            }
            case OBIEKTY_KLASY_MYCOLOR -> {
                String[] parts = input.split(";");
                if (parts.length != 3)
                    throw new IllegalArgumentException("Nieprawidłowy format! Oczekiwano: R;G;B");
                yield new MyColor(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }
            case OBIEKTY_KLASY_KSIAZKA -> {
                String[] parts = input.split(";");
                if (parts.length != 3)
                    throw new IllegalArgumentException("Nieprawidłowy format! Oczekiwano: Tytul;Autor;Rok");
                yield new Ksiazka(parts[0], parts[1], Integer.parseInt(parts[2]));
            }
            case OBIEKTY_KLASY_SAMOCHOD -> {
                String[] parts = input.split(";");
                if (parts.length != 3)
                    throw new IllegalArgumentException("Nieprawidłowy format! Oczekiwano: Marka;Kolor;Rok produkcji");
                yield new Samochod(parts[0], parts[1], Integer.parseInt(parts[2]));
            }
        };
    }

    public static GeneratorDanych<?> wybierzGenerator(TypDanych typ) {
        return switch (typ) {
            case INTEGER -> new GeneratorInteger();
            case DOUBLE -> new GeneratorDouble();
            case OBIEKTY_KLASY_PERSON -> new GeneratorPerson();
            case OBIEKTY_KLASY_MYCOLOR -> new GeneratorMyColor();
            case OBIEKTY_KLASY_KSIAZKA -> new GeneratorKsiazka();
            case OBIEKTY_KLASY_SAMOCHOD -> new GeneratorSamochod();
        };
    }

    public static void wypiszPrzykladFormatu(TypDanych typ) {
        System.out.println("Przykład formatu:");
        switch (typ) {
            case INTEGER -> System.out.println("123");
            case DOUBLE -> System.out.println("123.123");
            case OBIEKTY_KLASY_PERSON -> System.out.println("Imię;Rok");
            case OBIEKTY_KLASY_MYCOLOR -> System.out.println("123;123;123");
            case OBIEKTY_KLASY_KSIAZKA -> System.out.println("Tytul;Nazwisko autora;Rok wydania");
            case OBIEKTY_KLASY_SAMOCHOD -> System.out.println("Marka;Kolor;Rok produkcji");
        }
    }

    public static Collection<Object> utworzPustaKolekcje(Kolekcja typ) {
        return switch (typ) {
            case ARRAY_LIST -> new ArrayList<>();
            case LINKED_LIST -> new LinkedList<>();
            case HASH_SET -> new HashSet<>();
            case TREE_SET -> new TreeSet<>(Comparator.comparing(Object::toString));
        };
    }

    public static String naglowekCsv() {
        return "Typ danych;Kolekcja;Liczba elementów;Czas dodawania (ms);Czas wyszukiwania (ms);Czas usuwania (ms)\n";
    }

    public static Optional<Object> parsowanie(String input, TypDanych typ) {
        try {
            return Optional.of(parsujWartosc(input, typ));
        } catch (Exception e) {
            System.err.println("Błąd (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            return Optional.empty();
        }
    }

    public static <T extends Enum<T>> T wybierzEnum(Scanner scanner, Class<T> enumClass, String komunikat) {
        System.out.println(komunikat);
        T[] wartosci = enumClass.getEnumConstants();
        for (int i = 0; i < wartosci.length; i++) {
            System.out.printf(" %d. %s%n", i + 1, wartosci[i].name());
        }
        System.out.printf("Wybierz opcję od 1 do %d: ", wartosci.length);
        int wybor = scanner.nextInt();
        if (wybor < 1 || wybor > wartosci.length) {
            throw new IllegalArgumentException("Nieprawidłowy wybór.");
        }
        return wartosci[wybor - 1];
    }

    interface GeneratorDanych<T> {
        List<T> generuj(int ile);
    }

    interface Test {
        TestWynik wykonajTest(
                TypDanych typDanych,
                Kolekcja typKolekcji,
                List<?> dane,
                int liczbaPowtorzen,
                Object szukanaWartosc,
                Object wartoscDoUsuniecia
        );
    }

    static class TestWynik {
        TypDanych typDanych;
        Kolekcja typKolekcji;
        int liczbaElementow;
        double czasDodawania;
        double czasWyszukiwania;
        double czasUsuwania;

        public TestWynik(TypDanych typDanych, Kolekcja typKolekcji, int liczbaElementow,
                         double czasDodawania, double czasWyszukiwania, double czasUsuwania) {
            this.typDanych = typDanych;
            this.typKolekcji = typKolekcji;
            this.liczbaElementow = liczbaElementow;
            this.czasDodawania = czasDodawania;
            this.czasWyszukiwania = czasWyszukiwania;
            this.czasUsuwania = czasUsuwania;
        }

        @Override
        public String toString() {
            return "Typ danych: " + typDanych + ", Kolekcja: " + typKolekcji +
                    ", Liczba elementów: " + liczbaElementow +
                    ", Dodawanie: " + czasDodawania + " ms" +
                    ", Wyszukiwanie: " + czasWyszukiwania + " ms" +
                    ", Usuwanie: " + czasUsuwania + " ms";
        }
    }
    static class DomyslnyTester implements Test {
        public TestWynik wykonajTest(
                TypDanych typDanych,
                Kolekcja typKolekcji,
                List<?> dane,
                int liczbaPowtorzen,
                Object szukanaWartosc,
                Object wartoscDoUsuniecia
        )

        {
            Collection<Object> kolekcja = CollectionsPerformanceTester.utworzPustaKolekcje(typKolekcji);
            long sumaDodawania = 0;

            for (int i = 0; i < liczbaPowtorzen; i++) {
                Object element = dane.get(i % dane.size());
                long start = System.nanoTime();
                kolekcja.add(element);
                long end = System.nanoTime();
                sumaDodawania += (end - start);
            }

            for (Object element : dane) {
                kolekcja.add(element);
            }

            long startSzukanie = System.nanoTime();
            kolekcja.contains(szukanaWartosc);
            long endSzukanie = System.nanoTime();

            long startUsuwanie = System.nanoTime();
            while (kolekcja.remove(wartoscDoUsuniecia)) {

            }
            long endUsuwanie = System.nanoTime();

            return new TestWynik(
                    typDanych,
                    typKolekcji,
                    dane.size(),
                    sumaDodawania / 1_000_000.0,
                    (endSzukanie - startSzukanie) / 1_000_000.0,
                    (endUsuwanie - startUsuwanie) / 1_000_000.0
            );
        }
    }

    enum TypDanych {
        INTEGER,
        DOUBLE,
        OBIEKTY_KLASY_PERSON,
        OBIEKTY_KLASY_MYCOLOR,
        OBIEKTY_KLASY_KSIAZKA,
        OBIEKTY_KLASY_SAMOCHOD;
    }

    enum Liczba {
        STO(100),
        PIECSET(500),
        TYSIAC(1000),
        DZIESIEC_TYSIECY(10000),
        WLASNA(0);

        private final int liczba;

        Liczba(int liczba) {
            this.liczba = liczba;
        }

        public int getLiczba() {
            return liczba;
        }
    }

    enum Kolekcja {
        ARRAY_LIST(1),
        LINKED_LIST(2),
        HASH_SET(3),
        TREE_SET(4);

        final int kolekcja;

        Kolekcja(int kolekcja) {
            this.kolekcja = kolekcja;
        }
    }

    enum Prezentacja {
        KONSOLA,
        CSV,
        OBIE
    }

    static class GeneratorInteger implements GeneratorDanych<Integer> {
        public List<Integer> generuj(int ile) {
            Random r = new Random();
            return r.ints(ile, 0, 10000).boxed().collect(Collectors.toList());
        }
    }

    static class GeneratorDouble implements GeneratorDanych<Double> {
        public List<Double> generuj(int ile) {
            Random r = new Random();
            return r.doubles(ile, 0.0, 10000.0).boxed().collect(Collectors.toList());
        }
    }

    static class GeneratorPerson implements GeneratorDanych<Person> {
        private static final String[] IMIONA = {"Jan", "Piotr", "Agnieszka", "Joanna", "Szymon", "Wiktoria","Anton"};
        public List<Person> generuj(int ile) {
            Random r = new Random();
            return IntStream.range(0, ile).mapToObj(i -> new Person(r.nextInt(1950, 2024), IMIONA[r.nextInt(IMIONA.length)])).collect(Collectors.toList());
        }
    }

    static class GeneratorMyColor implements GeneratorDanych<MyColor> {
        public List<MyColor> generuj(int ile) {
            Random r = new Random();
            return IntStream.range(0, ile).mapToObj(i -> new MyColor(r.nextInt(256), r.nextInt(256), r.nextInt(256))).collect(Collectors.toList());
        }
    }

    static class GeneratorKsiazka implements GeneratorDanych<Ksiazka> {
        private static final String[] TYTULY = {"Makbet", "Lalka", "Hobbit", "Rok 1984", "Zdążyć przed Panem Bogiem"};
        private static final String[] AUTORZY = {"Szekspir", "Prus", "Tolkien", "Orwell","Krall"};
        public List<Ksiazka> generuj(int ile) {
            Random r = new Random();
            return IntStream.range(0, ile).mapToObj(i -> new Ksiazka(TYTULY[r.nextInt(TYTULY.length)], AUTORZY[r.nextInt(AUTORZY.length)], r.nextInt(1900, 2024))).collect(Collectors.toList());
        }
    }

    static class GeneratorSamochod implements GeneratorDanych<Samochod> {
        private static final String[] MARKI = {"Toyota", "Ford", "BMW", "Audi", "Porsche"};
        private static final String[] KOLORY = {"Czerwony", "Niebieski", "Czarny", "Biały", "Srebrny"};

        public List<Samochod> generuj(int ile) {
            Random r = new Random();
            return IntStream.range(0, ile).mapToObj(i -> new Samochod(
                    MARKI[r.nextInt(MARKI.length)],
                    KOLORY[r.nextInt(KOLORY.length)],
                    r.nextInt(1990, 2025)
            )).collect(Collectors.toList());
        }
    }

    static class Person {
        int rokUrodzenia;
        String imie;

        public Person(int rokUrodzenia, String imie) {
            this.rokUrodzenia = rokUrodzenia;
            this.imie = imie;
        }

        public String toString() {
            return "Imię: " + imie + ", Rok: " + rokUrodzenia;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return rokUrodzenia == person.rokUrodzenia && Objects.equals(imie, person.imie);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rokUrodzenia, imie);
        }
    }

    static class MyColor {
        int r;
        int g;
        int b;
        int sum;

        public MyColor(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.sum = r + g + b;
        }

        public String toString() {
            return "RGB(" + r + ", " + g + ", " + b + "), SUM: " + sum;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            MyColor other = (MyColor) obj;
            return r == other.r && g == other.g && b == other.b;
        }

        @Override
        public int hashCode() {
            return Objects.hash(r, g, b);
        }
    }

    static class Ksiazka {
        String tytul;
        String autor;
        int rok;

        public Ksiazka(String tytul, String autor, int rok) {
            this.tytul = tytul;
            this.autor = autor;
            this.rok = rok;
        }

        public String toString() {
            return tytul + " - " + autor + " (" + rok + ")";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Ksiazka ksiazka = (Ksiazka) obj;
            return rok == ksiazka.rok &&
                    Objects.equals(tytul, ksiazka.tytul) &&
                    Objects.equals(autor, ksiazka.autor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tytul, autor, rok);
        }
    }

    static class Samochod {
        String marka;
        String kolor;
        int rokProdukcji;

        public Samochod(String marka, String kolor, int rokProdukcji) {
            this.marka = marka;
            this.kolor = kolor;
            this.rokProdukcji = rokProdukcji;
        }

        @Override
        public String toString() {
            return marka + " / Kolor: " + kolor + " / Rok produkcji: " + rokProdukcji;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Samochod samochod = (Samochod) obj;
            return rokProdukcji == samochod.rokProdukcji &&
                    Objects.equals(marka, samochod.marka) &&
                    Objects.equals(kolor, samochod.kolor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(marka, kolor, rokProdukcji);
        }
    }
}






