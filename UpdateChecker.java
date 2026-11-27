package hexin.dc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

/**
 * Vérifie s'il existe une nouvelle version et propose de la télécharger.
 * Usage (dans ton écran principal) :  UpdateChecker.check(activity, "2.2.0");
 * Nécessite la permission INTERNET.
 */
public class UpdateChecker {
    static final String VERSION_URL = "https://rdnom12-prog.github.io/Hexin-dc/version.json";
    static final String PAGE_URL = "https://rdnom12-prog.github.io/Hexin-dc/";

    public static void check(final Activity a, final String current) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    HttpURLConnection c = (HttpURLConnection) new URL(VERSION_URL + "?t=" + System.currentTimeMillis()).openConnection();
                    c.setConnectTimeout(8000);
                    c.setReadTimeout(8000);
                    BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) sb.append(line);
                    r.close();
                    JSONObject o = new JSONObject(sb.toString());
                    final String latest = o.optString("version", current);
                    String apk = o.optString("apk", "");
                    final String link = apk.length() > 0 ? apk : PAGE_URL;
                    if (!newer(latest, current)) return;
                    a.runOnUiThread(new Runnable() {
                        public void run() {
                            if (a.isFinishing()) return;
                            new AlertDialog.Builder(a)
                                .setTitle("Mise à jour disponible")
                                .setMessage("La version " + latest + " est disponible. Tu utilises la version " + current + ".")
                                .setPositiveButton("Télécharger", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d, int w) {
                                        a.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                                    }
                                })
                                .setNegativeButton("Plus tard", null)
                                .show();
                        }
                    });
                } catch (Exception e) {
                    // Pas de connexion ou fichier illisible : on ne dit rien à l'utilisateur.
                }
            }
        }).start();
    }

    static boolean newer(String latest, String current) {
        String[] x = latest.split("\\.");
        String[] y = current.split("\\.");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            int p = i < x.length ? num(x[i]) : 0;
            int q = i < y.length ? num(y[i]) : 0;
            if (p != q) return p > q;
        }
        return false;
    }

    static int num(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }
}
