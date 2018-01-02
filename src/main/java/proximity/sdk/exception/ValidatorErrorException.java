package proximity.sdk.exception;

import org.json.JSONArray;
import org.json.JSONObject;
import postman.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ValidatorErrorException extends HttpException {

    /**
     * A set of validation errors.
     */
    private Map<String, ArrayList<String>> errors;

    /**
     * @param errorCode is always 'VALIDATION_ERROR'
     * @param response is a full server response
     */
    public ValidatorErrorException(String errorCode, Response<JSONObject> response) {
        super(errorCode, response);

        this.errors = new HashMap<String, ArrayList<String>>();
        this.parseErrors(response);
    }

    /**
     * @return validation errors
     */
    public Map<String, ArrayList<String>> getErrors() {
        return errors;
    }

    /**
     * @param response composes an array list from json
     */
    private void parseErrors(Response<JSONObject> response) {
        Set<String> fields = response.getBody().getJSONObject("errors").keySet();

        for (String key : fields) {
            JSONArray fieldErrors = response.getBody().getJSONObject("errors").getJSONArray(key);
            ArrayList<String> errors = new ArrayList<String>();

            for (int i = 0; i < fieldErrors.length(); i++) {
                errors.add(fieldErrors.getString(i));
            }

            this.errors.put(key, errors);
        }
    }
}
