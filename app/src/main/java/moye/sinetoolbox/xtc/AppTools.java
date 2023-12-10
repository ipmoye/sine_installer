package moye.sinetoolbox.xtc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class AppTools {
    public static String root_exec(String command) {
        Process process = null;
        DataOutputStream os = null;
        String data = "";
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();

            BufferedReader successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String tmp;
            while((tmp = successResult.readLine()) != null)
                data += tmp + "\n";
            while((tmp = errorResult.readLine()) != null)
                data += tmp + "\n";
        } catch (Exception e) {
            return "Er00";
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return data;
    }
}
