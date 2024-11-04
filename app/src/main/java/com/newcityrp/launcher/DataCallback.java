import org.json.JSONObject;

public interface DataCallback {
    void onSuccess(JSONObject data);
    void onFailure(Exception e);
}
