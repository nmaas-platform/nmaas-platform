package net.geant.nmaas.externalservices.kubernetes;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RemoteClusterHelper {

    public static String saveFileToTmp(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        String hash = computeSHA256(file);

        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path filePath = tmpDir.resolve(hash + ".yaml");

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    private static String computeSHA256(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = file.getInputStream();
             DigestInputStream dis = new DigestInputStream(is, digest)) {
            while (dis.read() != -1) {
            }
        }

        StringBuilder hexString = new StringBuilder();
        for (byte b : digest.digest()) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

}
