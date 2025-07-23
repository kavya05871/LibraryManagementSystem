import java.io.*;
import java.util.*;

abstract class User {
    protected String email, password, name;

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public String getEmail() { 
        return email; 
        }
    public String getPassword() { 
        return password; 
        }
    public String getName() { 
        return name; 
        }
}

class Admin extends User {
    public Admin(String email, String password, String name) {
        super(email, password, name);
    }
}

class Borrower extends User {
    private List<Book> borrowedBooks = new ArrayList<>();

    public Borrower(String email, String password, String name) {
        super(email, password, name);
    }

    public List<Book> getBorrowedBooks() {
         return borrowedBooks; }
}

class Book {
    public String id, title, author;
    public int quantity;
    public double cost;

    public Book(String id, String title, String author, int quantity, double cost) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.quantity = quantity;
        this.cost = cost;
    }

    public String getId() { 
        return id; 
        }
    public String getTitle() {
         return title;
          }
    public String getAuthor() {
         return author; 
         }
    public int getQuantity() { 
        return quantity;
         }
    public double getCost() { 
        return cost;
         }
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
        }

    public String toString() {
        return String.format("ID: %s | Title: %s | Author: %s | Qty: %d | %.2f",
                id, title, author, quantity, cost);
    }
}

class AuthService {
    public static User login(String email, String password, List<User> users) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
}

class BookService {
    public static void loadBooksFromFile(String filePath, List<Book> books, Map<String, Book> bookMap) {
        try (Scanner fs = new Scanner(new File(filePath))) {
            while (fs.hasNextLine()) {
                String line = fs.nextLine().trim();
                if (line.isEmpty() ) 
                continue;
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    Book book = new Book(parts[0], parts[1], parts[2],
                            Integer.parseInt(parts[3]), Double.parseDouble(parts[4]));
                    books.add(book);
                    bookMap.put(book.getId(), book);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to load books.txt: " + e.getMessage());
        }
    }

    public static void saveBooksToFile(String filePath, List<Book> books) {
        try (PrintWriter w = new PrintWriter(new FileWriter(filePath))) {
            for (Book book : books) {
                w.printf("%s,%s,%s,%d,%.2f%n", book.getId(), book.getTitle(),
                        book.getAuthor(), book.getQuantity(), book.getCost());
            }
        } catch (Exception e) {
            System.out.println("Error saving to books.txt: " + e.getMessage());
        }
    }

    public static void addBook(List<Book> books, Map<String, Book> bookMap, Scanner sc) {
        System.out.print("Enter ID: ");
        String id = sc.nextLine();
        System.out.print("Enter Title: ");
        String title = sc.nextLine();
        System.out.print("Enter Author: ");
        String author = sc.nextLine();
        System.out.print("Enter Quantity: ");
        int qty = Integer.parseInt(sc.nextLine());
        System.out.print("Enter Cost: ");
        double cost = Double.parseDouble(sc.nextLine());

        Book newBook = new Book(id, title, author, qty, cost);
        books.add(newBook);
        bookMap.put(id, newBook);
        saveBooksToFile("books.txt", books);
        System.out.println("Book added successfully.");
    }

    public static void viewBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books available.");
            return;
        }
        System.out.printf("%-10s %-30s %-30s %-8s %-8s%n", "ID", "Title", "Author", "Qty", "Cost");
        for (Book b : books) {
            System.out.printf("%-10s %-30s %-30s %-8d %.2f%n", b.getId(), b.getTitle(),
                    b.getAuthor(), b.getQuantity(), b.getCost());
        }
    }

    public static void editOrDeleteBook(List<Book> books, Map<String, Book> bookMap, Scanner sc) {
        System.out.print("Enter ID of book to edit/delete: ");
        String id = sc.nextLine();
        Book target = bookMap.get(id);
        if (target == null) {
            System.out.println("Book not found.");
            return;
        }

        System.out.println("1. Edit\n2. Delete");
        int opt = Integer.parseInt(sc.nextLine());
        if (opt == 1) {
            System.out.print("New Title: "); 
            target.title = sc.nextLine();
            System.out.print("New Author: ");
            target.author = sc.nextLine();
            System.out.print("New Qty: "); 
            target.quantity = Integer.parseInt(sc.nextLine());
            System.out.print("New Cost: "); 
            target.cost = Double.parseDouble(sc.nextLine());
            System.out.println("Book updated.");
        } else if (opt == 2) {
            books.remove(target);
            bookMap.remove(id);
            System.out.println("Book deleted.");
        }
        saveBooksToFile("books.txt", books);
    }

    public static void searchBook(Map<String, Book> bookMap, Scanner sc) {
        System.out.print("Enter Book ID: ");
        String id = sc.nextLine();
        Book book = bookMap.get(id);
        if (book != null) {
            System.out.println("Book found: " + book);
        } else {
            System.out.println("Book not found.");
        }
    }
}

