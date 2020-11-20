package uk.co.waterloobank;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class Constants {

    public static final String BASE_URL = "https://eu.rp.secure.iproov.me/api/v2/";

    private static final String API_KEY = ""; // TODO: Add your API key here
    private static final String SECRET = ""; // TODO: Add your Secret here

    private static final String SECRETS_ASSET_FILENAME = "secrets.json";

    private final String apiKey;
    private final String secret;

    Constants(Context context) {
        String a = API_KEY;
        String s = SECRET;

        try {
            InputStream in = context.getAssets().open(SECRETS_ASSET_FILENAME);
            byte[] array = new byte[in.available()];
            in.read(array);
            in.close();
            String jsonStr = new String(array, Charset.defaultCharset());
            JSONObject json = new JSONObject(jsonStr);
            a = API_KEY.isEmpty() ? json.optString("api_key") : API_KEY;
            s = SECRET.isEmpty() ? json.optString("secret") : SECRET;
        } catch (IOException|JSONException ex) { }

        this.apiKey = a;
        this.secret = s;
    }

    public final String getApiKey() {
        return apiKey;
    }

    public final String getSecret() {
        return secret;
    }
}
