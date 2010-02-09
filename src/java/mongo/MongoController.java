package mongo;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.ObjectId;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import config.Config;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;


public class MongoController {

    private Mongo m;
    private DB repo;
    private GridFS gfs;
    private DBCollection themes;
    private DBCollection objects;

    private boolean error = true;
    public boolean alive() {
        return (false == error);
    }

    public MongoController () {
        try {
            Config c   = new Config();
            String ip  = c.getConfig("mongodb_ip");
            int port   = Integer.valueOf(c.getConfig("mongodb_port"));
            String db  = c.getConfig("mongodb_db");
            String usr = c.getConfig("mongodb_usr");
            String pwd = c.getConfig("mongodb_pwd");

            m     = new Mongo(ip, port);
            repo  = m.getDB(db);
            if (false == repo.authenticate(usr, pwd.toCharArray())) {
                throw new Exception("invalid mongo credential");
            }
            gfs = new GridFS(repo);

            themes  = repo.getCollection("theme");
            objects = repo.getCollection("object");

            error = false;

        } catch (Exception ex) {
            error = true;

            themes  = null;
            objects = null;
            repo    = null;
            m       = null;
            Logger.getLogger(MongoController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveTheme(String name, String[] keywords, boolean show) {

        BasicDBObject t = new BasicDBObject();
        t.put("name", name);
        t.put("keyword", keywords);
        t.put("show", show);

        themes.update(new BasicDBObject("name", name), t, true, false);
    }

    public Map<String, Object> getTheme(Map<String, Object> theme) {

        String _id = (String) theme.get("_id");
        if (null != _id) theme.put("_id", new ObjectId(_id));

        DBObject o = themes.findOne(new BasicDBObject(theme));

        if (null == o) return null;

        return themeToMap(o);
    }

    public List<Map<String, Object>> dumpTheme() {
        return queryTheme(null);
    }

    public List<Map<String, Object>> queryTheme(Map<String, Object> theme) {

        if (null != theme) {
            String _id = (String) theme.get("_id");
            if (null != _id) theme.put("_id", new ObjectId(_id));
        }

        DBCursor c = (null == theme)
                     ? themes.find()
                     : themes.find(new BasicDBObject(theme));

        if (0 == c.count()) return null;

        List<Map<String, Object>> l = new LinkedList<Map<String, Object>>();

        while (c.hasNext()) {
            l.add(themeToMap(c.next()));
        }

        return l;
    }

    private Map<String, Object> themeToMap(DBObject o) {

        Map<String, Object> hm = o.toMap();

        BasicDBList k = (BasicDBList) hm.get("keyword");
        String[] keyword = (null == k)? null : new String[k.size()];
        if (null != keyword) k.toArray(keyword);

        hm.put("keyword", keyword);

        return hm;
    }

    public void saveObject(
        String sid, String pid, String theme, String type,
        String summary, String desc, String explain,
        String[] keyword, String[] ref, String submit_by
    ) {

        BasicDBObject o = new BasicDBObject();

        o.put("sid", sid);
        o.put("pid", pid);
        o.put("theme", theme);
        o.put("type", type);
        o.put("summary", summary);
        o.put("desc", desc);
        o.put("explain", explain);
        o.put("keyword", keyword);
        o.put("ref", ref);
        o.put("rating", 0);
        o.put("submit", submit_by);
        o.put("create", new Date());

        objects.insert(o);
    }

    public void updateObject(String _id, Map<String, Object> object) {

        objects.update(
            new BasicDBObject("_id", new ObjectId(_id)),
            new BasicDBObject(object)
        );
    }

    public List<Map<String, Object>> dumpObject() {
        return queryObject(null);
    }

    public List<Map<String, Object>> queryObject(Map<String, Object> object) {

        if (null != object) {
            String _id = (String) object.get("_id");
            if (null != _id) object.put("_id", new ObjectId(_id));
        }

        DBCursor c = (null == object)
                     ? objects.find()
                     : objects.find(new BasicDBObject(object));
        if (0 == c.count()) return null;

        List<Map<String, Object>> l = new LinkedList<Map<String, Object>>();
        while (c.hasNext()) {
            l.add(objToMap(c.next()));
        }

        return l;
    }

    public Map<String, Object> getObject(Map<String, Object> object) {

        String _id = (String) object.get("_id");
        if (null != _id) object.put("_id", new ObjectId(_id));

        DBObject o = objects.findOne(new BasicDBObject(object));

        if (null == o) return null;

        return objToMap(o);
    }

    private Map<String, Object> objToMap(DBObject o) {

        Map<String, Object> hm = o.toMap();

        BasicDBList k = (BasicDBList) hm.get("keyword");
        String[] keyword = (null == k)? null : new String[k.size()];
        if (null != keyword) k.toArray(keyword);

        BasicDBList r = (BasicDBList) hm.get("ref");
        String[] ref = (null == r)? null : new String[r.size()];
        if (null != ref) r.toArray(ref);

        BasicDBList v = (BasicDBList) hm.get("vote");
        Map[] vote = (null == v)? null : new Map[v.size()];
        for (int i = 0; null != vote && i < v.size(); ++i) {
            vote[i] = ((DBObject) v.get(i)).toMap();
        }

        hm.put("keyword", keyword);
        hm.put("ref", ref);
        hm.put("vote", vote);

        return hm;
    }

    public Map<String, Object> getFile(Map<String, Object> object) {

        String _id = (String) object.get("_id");
        if (null != _id) object.put("_id", new ObjectId(_id));

        DBObject o = objects.findOne(new BasicDBObject(object));
        if (null == o) return null;

        GridFSDBFile f = gfs.findOne((DBObject) o.get("desc"));
        if (null == f) return null;

        Map<String, Object> hm   = new LinkedHashMap<String, Object>();
        ByteArrayOutputStream os = new ByteArrayOutputStream(
            (int) f.getLength()
        );
        try {
            f.writeTo(os);
        } catch (IOException ex) {
            Logger.getLogger(MongoController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        hm.put("filename", f.getFilename());
        hm.put("contentType", f.getContentType());
        hm.put("data", os.toByteArray());

        return hm;
    }
}
