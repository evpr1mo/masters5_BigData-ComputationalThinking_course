package major_assignment2;

/**
 * MajorAssignment2.java
 *
 * Complete solution for Major Assignment 2 (CompThinking&BigData) in Java.
 *
 * - Reads a CSV created from R's nycflights13::flights
 * - Filters out rows with any NA/empty fields
 * - Implements Parts 1-5 as required:
 * Part1: counts by origin + LGA-specific queries
 * Part2: tailnum analysis, December B6 destinations, unreachable from LGA
 * Part3: reallocation to ECI and FlightScheduler class using HashSet/HashMap
 * Part4: graph analysis (2- and 3-flight reachability from EWR)
 * Part5: maximum number of flights in 2013 starting 1 Jan 2013 05:00 (beam search)
 *
 * Usage: edit CSV_FILE path in main() or pass args[0] as path.
 *
 * NOTE: this is a single-file solution for clarity. In production you might split classes.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MajorAssignment2 {

    // ---------- Helper data classes ----------
    static class Flight {
        // Fields from nycflights13::flights (subset used)
        int year, month, day;
        Integer dep_time; // numeric like 517 for 5:17am or possibly NA (we filter NA out)
        Integer arr_time;
        Integer air_time; // minutes
        String carrier;
        String tailnum;
        int flight; // flight number
        String origin;
        String dest;
        int distance; // miles

        // Derived departure/arrival LocalDateTime (calculated using dep_time and arr_time)
        // We'll compute a departure datetime assuming the hhmm integer format
        LocalDateTime departureDateTime;
        LocalDateTime arrivalDateTime;

        public Flight(int year, int month, int day, Integer dep_time, Integer arr_time, Integer air_time,
                      String carrier, String tailnum, int flight, String origin, String dest, int distance) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.dep_time = dep_time;
            this.arr_time = arr_time;
            this.air_time = air_time;
            this.carrier = carrier;
            this.tailnum = tailnum;
            this.flight = flight;
            this.origin = origin;
            this.dest = dest;
            this.distance = distance;

            if (dep_time != null) {
                this.departureDateTime = convertToDateTime(year, month, day, dep_time);
            } else {
                this.departureDateTime = null;
            }
            if (dep_time != null && air_time != null) {
                this.arrivalDateTime = this.departureDateTime.plusMinutes(air_time);
            } else if (arr_time != null) {
                // fallback if air_time exists but arrival time not consistent: convert arr_time to datetime
                this.arrivalDateTime = convertToDateTime(year, month, day, arr_time);
                // if arrival earlier than departure assume arrival next day:
                if (this.departureDateTime != null && this.arrivalDateTime.isBefore(this.departureDateTime)) {
                    this.arrivalDateTime = this.arrivalDateTime.plusDays(1);
                }
            } else {
                this.arrivalDateTime = null;
            }
        }

        // Convert hhmm int like 517 to LocalDateTime
        private static LocalDateTime convertToDateTime(int y, int m, int d, int hhmm) {
            int hh = hhmm / 100;
            int mm = hhmm % 100;
            // handle occasional invalid minutes/hours gracefully by bounding
            hh = Math.max(0, Math.min(23, hh));
            mm = Math.max(0, Math.min(59, mm));
            return LocalDateTime.of(LocalDate.of(y, m, d), LocalTime.of(hh, mm));
        }

        @Override
        public String toString() {
            return String.format("%s%d-%02d-%02d %04d %s %s->%s flight:%d dist:%d airtime:%s",
                    "", year, month, day, dep_time == null ? 0 : dep_time, carrier, origin, dest, flight,
                    distance, air_time == null ? "NA" : air_time.toString());
        }

        // Helper key: date + carrier + flight number to identify unique scheduled flight
        public String uniqueKey() {
            return String.format("%04d-%02d-%02d|%s|%d", year, month, day, carrier, flight);
        }
    }

    // ---------- CSV reading and parsing ----------
    /**
     * Read CSV into a List<Flight>, skipping any rows that contain "NA" or empty fields,
     * as required by the assignment (cancelled flights ignored).
     *
     * We parse the header to find indices allowing flexible column order.
     */
    public static List<Flight> readFlightsCSV(String csvFilePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
        String headerLine = br.readLine();
        if (headerLine == null) {
            br.close();
            throw new IOException("Empty CSV file");
        }
        String[] headers = headerLine.split(",");
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].trim().replaceAll("\"", "");
            idx.put(h, i);
        }

        // Required column names according to nycflights13::flights
        String[] required = {"year","month","day","dep_time","arr_time","air_time","carrier","tailnum","flight","origin","dest","distance"};
        for (String req : required) {
            if (!idx.containsKey(req)) {
                br.close();
                throw new IOException("CSV missing required column: " + req);
            }
        }

        List<Flight> flights = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            // naive split - assumes no commas inside fields
            String[] fields = line.split(",", -1);

            boolean hasNAorEmpty = false;
            for (String f : fields) {
                if (f == null) { hasNAorEmpty = true; break; }
                String t = f.trim().replaceAll("\"", "");
                if (t.equals("") || t.equalsIgnoreCase("NA")) { hasNAorEmpty = true; break; }
            }
            if (hasNAorEmpty) continue; // skip cancelled rows

            try {
                int year = Integer.parseInt(fields[idx.get("year")].trim().replaceAll("\"", ""));
                int month = Integer.parseInt(fields[idx.get("month")].trim().replaceAll("\"", ""));
                int day = Integer.parseInt(fields[idx.get("day")].trim().replaceAll("\"", ""));

                Integer dep_time = parseIntegerOrNull(fields[idx.get("dep_time")].trim());
                Integer arr_time = parseIntegerOrNull(fields[idx.get("arr_time")].trim());
                Integer air_time = parseIntegerOrNull(fields[idx.get("air_time")].trim());

                String carrier = fields[idx.get("carrier")].trim().replaceAll("\"", "");
                String tailnum = fields[idx.get("tailnum")].trim().replaceAll("\"", "");
                int flight = Integer.parseInt(fields[idx.get("flight")].trim().replaceAll("\"", ""));
                String origin = fields[idx.get("origin")].trim().replaceAll("\"", "");
                String dest = fields[idx.get("dest")].trim().replaceAll("\"", "");
                int distance = Integer.parseInt(fields[idx.get("distance")].trim().replaceAll("\"", ""));

                // If any of the required parsed values are null, we skip (shouldn't happen due to earlier check).
                Flight fobj = new Flight(year, month, day, dep_time, arr_time, air_time,
                        carrier, tailnum, flight, origin, dest, distance);
                flights.add(fobj);
            } catch (Exception e) {
                // Skip malformed row
                continue;
            }
        }
        br.close();
        return flights;
    }

    private static Integer parseIntegerOrNull(String s) {
        if (s == null) return null;
        s = s.trim().replaceAll("\"", "");
        if (s.equals("") || s.equalsIgnoreCase("NA")) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }

    // ---------- Part 1 ----------
    public static void part1(List<Flight> flights) {
        System.out.println("=== PART 1 ===");

        // 1. Already read in dataset earlier.
        System.out.println("Total valid (non-NA) flights read: " + flights.size());

        // 2. Count flights leaving the three airports EWR, JFK, LGA
        Map<String, Long> countsByOrigin = flights.stream()
                .collect(Collectors.groupingBy(f -> f.origin, Collectors.counting()));

        System.out.println("Counts by origin (EWR, JFK, LGA):");
        for (String o : Arrays.asList("EWR","JFK","LGA")) {
            System.out.println(o + ": " + countsByOrigin.getOrDefault(o, 0L));
        }

        // 3. Consider only flights leaving LGA and provide evidence (we'll print sample + count)
        List<Flight> flightsFromLGA = flights.stream()
                .filter(f -> f.origin.equals("LGA"))
                .collect(Collectors.toList());

        System.out.println("Number of flights leaving LGA (non-NA): " + flightsFromLGA.size());
        System.out.println("Sample 10 flights from LGA:");
        flightsFromLGA.stream().limit(10).forEach(System.out::println);

        // The questions that are locked are not printed here, as requested (we only supply code & evidence).
    }

    // ---------- Part 2 ----------
    public static void part2(List<Flight> flights) {
        System.out.println("\n=== PART 2 ===");
        // 1. Number of different tailnum (aircraft) for UA, HA, B6
        List<String> airlinesOfInterest = Arrays.asList("UA","HA","B6");
        for (String c : airlinesOfInterest) {
            Set<String> tails = flights.stream()
                    .filter(f -> f.carrier.equals(c))
                    .map(f -> f.tailnum)
                    .collect(Collectors.toSet());
            System.out.println("Airline " + c + " distinct tailnum count: " + tails.size());
            // Optionally show sample tailnums
            System.out.println("Sample tailnums: " + tails.stream().limit(10).collect(Collectors.toList()));
        }

        // 3. B6 (JetBlue) destinations in December
        List<Flight> b6Dec = flights.stream()
                .filter(f -> f.carrier.equals("B6") && f.month == 12)
                .collect(Collectors.toList());
        Set<String> b6DecDests = b6Dec.stream().map(f -> f.dest).collect(Collectors.toSet());
        System.out.println("B6 December destinations (count " + b6DecDests.size() + "): " + b6DecDests);

        // 5. Airports not reachable from LGA (meaning: there is no flight with origin LGA and dest = that airport in dataset)
        // Build set of all airports and set of LGA reachable
        Set<String> allAirports = flights.stream()
                .flatMap(f -> Arrays.stream(new String[]{f.origin, f.dest}))
                .collect(Collectors.toSet());
        Set<String> reachableFromLGA = flights.stream()
                .filter(f -> f.origin.equals("LGA"))
                .map(f -> f.dest)
                .collect(Collectors.toSet());

        Set<String> notReachableFromLGA = new HashSet<>(allAirports);
        notReachableFromLGA.removeAll(reachableFromLGA);
        // Remove LGA itself (we're interested in other airports)
        notReachableFromLGA.remove("LGA");

        System.out.println("Airports that appear in dataset but are NOT reached directly from LGA (count " + notReachableFromLGA.size() + "):");
        System.out.println(notReachableFromLGA.stream().limit(50).collect(Collectors.toList()));
    }

    // ---------- Part 3: Re-allocation scenario + FlightScheduler class ----------
    public static void part3(List<Flight> flights) {
        System.out.println("\n=== PART 3 ===");
        // Airports with runways:
        Map<String, Integer> runways = new HashMap<>();
        runways.put("EWR", 3);
        runways.put("LGA", 2);
        runways.put("JFK", 4);
        runways.put("ECI", 4); // new airport

        // Count total flights across EWR,LGA,JFK (we are only allowed to cancel flights in dataset)
        Map<String, Long> originCounts = flights.stream()
                .filter(f -> runways.containsKey(f.origin) && !f.origin.equals("ECI"))
                .collect(Collectors.groupingBy(f -> f.origin, Collectors.counting()));

        long ewr = originCounts.getOrDefault("EWR", 0L);
        long lga = originCounts.getOrDefault("LGA", 0L);
        long jfk = originCounts.getOrDefault("JFK", 0L);
        long total = ewr + lga + jfk;

        System.out.println("EWR: " + ewr + ", LGA: " + lga + ", JFK: " + jfk + ", total: " + total);

        // We need to redistribute flights between the 4 airports proportional to number of runways
        int totalRunways = runways.values().stream().mapToInt(Integer::intValue).sum(); // includes ECI
        // But runways map currently includes ECI as well. However we want distribution across EWR,LGA,JFK,ECI
        totalRunways = runways.get("EWR") + runways.get("LGA") + runways.get("JFK") + runways.get("ECI");

        // target flights per airport proportional to runways
        double perRunway = (double) total / totalRunways;
        Map<String, Long> target = new HashMap<>();
        for (String a : Arrays.asList("EWR","LGA","JFK","ECI")) {
            long t = Math.round(runways.get(a) * perRunway);
            target.put(a, t);
        }

        // Since rounding may create slight mismatch, adjust ECI to absorb difference so ECI gets whole-number flights reallocated
        long allocatedTotal = target.get("EWR")+target.get("LGA")+target.get("JFK")+target.get("ECI");
        long diff = total - allocatedTotal;
        target.put("ECI", target.get("ECI") + diff);

        System.out.println("Target distribution after balancing (EWR,LGA,JFK,ECI): " + target);

        // Compute how many flights will be re-allocated *to ECI* (i.e., flights cancelled at existing airports and moved to ECI)
        long flightsToECI = 0L;
        // We will re-allocate from airports where current > target, transferring surplus to ECI until ECI reaches target
        long eciTarget = target.get("ECI");
        long currentECI = 0; // initially no flights have origin ECI in dataset
        long eciNeeded = Math.max(0L, eciTarget - currentECI);

        // Surplus available from each existing airport
        Map<String, Long> surplus = new HashMap<>();
        surplus.put("EWR", Math.max(0L, ewr - target.get("EWR")));
        surplus.put("LGA", Math.max(0L, lga - target.get("LGA")));
        surplus.put("JFK", Math.max(0L, jfk - target.get("JFK")));

        long reallocated = 0L;
        // Pull from surpluses until ECI needed satisfied
        for (String a : Arrays.asList("EWR","JFK","LGA")) {
            if (eciNeeded <= 0) break;
            long take = Math.min(surplus.getOrDefault(a, 0L), eciNeeded);
            reallocated += take;
            eciNeeded -= take;
        }

        // If still need, we may have to reallocate by moving flights from airports that are below target (impractical),
        // but since ECI counted in target distribution it should be filled by surpluses; here we simply ensure integer:
        flightsToECI = reallocated;
        System.out.println("Number of existing flights that will get re-allocated to ECI (whole number): " + flightsToECI);

        // 3. Implement FlightScheduler class + demo
        System.out.println("\n--- Demonstration of FlightScheduler ---");
        FlightScheduler scheduler = new FlightScheduler();
        // For demo, instead of passing full CSV file path we will load flights into scheduler via a method that accepts existing List<Flight>.
        // But class implements loadData(String) for file-based loading as required.
        scheduler.loadFlightsFromList(flights); // convenience method for tests
        // Mark some random flights reallocated (for demonstration pick first 3 flights)
        System.out.println("Marking first 3 flights as reallocated (demo).");
        int marks = 0;
        for (Flight f : flights.stream().limit(3).collect(Collectors.toList())) {
            scheduler.reallocate(f.day, f.month, f.year, f.carrier + "-" + f.flight); // stored key depends on implementation
            marks++;
            if (marks >= 3) break;
        }
        // Check a sample flight (should return false if reallocated or originally absent)
        Flight sample = flights.get(0);
        boolean ok = scheduler.check(sample.day, sample.month, sample.year, sample.carrier + "-" + sample.flight);
        System.out.println("Check flight " + sample.carrier + "-" + sample.flight + " on " + sample.year + "-" + sample.month + "-" + sample.day + ": OK? " + ok);
    }

    /**
     * FlightScheduler class using HashMaps/HashSets to mark reallocated flights.
     *
     * Methods:
     * - FlightScheduler()
     * - void loadData(String flightDataFile) -> loads CSV (not used in demo)
     * - void loadFlightsFromList(List<Flight>) -> convenience to load existing parsed flights
     * - void reallocate(int day, int month, int year, String flightCode) -> marks the scheduled flight as reallocated origin=ECI
     * - boolean check(int day, int month, int year, String flightCode) -> true if flight still ok, false if reallocated/cancelled
     */
    public static class FlightScheduler {
        // We'll identify flights by a string key: "YYYY-MM-DD|carrier|flightNumber"
        private Map<String, Flight> flightMap; // maps key -> Flight
        private Set<String> reallocatedSet; // set of keys that are reallocated/cancelled

        public FlightScheduler() {
            flightMap = new HashMap<>();
            reallocatedSet = new HashSet<>();
        }

        // Loads CSV file similarly to readFlightsCSV but adds to flightMap
        public void loadData(String flightDataFile) {
            try {
                List<Flight> list = readFlightsCSV(flightDataFile);
                loadFlightsFromList(list);
            } catch (IOException e) {
                System.err.println("Failed to load data: " + e.getMessage());
            }
        }

        // Convenience for demo/testing: load from already parsed list
        public void loadFlightsFromList(List<Flight> flights) {
            for (Flight f : flights) {
                String key = makeKey(f.year, f.month, f.day, f.carrier, f.flight);
                flightMap.put(key, f);
                // if the origin is already ECI treat it as reallocated/cancelled
                if ("ECI".equals(f.origin)) {
                    reallocatedSet.add(key);
                }
            }
        }

        // Mark a flight as re-allocated to ECI and update origin in map if present.
        public void reallocate(int day, int month, int year, String flightCode) {
            // flightCode expected format: "CARRIER-flightNumber" (demo); but we will support direct numeric flight if needed
            String key = makeKey(year, month, day, parseCarrierFromFlightCode(flightCode), parseFlightNumFromFlightCode(flightCode));
            reallocatedSet.add(key);
            if (flightMap.containsKey(key)) {
                Flight f = flightMap.get(key);
                f.origin = "ECI"; // update origin to ECI
            }
        }

        // Check returns true if flight is OK (not reallocated/cancelled), false otherwise
        public boolean check(int day, int month, int year, String flightCode) {
            String key = makeKey(year, month, day, parseCarrierFromFlightCode(flightCode), parseFlightNumFromFlightCode(flightCode));
            // If flight not found in map, assume cancelled -> return false
            if (!flightMap.containsKey(key)) return false;
            // If present but in reallocatedSet -> return false
            return !reallocatedSet.contains(key);
        }

        private static String makeKey(int year, int month, int day, String carrier, int flightNum) {
            return String.format("%04d-%02d-%02d|%s|%d", year, month, day, carrier, flightNum);
        }

        private static String parseCarrierFromFlightCode(String flightCode) {
            // flightCode "AA-401" -> "AA"
            if (flightCode == null) return "";
            String[] parts = flightCode.split("-");
            if (parts.length >= 2) return parts[0];
            // fallback: assume the whole string is carrier
            return flightCode;
        }

        private static int parseFlightNumFromFlightCode(String flightCode) {
            if (flightCode == null) return 0;
            String[] parts = flightCode.split("-");
            if (parts.length >= 2) {
                try { return Integer.parseInt(parts[1]); } catch (NumberFormatException e) { return 0; }
            }
            return 0;
        }
    }

    // ---------- Part 4: Graph problems ----------
    public static void part4(List<Flight> flights) {
        System.out.println("\n=== PART 4 === (Graph reachability ignoring direction)");

        // Build undirected adjacency: airport -> set of neighbor airports
        Map<String, Set<String>> adj = new HashMap<>();
        for (Flight f : flights) {
            String a = f.origin;
            String b = f.dest;
            adj.computeIfAbsent(a, k -> new HashSet<>()).add(b);
            adj.computeIfAbsent(b, k -> new HashSet<>()).add(a);
        }

        // 1. How many different airports can you reach from EWR by taking two flights?
        Set<String> afterOne = adj.getOrDefault("EWR", Collections.emptySet());
        Set<String> afterTwo = new HashSet<>();
        for (String mid : afterOne) {
            Set<String> neighbors = adj.getOrDefault(mid, Collections.emptySet());
            afterTwo.addAll(neighbors);
        }
        // remove EWR itself if present
        afterTwo.remove("EWR");
        System.out.println("Distinct airports reachable from EWR in 2 flights: " + afterTwo.size());
        // print sample subset
        System.out.println("Sample: " + afterTwo.stream().limit(20).collect(Collectors.toList()));

        // 3. How many different airports can you reach from EWR by taking three flights?
        Set<String> afterThree = new HashSet<>();
        // do BFS up to depth 3, undirected
        Set<String> visited = new HashSet<>();
        Queue<String> q = new ArrayDeque<>();
        q.add("EWR");
        visited.add("EWR");
        int depth = 0;
        // do level-order BFS but collect nodes at depth 3
        Map<String, Integer> dist = new HashMap<>();
        dist.put("EWR", 0);
        while (!q.isEmpty()) {
            String cur = q.poll();
            int dcur = dist.get(cur);
            if (dcur >= 3) continue;
            for (String nb : adj.getOrDefault(cur, Collections.emptySet())) {
                if (!dist.containsKey(nb)) {
                    dist.put(nb, dcur + 1);
                    q.add(nb);
                }
            }
        }
        for (Map.Entry<String,Integer> e : dist.entrySet()) {
            if (e.getValue() == 3) afterThree.add(e.getKey());
        }
        afterThree.remove("EWR");
        System.out.println("Distinct airports reachable from EWR in 3 flights: " + afterThree.size());
        System.out.println("Sample: " + afterThree.stream().limit(20).collect(Collectors.toList()));
    }

    // ---------- Part 5: Maximum flights in 2013 starting 2013-01-01 05:00 ----------
    /**
     * We implement a beam-search heuristic that explores multiple promising itineraries.
     * - Start at each of EWR, LGA, JFK at 2013-01-01 05:00 (inclusive)
     * - At each step, consider flights departing from current airport with departure >= current time
     * - For each candidate flight, produce a new itinerary; keep only top-K itineraries by
     * (1) number of flights taken (desc), (2) earlier current time (asc) as tie-breaker.
     * - Repeat until no itineraries can be expanded or we exceed 2013-12-31 23:59.
     * Beam width K is adjustable; larger K gives better result at cost of time/memory.
     */
    public static int part5(List<Flight> flights) {
        System.out.println("\n=== PART 5 === (Max flights in 2013 heuristic)");

        // Build map origin->sorted list of flights by departure datetime
        Map<String, List<Flight>> flightsByOrigin = new HashMap<>();
        for (Flight f : flights) {
            if (f.departureDateTime == null || f.arrivalDateTime == null) continue; // must have times
            if (f.year != 2013) continue;
            flightsByOrigin.computeIfAbsent(f.origin, k -> new ArrayList<>()).add(f);
        }
        // sort each list by departure time
        for (List<Flight> lst : flightsByOrigin.values()) {
            lst.sort(Comparator.comparing(f -> f.departureDateTime));
        }

        // beam search params
        final int BEAM_WIDTH = 200; // adjust: larger -> better result but slower/more memory
        final LocalDateTime END_OF_2013 = LocalDateTime.of(2013,12,31,23,59);

        class State {
            String airport;
            LocalDateTime time; // current time at airport (we are on ground and can board flights departing at >= time)
            int flightsTaken;
            // To prevent immediate repeats, we could store visited events, but assignment allows reusing flights if they fit times.
            // We'll store a history size only for potential debugging (not used in equality)
            // List<String> path;

            public State(String airport, LocalDateTime time, int flightsTaken/*, List<String> path*/) {
                this.airport = airport;
                this.time = time;
                this.flightsTaken = flightsTaken;
                // this.path = path;
            }
        }

        // initial states: start at each of EWR, LGA, JFK at 2013-01-01 05:00
        LocalDateTime startTime = LocalDateTime.of(2013,1,1,5,0);
        List<State> beam = new ArrayList<>();
        for (String s : Arrays.asList("EWR","LGA","JFK")) beam.add(new State(s, startTime, 0));

        int bestFlights = 0;

        // helper: get next flights from origin departing at or after time
        // We'll binary search the sorted list per origin to quickly find starting index
        for (int iter = 0; iter < 10000; iter++) { // safety upper bound
            List<State> nextBeamCandidates = new ArrayList<>();
            boolean anyExpanded = false;
            for (State st : beam) {
                // find candidate flights
                List<Flight> originList = flightsByOrigin.getOrDefault(st.airport, Collections.emptyList());
                if (originList.isEmpty()) continue;

                // binary search to find first index with departure >= st.time
                int lo = 0, hi = originList.size()-1, idx = originList.size();
                while (lo <= hi) {
                    int mid = (lo+hi)/2;
                    if (!originList.get(mid).departureDateTime.isBefore(st.time)) {
                        idx = mid;
                        hi = mid-1;
                    } else lo = mid+1;
                }
                // from idx iterate up to some limit to avoid exploding branches (e.g., consider first M options)
                int MAX_OPTIONS_PER_STATE = 30;
                int optionsTaken = 0;
                for (int i = idx; i < originList.size() && optionsTaken < MAX_OPTIONS_PER_STATE; i++) {
                    Flight f = originList.get(i);
                    // ensure arrival within 2013
                    if (f.arrivalDateTime.isAfter(END_OF_2013)) continue;
                    // new state after taking this flight
                    State ns = new State(f.dest, f.arrivalDateTime, st.flightsTaken + 1);
                    nextBeamCandidates.add(ns);
                    optionsTaken++;
                    anyExpanded = true;
                }
                // Also consider 'staying' (i.e., we could wait longer - but waiting without flying is not useful for maximizing flights)
                // So we don't add waiting-only states.
            }
            if (!anyExpanded) break;

            // Keep top BEAM_WIDTH candidates ordered by flightsTaken (desc), then earlier time (asc)
            nextBeamCandidates.sort((a,b) -> {
                if (b.flightsTaken != a.flightsTaken) return Integer.compare(b.flightsTaken, a.flightsTaken);
                return a.time.compareTo(b.time);
            });

            if (!nextBeamCandidates.isEmpty()) {
                beam = nextBeamCandidates.stream().limit(BEAM_WIDTH).collect(Collectors.toList());
                bestFlights = Math.max(bestFlights, beam.get(0).flightsTaken);
            } else break;
            // Stop if beam converged and no improvement likely
            if (bestFlights > 2000) break; // safety guard
        }

        System.out.println("Beam-search heuristic best flights count found: " + bestFlights);
        return bestFlights;
    }

    // ---------- main ----------
    public static void main(String[] args) {
     String CSV_FILE = "src/major_assignment2/flights.csv";
        if (args.length > 0) CSV_FILE = args[0];

        try {
            System.out.println("Loading CSV: " + CSV_FILE + " (this may take a moment) ...");
            List<Flight> flights = readFlightsCSV(CSV_FILE);
            System.out.println("Loaded flights: " + flights.size());

            // PART 1
            part1(flights);

            // PART 2
            part2(flights);

            // PART 3
            part3(flights);

            // PART 4
            part4(flights);

            // PART 5 (heuristic)
            int maxFlights = part5(flights);
            System.out.println("Heuristic maximum flights in 2013 (approx): " + maxFlights);

        } catch (IOException e) {
            System.err.println("Failed to load/parse CSV file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}