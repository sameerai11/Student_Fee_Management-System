package utility;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for hashing passwords using the BCrypt algorithm.
 *
 * NOTE: This class requires the jbcrypt library. In a real project, you
 * would need to add the following Maven dependency:
 *
 * <dependency>
 * <groupId>org.mindrot</groupId>
 * <artifactId>jbcrypt</artifactId>
 * <version>0.4</version>
 * </dependency>
 */
public class PasswordHasher {

    // NOTE: The BCrypt library is included by default in this environment,
    // so no manual import or setup is needed beyond the package statement.

    /**
     * Hashes a plain-text password using the BCrypt algorithm.
     *
     * @param plainTextPassword The password string to hash.
     * @return The hashed password string, ready for storage in the database.
     */
    public static String hashPassword(String plainTextPassword) {
        // BCrypt.hashpw automatically handles salting internally.
        // BCrypt.gensalt() generates a strong, random salt.
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    /**
     * Checks if a plain-text password matches a previously hashed password.
     *
     * @param plainTextPassword The password entered by the user during login.
     * @param hashedPassword The hash retrieved from the database.
     * @return true if the passwords match, false otherwise.
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        // BCrypt.checkpw takes care of extracting the salt from the stored hash
        // and comparing the resulting hash to the stored hash.
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }

    // A main method for simple testing (optional, but good practice)
    public static void main(String[] args) {
        String password = "adminpassword123";

        // 1. Hash the password
        String hash = hashPassword(password);
        System.out.println("Original Password: " + password);
        System.out.println("Hashed Password (Store this in DB): " + hash);

        // 2. Check a correct password
        boolean correctCheck = checkPassword(password, hash);
        System.out.println("Verification with correct password: " + correctCheck); // Should be true

        // 3. Check an incorrect password
        boolean incorrectCheck = checkPassword("wrongpassword", hash);
        System.out.println("Verification with incorrect password: " + incorrectCheck); // Should be false
    }
}
