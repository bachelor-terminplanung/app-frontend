package at.terminplaner;

import android.app.Application;
public class MyApp extends Application{
    private int userId; //Globale User-ID

    /*
    um die id abzurufen:
    int userId = ((MyApp) requireActivity().getApplication()).getUserId();
     */
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