class BorrowService {
    public static void borrowBook(Borrower b, Map<String, Book> bookMap, Scanner sc) {
        if (b.getBorrowedBooks().size() >= 3) {
            System.out.println("You already borrowed 3 books.");
            return;
        }
        System.out.print("Enter Book ID to borrow: ");
        String id = sc.nextLine();
        Book book = bookMap.get(id);
        if (book != null) {
            if (book.getQuantity() > 0) {
                b.getBorrowedBooks().add(book);
                book.setQuantity(book.getQuantity() - 1);
                saveBorrowedRecord(b, book);
                System.out.println("Book borrowed successfully.");
            } else {
                System.out.println("Book out of stock.");
            }
        } else {
            System.out.println("Book not found.");
        }
    }

    public static void returnBook(Borrower borrower, Map<String, Book> bookMap, Scanner sc) {
        List<Book> borrowed = borrower.getBorrowedBooks();
        if (borrowed.isEmpty()) {
            System.out.println("No borrowed books.");
            return;
        }
        for (int i = 0; i < borrowed.size(); i++) {
            System.out.println((i + 1) + ". " + borrowed.get(i));
        }
        System.out.print("Enter book number to return: ");
        int choice = Integer.parseInt(sc.nextLine());
        if (choice < 1 || choice > borrowed.size()) return;
        Book returning = borrowed.remove(choice - 1);
        Book original = bookMap.get(returning.getId());
        if (original != null) original.setQuantity(original.getQuantity() + 1);
        System.out.println("Returned successfully.");
    }

    private static void saveBorrowedRecord(Borrower b, Book book) {
        try (FileWriter writer = new FileWriter("borrowed_books.txt", true)) {
            writer.write(b.getEmail() + "," + book.getId() + "," + book.getTitle() + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Error saving borrowed record.");
        }
    }
}

class FineService {
    public static void showFineHistory(Borrower b) {
        System.out.println("No fine due.");
    }
}

class ReportService {
    public static void generateReports() {
        System.out.println("Report generated.");
    }

    public static void showBorrowedBooks(Borrower b) {
        for (Book book : b.getBorrowedBooks()) System.out.println(book);
    }
}

public class MainApp {
    static Scanner sc = new Scanner(System.in);
    static List<Book> books = new ArrayList<>();
    static Map<String, Book> bookMap = new HashMap<>();
    static List<User> users = new ArrayList<>();

    public static void main(String[] args) {
        BookService.loadBooksFromFile("books.txt", books, bookMap);
        loadUsersFromFile("users.txt", users);

        while (true) {
            System.out.println("\n=== LIBRARY SYSTEM ===");
            System.out.print("Email: ");
            String email = sc.nextLine();
            System.out.print("Password: ");
            String password = sc.nextLine();

            User user = AuthService.login(email, password, users);
            if (user instanceof Admin admin) 
                showAdminMenu(admin);
            else if (user instanceof Borrower b) 
                showBorrowerMenu(b);
            else System.out.println("Login failed.");
        }
    }

    static void loadUsersFromFile(String file, List<User> users) {
        try (Scanner s = new Scanner(new File(file))) {
            while (s.hasNextLine()) {
                String[] p = s.nextLine().split(",");
                if (p[0].equalsIgnoreCase("admin"))
                    users.add(new Admin(p[1], p[2], p[3]));
                else if (p[0].equalsIgnoreCase("borrower"))
                    users.add(new Borrower(p[1], p[2], p[3]));
            }
        } catch (Exception e) {
            System.out.println("Error loading users.txt");
        }
    }

    static void showAdminMenu(Admin a) {
        while (true) {
            System.out.println("\n1. Add Book\n2. View Books\n3. Search\n4. Edit/Delete\n5. Reports\n6. Logout");
            int ch = Integer.parseInt(sc.nextLine());
            switch (ch) {
                case 1 -> BookService.addBook(books, bookMap, sc);
                case 2 -> BookService.viewBooks(books);
                case 3 -> BookService.searchBook(bookMap, sc);
                case 4 -> BookService.editOrDeleteBook(books, bookMap, sc);
                case 5 -> ReportService.generateReports();
                case 6 -> {
                    BookService.saveBooksToFile("books.txt", books);
                    return;
                }
            }
        }
    }

    static void showBorrowerMenu(Borrower b) {
        while (true) {
            System.out.println("\n1. View\n2. Borrow\n3. Return\n4. Fines\n5. History\n6. Logout");
            int ch = Integer.parseInt(sc.nextLine());
            switch (ch) {
                case 1 -> BookService.viewBooks(books);
                case 2 -> BorrowService.borrowBook(b, bookMap, sc);
                case 3 -> BorrowService.returnBook(b, bookMap, sc);
                case 4 -> FineService.showFineHistory(b);
                case 5 -> ReportService.showBorrowedBooks(b);
                case 6 -> {
                    BookService.saveBooksToFile("books.txt", books);
                    return;
                }
            }
        }
    }
}

