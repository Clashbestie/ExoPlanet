import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Config
{
    private static HashMap<String, String> config = new HashMap<>();

    public static int getInt(String key)
    {
        String x = config.get(key.toUpperCase());
        return (x == null) ? 0 : Integer.parseInt(x);
    }

    public static String get(String key)
    {
        String x = config.get(key.toUpperCase());
        return (x == null) ? "" : x;
    }

    public static boolean getBool(String key)
    {
        String x = config.get(key.toUpperCase());
        return x != null && Boolean.parseBoolean(x);
    }

    private static void initDefaults()
    {

    }

    public static void loadConfig(InputStream in)
    {
        initDefaults();
        if (in == null) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) continue;
                String[] t = line.split("=");
                if (t.length <= 1) continue;
                config.put(t[0].trim().toUpperCase(), t[1].trim());
            }
            in.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
