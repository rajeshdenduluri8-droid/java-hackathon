import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Keeps all contacts in memory and saves them to a tab-separated text file
 * (contacts.txt) so data survives restarts. No database needed.
 */
public class ContactStore {
    private final Path file;
    private final List<Contact> contacts = new ArrayList<>();
    private int nextId = 1;

    public ContactStore(String fileName) {
        this.file = Paths.get(fileName);
        load();
    }

    // ---------- CRUD ----------
    public synchronized List<Contact> findAll(String query, boolean onlyFavourites) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return contacts.stream()
                .filter(c -> !onlyFavourites || c.isFavourite())
                .filter(c -> q.isEmpty()
                        || c.getName().toLowerCase().contains(q)
                        || c.getPhone().contains(q))
                .sorted(Comparator.comparing(c -> c.getName().toLowerCase()))
                .collect(Collectors.toList());
    }

    public synchronized Contact findById(int id) {
        for (Contact c : contacts) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    public synchronized void add(String name, String phone, String email, String category) {
        contacts.add(new Contact(nextId++, name, phone, email, category, false));
        save();
    }

    public synchronized boolean update(int id, String name, String phone, String email, String category) {
        Contact c = findById(id);
        if (c == null) return false;
        c.setName(name);
        c.setPhone(phone);
        c.setEmail(email);
        c.setCategory(category);
        save();
        return true;
    }

    public synchronized boolean delete(int id) {
        boolean removed = contacts.removeIf(c -> c.getId() == id);
        if (removed) save();
        return removed;
    }

    public synchronized boolean toggleFavourite(int id) {
        Contact c = findById(id);
        if (c == null) return false;
        c.setFavourite(!c.isFavourite());
        save();
        return true;
    }

    // ---------- File persistence ----------
    private void load() {
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] p = line.split("\t", -1);
                if (p.length < 6) continue;
                int id = Integer.parseInt(p[0]);
                contacts.add(new Contact(id, p[1], p[2], p[3], p[4], Boolean.parseBoolean(p[5])));
                nextId = Math.max(nextId, id + 1);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Could not read " + file + ": " + e.getMessage());
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Contact c : contacts) {
            lines.add(String.join("\t", String.valueOf(c.getId()), c.getName(), c.getPhone(),
                    c.getEmail(), c.getCategory(), String.valueOf(c.isFavourite())));
        }
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Could not save " + file + ": " + e.getMessage());
        }
    }
}
