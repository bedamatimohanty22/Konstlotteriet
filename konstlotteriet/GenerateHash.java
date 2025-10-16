import io.quarkus.elytron.security.common.BcryptUtil;

public class GenerateHash {
    public static void main(String[] args) {
        String password = "admin123";
        String hash = BcryptUtil.bcryptHash(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);

        // Verify it works
        boolean matches = BcryptUtil.matches(password, hash);
        System.out.println("Verification: " + matches);
    }
}