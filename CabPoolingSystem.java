import java.util.*;

// Singleton Pattern
class RideManager {
    private static RideManager instance;
    private List<Ride> rides = new ArrayList<>();

    private RideManager() { }

    public static RideManager getInstance() {
        if (instance == null) {
            instance = new RideManager();
        }
        return instance;
    }

    public void addRide(Ride ride) {
        rides.add(ride);
    }

    public List<Ride> getAllRides() {
        return rides;
    }

    public Ride getRideById(int id) {
        for (Ride r : rides) {
            if (r.rideId == id) return r;
        }
        return null;
    }
}

// Factory Pattern
class RideFactory {
    private int generateRideId() {
        return new Random().nextInt(1000);
    }

    public Ride createRide(String type, User user, double distance, String source, String destination, String password, CabStrategy strategy) {
        if (type.equalsIgnoreCase("solo")) {
            return new SoloRide(generateRideId(), user, distance, source, destination, password, strategy);
        } else if (type.equalsIgnoreCase("pool")) {
            return new PoolRide(generateRideId(), user, distance, source, destination, password, strategy);
        }
        return null;
    }
}

// Observer Pattern
interface RideCreationObserver {
    void notify(String msg);
}

class NotificationService {
    private static final List<RideCreationObserver> observers = new ArrayList<>();

    public static void addObserver(RideCreationObserver observer) {
        observers.add(observer);
    }

    public static void notifyAllObservers(String message) {
        for (RideCreationObserver obs : observers) {
            obs.notify(message);
        }
    }
}

// Strategy Pattern
interface CabStrategy {
    double calculateFare(double distance);
}

class MiniCabStrategy implements CabStrategy {
    public double calculateFare(double distance) {
        return 8.0 * distance;
    }
}

class SedanCabStrategy implements CabStrategy {
    public double calculateFare(double distance) {
        return 10.0 * distance;
    }
}

// Ride base class
abstract class Ride {
    int rideId;
    User creator;
    double distance;
    String source;
    String destination;
    String password;
    CabStrategy strategy;
    String rideType;

    public Ride(int rideId, User creator, double distance, String source, String destination, String password, CabStrategy strategy, String rideType) {
        this.rideId = rideId;
        this.creator = creator;
        this.distance = distance;
        this.source = source;
        this.destination = destination;
        this.password = password;
        this.strategy = strategy;
        this.rideType = rideType;
    }

    public double calculateFare() {
        return strategy.calculateFare(distance);
    }
}

class SoloRide extends Ride {
    public SoloRide(int rideId, User creator, double distance, String source, String destination, String password, CabStrategy strategy) {
        super(rideId, creator, distance, source, destination, password, strategy, "Solo");
    }
}

class PoolRide extends Ride {
    List<User> coRiders = new ArrayList<>();
    Map<User, String> joinRequests = new HashMap<>();

    public PoolRide(int rideId, User creator, double distance, String source, String destination, String password, CabStrategy strategy) {
        super(rideId, creator, distance, source, destination, password, strategy, "Pool");
    }

    public void requestJoin(User user) {
        joinRequests.put(user, "PENDING");
        creator.notify("Join request received from " + user.name + " for Ride ID " + rideId);
    }

    public void approveRequest(User user, String action) {
        if (action.equalsIgnoreCase("approve")) {
            coRiders.add(user);
            joinRequests.put(user, "APPROVED");
            user.notify("Your request to join Ride ID " + rideId + " is approved!");
        } else {
            joinRequests.put(user, "REJECTED");
            user.notify("Your request to join Ride ID " + rideId + " is rejected.");
        }
    }

    public Map<User, String> getJoinRequests() {
        return joinRequests;
    }
}

// User class
class User implements RideCreationObserver {
    String name;
    String id;

