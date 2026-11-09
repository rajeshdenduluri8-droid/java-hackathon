import java.util.List;
import java.util.Map;

/** Builds the HTML for every page (server-side rendering). */
public class HtmlPages {

    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static final String CSS = """
        *{box-sizing:border-box}
        body{margin:0;font-family:"Segoe UI",Roboto,Arial,sans-serif;background:#f4f6fb;color:#222}
        .navbar{display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;background:#1f3a68;color:#fff;padding:12px 28px}
        .brand{font-size:1.25rem;font-weight:600}
        .navbar nav a{color:#cdd8ee;text-decoration:none;margin-left:14px;padding:6px 10px;border-radius:6px}
        .navbar nav a:hover,.navbar nav a.active{background:rgba(255,255,255,.15);color:#fff}
        .navbar nav a.add{background:#2e8b57;color:#fff}
        .container{max-width:1000px;margin:28px auto;padding:0 16px}
        .container.narrow{max-width:520px}
        h1{margin-top:0}
        .alert{padding:10px 14px;border-radius:6px;margin-bottom:14px;background:#e3f6e8;color:#1b6b36}
        .search{display:flex;gap:8px;margin-bottom:16px;align-items:center}
        .search input{flex:1;padding:10px;border:1px solid #c5ccdb;border-radius:6px}
        button,.btn{padding:8px 14px;border:1px solid #c5ccdb;background:#fff;border-radius:6px;cursor:pointer;text-decoration:none;color:#222;font-size:.9rem;display:inline-block}
        button:hover,.btn:hover{background:#eef1f8}
        .btn.primary{background:#1f3a68;color:#fff;border-color:#1f3a68}
        .btn.danger{color:#b02a2a;border-color:#e0b4b4}
        .btn.small{padding:5px 10px}
        table{width:100%;border-collapse:collapse;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 1px 4px rgba(0,0,0,.08)}
        th,td{padding:12px;text-align:left;border-bottom:1px solid #eceff5}
        th{background:#eef1f8;font-size:.85rem;text-transform:uppercase;color:#556}
        td form{margin:0;display:inline}
        .actions{white-space:nowrap}
        .star{border:none;background:none;font-size:1.4rem;color:#aaa;padding:0}
        .star.on{color:#f5b301}
        .star:hover{background:none;color:#f5b301}
        .badge{padding:3px 10px;border-radius:12px;font-size:.8rem}
        .badge.Personal{background:#e6f0ff;color:#1d4ea0}
        .badge.Professional{background:#fff0dc;color:#9a5a00}
        .empty{text-align:center;color:#667;padding:40px 0}
        .card{background:#fff;padding:24px;border-radius:8px;box-shadow:0 1px 4px rgba(0,0,0,.08)}
        .card label.t{display:block;margin:14px 0 4px;font-weight:600}
        .card input[type=text],.card input[type=email]{width:100%;padding:10px;border:1px solid #c5ccdb;border-radius:6px}
        .radios{display:flex;gap:20px}
        .err{color:#b02a2a;font-size:.85rem;display:block;margin-top:4px}
        .form-actions{margin-top:22px;display:flex;gap:10px}
        """;

