package restapi;

import com.google.gson.Gson;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import config.Config;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import mongo.MongoController;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

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
//            MongoController m = new MongoController();
            MongoController m = MongoController.getInstance();

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

//            MongoController m = new MongoController();
            MongoController m = MongoController.getInstance();

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
    @Path("learning_objects/keyword/")
    @Produces("application/json")
    public String getLearningObjectsByKeyword(@QueryParam("q") String keyword) {

        try {
//            MongoController m = new MongoController();
            MongoController m = MongoController.getInstance();

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
//            MongoController m = new MongoController();
            MongoController m = MongoController.getInstance();

            Map<String, Object> q = new HashMap<String, Object>();
            q.put("_id", oid);
            
            return new Gson().toJson(m.getObject(q));

        } catch (IOException ex) {
            return "{\"error\": 1}";
        }
    }

    /**
     * Query for learning object by keyword and summary
     */
    @GET
    @Path("learning_objects/object/")
    @Produces("application/json")
    public String queryLearningObject(@QueryParam("q") String query) {

        try {
//            MongoController m = new MongoController();
            MongoController m = MongoController.getInstance();
            DBObject q = QueryBuilder.start()
                .or(
                    QueryBuilder.start("keyword")
                        .regex(Pattern.compile(query, Pattern.CASE_INSENSITIVE)).get(),
                    QueryBuilder.start("summary")
                        .regex(Pattern.compile(query, Pattern.CASE_INSENSITIVE)).get()
                ).get();

            return new Gson().toJson(m.queryObject(q));

        } catch (IOException ex) {
            return "{\"error\": 1}";
        }
    }


    /**
     * Query for learning object by student id
     */
    @GET
    @Path("learning_objects/submitby")
    @Produces("application/json")
    public String getLearningObjectByWhomSubmit(@QueryParam("sid") String sid) {

        try {
//            MongoController m = new MongoController();
            MongoController m = MongoController.getInstance();
            DBObject q = QueryBuilder.start()
                .or(
                    QueryBuilder.start("sid")
                        .is(sid).get(),
                    QueryBuilder.start("pid")
                        .is(sid).get()
                ).get();

            return new Gson().toJson(m.queryObject(q));

        } catch (IOException ex) {
            return "{\"error\": 1}";
        }
    }

    /**
     * Query for topic(s)
     */
    @GET
    @Path("topics/")
    @Produces("application/json")
    public String getTopics(@QueryParam("q") String query) {
        
        List<Map<String, Object>> categories = new Config().getTopics();
        List<Map<String, String>> topics = new LinkedList<Map<String, String>>();
        HashMap<String, String> m;

        for (Map<String, Object> c : categories) {
            for (String t : (List<String>) c.get("topic")) {
                if (query.isEmpty() || t.toLowerCase().contains(query.toLowerCase())) {
                    m = new HashMap<String, String>();
                    m.put("id", t);
                    m.put("name", t);

                    topics.add(m);
                }
            }
        }
        if (0 == topics.size()) return "{\"error\": 1}";

        return new Gson().toJson(topics);
    }

    /*
    @GET
    @Path("stats/user")
    @Produces("application/xls")
    public Response getStatsByUser(@Context UriInfo info) {
        // http://poi.apache.org/spreadsheet/how-to.html#user_api
        Workbook wb = new HSSFWorkbook();
        Sheet s = wb.createSheet();

        wb.setSheetName(0, "stats_by_students");

        Row r = null;
        Cell c = null;

        // user id, object[, object, ...]
        r = s.createRow(0);

        c = r.createCell(0);
        c.setCellValue("User ID");

        c = r.createCell(1);
        c.setCellValue("Objects");
        
        byte[] out = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {

            MongoController m = MongoController.getInstance();
            List<String> distinct_submit = m.distinctSubmit();

            int len = distinct_submit.size();
            for (int i = 0; i < len; ++i) {
                String whom = distinct_submit.get(i);

                r = s.createRow(i + 1);

                c = r.createCell(0);
                c.setCellValue(whom);

                HashMap<String, Object> q = new HashMap<String, Object>();
                q.put("submit", whom);

                List<Map<String, Object>> objs = m.queryObject(q);
                for (int j = 0; j < objs.size(); ++j) {
                    Map<String, Object> obj = objs.get(j);

                    String[] uri_segments = info.getBaseUri().toString().split("/");
                    // ignore last segments
                    String permlink = "";
                    for (int k = 0; k < uri_segments.length - 1; ++k) {
                        permlink += uri_segments[k] + "/";
                    }
                    permlink += "edit.jsp?oid=" + obj.get("_id").toString();

                    HSSFHyperlink l = new HSSFHyperlink(HSSFHyperlink.LINK_URL);
                    l.setAddress(permlink);

                    c = r.createCell(j + 1);
                    c.setCellValue(permlink);
                    c.setHyperlink(l);
                }
            }


            wb.write(os);
            out = os.toByteArray();
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(RestAPI.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Response.ok(out)
                .header("Content-Disposition", "attachment; filename=stats.xls")
                .build();
    }
    */
}



