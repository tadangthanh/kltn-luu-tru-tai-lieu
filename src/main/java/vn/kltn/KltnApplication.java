package vn.kltn;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.util.Base64;

@SpringBootApplication
public class KltnApplication {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		// Load .env variables into environment
		Dotenv dotenv = Dotenv.configure().load();
		System.setProperty("AZURE_STORAGE_CONNECTION_STRING", dotenv.get("AZURE_STORAGE_CONNECTION_STRING"));
		System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));
		System.setProperty("GOOGLE_API_KEY", dotenv.get("GOOGLE_API_KEY"));
		System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
		System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));
//		// 1. Tạo cặp khóa RSA 2048-bit
//		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
//		keyGen.initialize(2048);
//		KeyPair keyPair = keyGen.generateKeyPair();
//
//		// 2. Chuyển đổi khóa riêng thành định dạng PEM
//		String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n" +
//				Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) +
//				"\n-----END PRIVATE KEY-----";
//
//		// 3. Chuyển đổi khóa công khai thành định dạng PEM
//		String publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n" +
//				Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) +
//				"\n-----END PUBLIC KEY-----";
//
//		// 4. Lưu khóa riêng vào file
//		try (FileWriter privateKeyWriter = new FileWriter("private_key.pem")) {
//			privateKeyWriter.write(privateKeyPEM);
//		}
//
//		// 5. Lưu khóa công khai vào file
//		try (FileWriter publicKeyWriter = new FileWriter("public_key.pem")) {
//			publicKeyWriter.write(publicKeyPEM);
//		}
//
//		System.out.println("Cặp khóa RSA đã được lưu ở định dạng PEM!");
		SpringApplication.run(KltnApplication.class, args);
	}

}