    private static String layout(String title, String active, String body) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>" + esc(title) + " | Contact Manager</title><style>" + CSS + "</style></head><body>"
                + "<header class=\"navbar\"><div class=\"brand\">📒 Contact Manager</div><nav>"
                + "<a href=\"/contacts\"" + cls(active.equals("all")) + ">All Contacts</a>"
                + "<a href=\"/favourites\"" + cls(active.equals("favourites")) + ">★ Favourites</a>"
                + "<a href=\"/add\" class=\"add\">+ Add Contact</a></nav></header>"
                + "<main class=\"container" + (active.equals("form") ? " narrow" : "") + "\">" + body + "</main>"
                + "</body></html>";
    }

    private static String cls(boolean active) {
        return active ? " class=\"active\"" : "";
    }

    /** List page used for both "All Contacts" and "Favourites". */
    public static String listPage(List<Contact> contacts, boolean favouritesView, String q, String msg) {
        String view = favouritesView ? "favourites" : "all";
        String action = favouritesView ? "/favourites" : "/contacts";
        boolean searching = q != null && !q.isBlank();
        StringBuilder b = new StringBuilder();
        b.append("<h1>").append(favouritesView ? "★ Favourite Contacts" : "All Contacts").append("</h1>");
        if (msg != null && !msg.isBlank()) b.append("<div class=\"alert\">").append(esc(msg)).append("</div>");

        b.append("<form class=\"search\" method=\"get\" action=\"").append(action).append("\">")
         .append("<input type=\"text\" name=\"q\" value=\"").append(esc(q))
         .append("\" placeholder=\"Search by name or phone number...\">")
         .append("<button type=\"submit\">Search</button>");
        if (searching) b.append("<a href=\"").append(action).append("\">Clear</a>");
        b.append("</form>");

        if (contacts.isEmpty()) {
            String text = searching ? "No contacts match your search."
                    : favouritesView ? "No favourite contacts yet. Click the ☆ next to a contact to add one."
                    : "No contacts yet. Click '+ Add Contact' to create your first one.";
            b.append("<p class=\"empty\">").append(esc(text)).append("</p>");
            return layout(favouritesView ? "Favourites" : "Contacts", view, b.toString());
        }

        b.append("<table><thead><tr><th>★</th><th>Name</th><th>Phone</th><th>Email</th><th>Category</th><th>Actions</th></tr></thead><tbody>");
        for (Contact c : contacts) {
            b.append("<tr><td>")
             .append("<form method=\"post\" action=\"/favourite\">")
             .append("<input type=\"hidden\" name=\"id\" value=\"").append(c.getId()).append("\">")
             .append("<input type=\"hidden\" name=\"from\" value=\"").append(view).append("\">")
             .append("<button type=\"submit\" class=\"star").append(c.isFavourite() ? " on" : "").append("\" title=\"")
             .append(c.isFavourite() ? "Remove from favourites" : "Add to favourites").append("\">")
             .append(c.isFavourite() ? "★" : "☆").append("</button></form></td>")
             .append("<td>").append(esc(c.getName())).append("</td>")
             .append("<td>").append(esc(c.getPhone())).append("</td>")
             .append("<td>").append(esc(c.getEmail())).append("</td>")
             .append("<td><span class=\"badge ").append(esc(c.getCategory())).append("\">")
             .append(esc(c.getCategory())).append("</span></td>")
             .append("<td class=\"actions\"><a class=\"btn small\" href=\"/edit?id=").append(c.getId()).append("\">Edit</a> ")
             .append("<form method=\"post\" action=\"/delete\" onsubmit=\"return confirm('Delete this contact?');\">")
             .append("<input type=\"hidden\" name=\"id\" value=\"").append(c.getId()).append("\">")
             .append("<input type=\"hidden\" name=\"from\" value=\"").append(view).append("\">")
             .append("<button type=\"submit\" class=\"btn small danger\">Delete</button></form></td></tr>");
        }
        b.append("</tbody></table>");
        return layout(favouritesView ? "Favourites" : "Contacts", view, b.toString());
    }

    /** Add / edit form. id = 0 means a new contact. */
    public static String formPage(int id, String name, String phone, String email, String category,
                                  Map<String, String> errors) {
        boolean isNew = id == 0;
        StringBuilder b = new StringBuilder();
        b.append("<h1>").append(isNew ? "Add Contact" : "Edit Contact").append("</h1>");
        b.append("<form class=\"card\" method=\"post\" action=\"/save\">");
        b.append("<input type=\"hidden\" name=\"id\" value=\"").append(id).append("\">");

        b.append("<label class=\"t\" for=\"name\">Name</label>")
         .append("<input type=\"text\" id=\"name\" name=\"name\" maxlength=\"100\" value=\"").append(esc(name)).append("\" autofocus>");
        error(b, errors, "name");

        b.append("<label class=\"t\" for=\"phone\">Phone Number</label>")
         .append("<input type=\"text\" id=\"phone\" name=\"phone\" maxlength=\"20\" placeholder=\"+91 98765 43210\" value=\"")
         .append(esc(phone)).append("\">");
        error(b, errors, "phone");

        b.append("<label class=\"t\" for=\"email\">Email</label>")
         .append("<input type=\"email\" id=\"email\" name=\"email\" maxlength=\"150\" value=\"").append(esc(email)).append("\">");
        error(b, errors, "email");

        b.append("<label class=\"t\">Category</label><div class=\"radios\">");
        for (String cat : new String[]{"Personal", "Professional"}) {
            b.append("<label><input type=\"radio\" name=\"category\" value=\"").append(cat).append("\"")
             .append(cat.equals(category) ? " checked" : "").append("> ").append(cat).append("</label>");
        }
        b.append("</div>");
        error(b, errors, "category");

        b.append("<div class=\"form-actions\"><button type=\"submit\" class=\"btn primary\">")
         .append(isNew ? "Add Contact" : "Save Changes")
         .append("</button><a class=\"btn\" href=\"/contacts\">Cancel</a></div></form>");
        return layout(isNew ? "Add Contact" : "Edit Contact", "form", b.toString());
    }

    private static void error(StringBuilder b, Map<String, String> errors, String field) {
        if (errors != null && errors.containsKey(field)) {
            b.append("<span class=\"err\">").append(esc(errors.get(field))).append("</span>");
        }
    }
}
