import io.quarkus.elytron.security.common.BcryptUtil;

public class tempHashGenerator {
    public static void main(String[] args) {
        String password = "admin";
        String hash = BcryptUtil.bcryptHash(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
    }
}