    public User(String name, String id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public void notify(String msg) {
        System.out.println("Notification to " + name + ": " + msg);
    }
}

// Main system
public class CabPoolingSystem {
    static Scanner sc = new Scanner(System.in);
    static Map<String, User> users = new HashMap<>();
    static RideFactory rideFactory = new RideFactory();
    static RideManager rideManager = RideManager.getInstance();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n===== Smart Online Cab Pooling System =====");
            System.out.println("1. Register User");
            System.out.println("2. Book Solo Ride");
            System.out.println("3. Create Pool Ride");
            System.out.println("4. Request to Join Pool Ride");
            System.out.println("5. Approve/Reject Requests");
            System.out.println("6. View My Rides");
            System.out.println("7. Exit");
            System.out.print("Enter choice: ");
            int ch = sc.nextInt();
            sc.nextLine();

            switch (ch) {
                case 1 -> registerUser();
                case 2 -> bookRide("solo");
                case 3 -> bookRide("pool");
                case 4 -> requestJoin();
                case 5 -> approveRequests();
                case 6 -> viewRides();
                case 7 -> System.exit(0);
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    static void registerUser() {
        System.out.print("Enter Name: ");
        String name = sc.nextLine();
        System.out.print("Enter ID: ");
        String id = sc.nextLine();
        User user = new User(name, id);
        users.put(id, user);
        NotificationService.addObserver(user);
        System.out.println("User registered.");
    }

    static void bookRide(String type) {
        System.out.print("Enter User ID: ");
        String id = sc.nextLine();
        if (!users.containsKey(id)) return;

        System.out.print("Enter Distance: ");
        double distance = sc.nextDouble();
        sc.nextLine();
        System.out.print("Enter Source: ");
        String source = sc.nextLine();
        System.out.print("Enter Destination: ");
        String destination = sc.nextLine();
        System.out.print("Set Ride Password: ");
        String pwd = sc.nextLine();

        CabStrategy strategy = type.equals("solo") ? new MiniCabStrategy() : new SedanCabStrategy();
        Ride ride = rideFactory.createRide(type, users.get(id), distance, source, destination, pwd, strategy);
        rideManager.addRide(ride);
        if (type.equals("pool")) {
            NotificationService.notifyAllObservers("New Pool Ride created by " + users.get(id).name);
        }
        System.out.println(type + " ride created successfully.");
    }

    static void requestJoin() {
        System.out.print("Enter Your User ID: ");
        String userId = sc.nextLine();
        if (!users.containsKey(userId)) return;

        System.out.println("Available Pool Rides:");
        for (Ride r : rideManager.getAllRides()) {
            if (r instanceof PoolRide) {
                System.out.println("Ride ID: " + r.rideId + " | From: " + r.source + " To: " + r.destination + " | Created by: " + r.creator.name);
            }
        }

        System.out.print("Enter Ride ID to join: ");
        int rideId = sc.nextInt();
        sc.nextLine();

        Ride r = rideManager.getRideById(rideId);
        if (r instanceof PoolRide poolRide) {
            poolRide.requestJoin(users.get(userId));
        } else {
            System.out.println("Invalid Pool Ride.");
        }
    }

    static void approveRequests() {
        System.out.print("Enter Your User ID: ");
        String creatorId = sc.nextLine();
        System.out.print("Enter Ride ID: ");
        int rideId = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter Ride Password: ");
        String pwd = sc.nextLine();

        Ride r = rideManager.getRideById(rideId);
        if (r instanceof PoolRide poolRide && poolRide.creator.id.equals(creatorId) && poolRide.password.equals(pwd)) {
            for (Map.Entry<User, String> entry : poolRide.getJoinRequests().entrySet()) {
                if (entry.getValue().equals("PENDING")) {
                    System.out.println("Join request from: " + entry.getKey().name);
                    System.out.print("Approve/Reject? ");
                    String action = sc.nextLine();
                    poolRide.approveRequest(entry.getKey(), action);
                }
            }
        } else {
            System.out.println("Invalid credentials or ride.");
        }
    }

    static void viewRides() {
        System.out.print("Enter Your User ID: ");
        String id = sc.nextLine();
        for (Ride r : rideManager.getAllRides()) {
            if (r.creator.id.equals(id)) {
                System.out.println("Ride ID: " + r.rideId + " | " + r.source + " to " + r.destination + " | Type: " + r.rideType);
            }
        }
    }
}