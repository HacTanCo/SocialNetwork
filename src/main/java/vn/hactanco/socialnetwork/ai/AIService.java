package vn.hactanco.socialnetwork.ai;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class AIService {

	@Value("${groq.api.key}")
	private String API_KEY;

	private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

	private final ObjectMapper mapper = new ObjectMapper();

	// ================= CORE CALL =================
	public String callGroq(String message) throws Exception {

		URL url = new URL(API_URL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("POST");
		conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setConnectTimeout(10000);
		conn.setReadTimeout(10000);
		conn.setDoOutput(true);

		// 🔥 build JSON ngay tại đây
		ObjectNode body = mapper.createObjectNode();
		body.put("model", "llama-3.3-70b-versatile");

		ArrayNode messages = mapper.createArrayNode();

		ObjectNode msg = mapper.createObjectNode();
		msg.put("role", "user");
		msg.put("content", message);

		messages.add(msg);

		body.set("messages", messages);

		String jsonBody = mapper.writeValueAsString(body);

		try (OutputStream os = conn.getOutputStream()) {
			os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
		}

		InputStream is = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		String response = br.lines().reduce("", (a, b) -> a + b);

		if (conn.getResponseCode() >= 400) {
			System.out.println("❌ Groq error: " + response);
			return "AI đang lỗi 😢";
		}

		JsonNode root = mapper.readTree(response);

		return root.path("choices").get(0).path("message").path("content").asText();
	}
}