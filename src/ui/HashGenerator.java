import org.mindrot.jbcrypt.BCrypt;

public class HashGenerator {
    public static void main(String[] args) {
        String plainPassword = "testpass"; // The password you will use to log in
        // Generate the hash using a cost factor of 10
        String newHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
        System.out.println("--- NEW HASH ---");
        System.out.println(newHash); // Copy this entire string!
        System.out.println("--- END HASH ---");
    }
}