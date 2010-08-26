package restapi;

import com.google.gson.Gson;
import com.mongodb.QueryBuilder;
import config.Config;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import mongo.MongoController;

@Path("/")
public class RestAPI {
    @Context
    private UriInfo context;



    public RestAPI() {
    }

    /**
     * Get all learning objects
     */
    @GET
    @Path("learning_objects")
    @Produces("application/json")
    public String getLearningObjects() {
        try {
            MongoController m = new MongoController();

            List<Map<String, Object>> objects = m.dumpObject();
            return new Gson().toJson(objects);
            
        } catch (IOException ex) {
                return "{\"error\": 1}";
        }
    }

    /**
     * Get learning objects by category
     */
    @GET
    @Path("learning_objects/category/{category}")
    @Produces("application/json")
    public String getLearningObjectsByCategory(
        @PathParam("category") String category) {
        
        try {
            List<Map<String, Object>> categories = new Config().getTopics();
            List<String> topics = null;

            for (Map<String, Object> c : categories) {
                if (c.get("category").equals(category)) {
                    topics = (List<String>) c.get("topic");
                }
            }
            if (null == topics) return "{\"error\": 1}";

            MongoController m = new MongoController();

            HashMap<String, Object> hm = new HashMap<String, Object>();

            Map<String, Object> q;
            for (String t : topics) {
                q = new HashMap<String, Object>();
                q.put("keyword", Pattern.compile(t, Pattern.CASE_INSENSITIVE));
                hm.put(t, m.queryObject(q));
            }

            return new Gson().toJson(hm);

        } catch (IOException ex) {
            return "{\"error\": 1}";
        }
    }

    /**
     * Get learning objects by keyword
     */
    @GET
    @Path("learning_objects/keyword/{keyword}")
    @Produces("application/json")
    public String getLearningObjectsByKeyword(
        @PathParam("keyword") String keyword) {

        try {
            MongoController m = new MongoController();

            Map<String, Object> q = new HashMap<String, Object>();
            q.put("keyword", Pattern.compile(keyword, Pattern.CASE_INSENSITIVE));

            return new Gson().toJson(m.queryObject(q));

        } catch (IOException ex) {
            return "{\"error\": 1}";
        }
    }

    /**
     * Get learning object by id
     */
    @GET
    @Path("learning_objects/object/{oid}")
    @Produces("application/json")
    public String getLearningObjectById(@PathParam("oid") String oid) {

        try {
            MongoController m = new MongoController();

            Map<String, Object> q = new HashMap<String, Object>();
            q.put("_id", oid);
            
            return new Gson().toJson(m.getObject(q));

        } catch (IOException ex) {
            return "{\"error\": 1}";
        }
    }
}
