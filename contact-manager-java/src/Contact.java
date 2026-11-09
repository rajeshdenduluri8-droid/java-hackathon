/** A single address-book entry. */
public class Contact {
    private int id;
    private String name;
    private String phone;
    private String email;
    private String category;      // "Personal" or "Professional"
    private boolean favourite;

    public Contact(int id, String name, String phone, String email, String category, boolean favourite) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.category = category;
        this.favourite = favourite;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isFavourite() { return favourite; }
    public void setFavourite(boolean favourite) { this.favourite = favourite; }
}
