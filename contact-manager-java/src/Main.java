import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Simple Contact Online Management - plain Java web app.
 * Uses only the JDK's built-in HTTP server: no Maven, no Spring, no libraries.
 *
 * Run:  http://localhost:8080
 */
public class Main {

    private static final int PORT = 8080;
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9\\s\\-]{7,15}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static final ContactStore store = new ContactStore("contacts.txt");

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", Main::handle);
        server.start();
        System.out.println("Contact Manager running at http://localhost:" + PORT);
        System.out.println("Press Ctrl+C to stop.");
    }

    // ---------------- Routing ----------------
    private static void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();
            Map<String, String> query = parse(ex.getRequestURI().getRawQuery());

            if (method.equals("GET")) {
                switch (path) {
                    case "/" -> redirect(ex, "/contacts");
                    case "/contacts" -> html(ex, HtmlPages.listPage(
                            store.findAll(query.get("q"), false), false, query.get("q"), query.get("msg")));
                    case "/favourites" -> html(ex, HtmlPages.listPage(
                            store.findAll(query.get("q"), true), true, query.get("q"), query.get("msg")));
                    case "/add" -> html(ex, HtmlPages.formPage(0, "", "", "", "Personal", null));
                    case "/edit" -> showEdit(ex, query);
                    default -> notFound(ex);
                }
            } else if (method.equals("POST")) {
                Map<String, String> form = parse(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                switch (path) {
                    case "/save" -> save(ex, form);
                    case "/delete" -> {
                        store.delete(intOf(form.get("id")));
                        redirect(ex, back(form) + "?msg=" + enc("Contact deleted."));
                    }
                    case "/favourite" -> {
                        store.toggleFavourite(intOf(form.get("id")));
                        redirect(ex, back(form));
                    }
                    default -> notFound(ex);
                }
            } else {
                send(ex, 405, "Method not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            send(ex, 500, "Something went wrong: " + HtmlPages.esc(e.getMessage()));
        }
    }

    private static void showEdit(HttpExchange ex, Map<String, String> query) throws IOException {
        Contact c = store.findById(intOf(query.get("id")));
        if (c == null) {
            redirect(ex, "/contacts?msg=" + enc("Contact not found."));
            return;
        }
        html(ex, HtmlPages.formPage(c.getId(), c.getName(), c.getPhone(), c.getEmail(), c.getCategory(), null));
    }

    private static void save(HttpExchange ex, Map<String, String> f) throws IOException {
        int id = intOf(f.get("id"));
        String name = clean(f.get("name"));
        String phone = clean(f.get("phone"));
        String email = clean(f.get("email"));
        String category = clean(f.get("category"));

        Map<String, String> errors = new LinkedHashMap<>();
        if (name.isEmpty()) errors.put("name", "Name is required");
        if (phone.isEmpty()) errors.put("phone", "Phone number is required");
        else if (!PHONE.matcher(phone).matches())
            errors.put("phone", "Enter a valid phone number (7-15 digits, optional +, spaces or dashes)");
        if (email.isEmpty()) errors.put("email", "Email is required");
        else if (!EMAIL.matcher(email).matches()) errors.put("email", "Enter a valid email address");
        if (!category.equals("Personal") && !category.equals("Professional"))
            errors.put("category", "Please select a category");

        if (!errors.isEmpty()) {
            html(ex, HtmlPages.formPage(id, name, phone, email, category, errors));
            return;
        }

        if (id == 0) {
            store.add(name, phone, email, category);
            redirect(ex, "/contacts?msg=" + enc("Contact added."));
        } else if (store.update(id, name, phone, email, category)) {
            redirect(ex, "/contacts?msg=" + enc("Contact updated."));
        } else {
            redirect(ex, "/contacts?msg=" + enc("Contact not found."));
        }
    }

    // ---------------- Helpers ----------------
    private static String back(Map<String, String> form) {
        return "favourites".equals(form.get("from")) ? "/favourites" : "/contacts";
    }

    /** Trim and strip tabs/newlines (the data file is tab separated). */
    private static String clean(String s) {
        return s == null ? "" : s.replaceAll("[\\t\\r\\n]+", " ").trim();
    }

    private static int intOf(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static Map<String, String> parse(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isEmpty()) return map;
        for (String pair : raw.split("&")) {
            int i = pair.indexOf('=');
            String k = i < 0 ? pair : pair.substring(0, i);
            String v = i < 0 ? "" : pair.substring(i + 1);
            map.put(URLDecoder.decode(k, StandardCharsets.UTF_8), URLDecoder.decode(v, StandardCharsets.UTF_8));
        }
        return map;
    }

    private static void html(HttpExchange ex, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().set("Location", location);
        ex.sendResponseHeaders(303, -1);
        ex.close();
    }

    private static void notFound(HttpExchange ex) throws IOException {
        send(ex, 404, "Page not found");
    }

    private static void send(HttpExchange ex, int code, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
