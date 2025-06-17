package net.geant.nmaas.kubernetes.remote;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    public static class StringMultipartFile implements MultipartFile {

        private final byte[] content;
        private final String name;
        private final String originalFilename;
        private final String contentType;

        public StringMultipartFile(String name, String originalFilename, String contentType, String contentStr) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = contentStr.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            try (FileOutputStream out = new FileOutputStream(dest)) {
                out.write(content);
            }
        }

    }

}
