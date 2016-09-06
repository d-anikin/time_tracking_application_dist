import java.util.prefs.Preferences;

/**
 * Created by dmitry on 06.09.16.
 */
public class App {
    private Preferences userPrefs;
    private RedmineApi redmineApi;

    private void authorize() {
        if (userPrefs.get("redmine_url", "").isEmpty()) {
            authorizeForm();
        } else {
            this.redmineApi = new RedmineApi(userPrefs.get("redmine_url", ""), userPrefs.get("redmine_api_key", ""));
            if (!redmineApi.checkAccess()) {
                authorizeForm();
            }
        }
    }

    private void authorizeForm() {
        LoginForm dialog = new LoginForm(userPrefs);
        dialog.setVisible(true);
        this.redmineApi = new RedmineApi(userPrefs.get("redmine_url", ""), userPrefs.get("redmine_api_key", ""));
    }

    public void mainForm() {
        System.out.print("Show main form");
    }

    public static void main(String[] args) {
        App app = new App();
        System.exit(0);
    }

    public App() {
        userPrefs = Preferences.userRoot().node("redmine_time_tracker_app");
        authorize();
        mainForm();
    }
}
