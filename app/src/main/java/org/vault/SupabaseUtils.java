package org.vault;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class SupabaseUtils {
  private static final String SUPABASE_URL = "https://nkonqvgfxvhiefhpnrmg.supabase.co/";
  private static final String SUPABASE_API_KEY = System.getenv("SUPABASE_API_KEY");
  private static final String BUCKET_NAME = "vaults";

  private static final HttpClient client = HttpClient.newHttpClient();

  public static boolean uploadVault(Path localFile, String remotePath) {
    try {
      String uploadUrl = String.format(
          "%sstorage/v1/object/%s/%s",
          SUPABASE_URL,
          BUCKET_NAME,
          URLEncoder.encode(remotePath, StandardCharsets.UTF_8));

      HttpRequest req = HttpRequest.newBuilder(URI.create(uploadUrl))
          .header("apikey", SUPABASE_API_KEY)
          .header("Authorization", "Bearer " + SUPABASE_API_KEY)
          .POST(BodyPublishers.ofFile(localFile))
          .build();

      HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
      int code = res.statusCode();

      if (code / 100 == 2) {
        System.out.println("[Supabase.uploadVault] Success: " + res.body());
        return true;
      } else {
        System.err.println("[Supabase.uploadVault] Failed [" + code + "]: " + res.body());
        return false;
      }
    } catch (Exception e) {
      System.err.println("[Supabase.uploadVault] ERROR: ");
      e.printStackTrace();
      return false;
    }
  }

  public static boolean downloadVault(String remotePath, Path localDest) {
    try {
      String downloadUrl = String.format(
          "%sstorage/v1/object/public/%s/%s",
          SUPABASE_URL,
          BUCKET_NAME,
          URLEncoder.encode(remotePath, StandardCharsets.UTF_8));

      HttpRequest req = HttpRequest.newBuilder(URI.create(downloadUrl))
          .header("apikey", SUPABASE_API_KEY)
          .header("Authorization", "Bearer " + SUPABASE_API_KEY)
          .GET()
          .build();

      HttpResponse<Path> res = client.send(req, BodyHandlers.ofFile(localDest));
      int code = res.statusCode();

      if (code / 100 == 2) {
        System.out.println("[Supabase.downloadVault] Success: wrote to " + localDest);
        return true;
      } else {
        System.err.println("[Supabase.downloadVault] Failed [" + code + "]");
        return false;
      }
    } catch (Exception e) {
      System.err.println("[Supabase.downloadVault] ERROR:");
      e.printStackTrace();
      return false;
    }
  }
}
