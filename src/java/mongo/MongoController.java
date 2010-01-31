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
import com.mongodb.gridfs.GridFSInputFile;
import config.Config;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

        Map<String, Object> t = o.toMap();

        BasicDBList k = (BasicDBList) t.get("keyword");
        String[] keyword = new String[k.size()];
        k.toArray(keyword);

        t.put("keyword", keyword);

        return t;
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
        Map<String, Object> hm;

        DBObject o;
        BasicDBList k;
        String[] keyword;

        while (c.hasNext()) {
            o = c.next();
            hm = o.toMap();

            k = (BasicDBList) hm.get("keyword");
            keyword = new String[k.size()];
            k.toArray(keyword);

            hm.put("keyword", keyword);

            l.add(hm);
        }

        return l;
    }

    public void saveObject(
        String sid, String pid, String theme, String title, String desc,
        String[] keyword, String[] ref
    ) {
        
        BasicDBObject o = new BasicDBObject();

        o.put("sid", sid);
        o.put("pid", pid);
        o.put("theme", theme);
        o.put("title", title);
        o.put("desc", desc);
        o.put("desc_type", "txt");
        o.put("keyword", keyword);
        o.put("ref", ref);
        o.put("rating", 0);

        objects.insert(o);
    }

    public void saveObject(
        String sid, String pid, String theme, String title,
        String fname, String ftype, byte[] desc,
        String[] keyword, String[] ref
    ) {

        GridFSInputFile f = gfs.createFile(desc);
        f.setContentType(ftype);
        f.setFilename(fname);
        f.save();

        BasicDBObject o = new BasicDBObject();

        o.put("sid", sid);
        o.put("pid", pid);
        o.put("theme", theme);
        o.put("title", title);
        o.put("desc", f);
        o.put("desc_type", "file");
        o.put("keyword", keyword);
        o.put("ref", ref);
        o.put("rating", 0f);

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

        List< Map<String, Object> > l = new LinkedList< Map<String, Object> >();
        Map<String, Object> hm;
        DBObject o;
        String[] keyword, ref;
        BasicDBList k, r, v;
        Map<String, Object>[] vote;
        while (c.hasNext()) {
            o = c.next();
            hm = o.toMap();

            k = (BasicDBList) hm.get("keyword");
            keyword = (null == k)? null : new String[k.size()];
            if (null != keyword) k.toArray(keyword);

            r = (BasicDBList) hm.get("ref");
            ref = (null == r)? null : new String[r.size()];
            if (null != ref) r.toArray(ref);

            v = (BasicDBList) hm.get("vote");
            vote = (null == v)? null : new Map[v.size()];
            for (int i = 0; null != vote && i < v.size(); ++i) {
                vote[i] = ((DBObject) v.get(i)).toMap();
            }

            hm.put("keyword", keyword);
            hm.put("ref", ref);
            hm.put("vote", vote);

            l.add(hm);
        }

        return l;
    }

    public Map<String, Object> getObject(Map<String, Object> object) {

        String _id = (String) object.get("_id");
        if (null != _id) object.put("_id", new ObjectId(_id));

        DBObject o = objects.findOne(new BasicDBObject(object));

        if (null == o) return null;

        Map<String, Object> hm = o.toMap();

        String[] keyword, ref;
        BasicDBList k, r, v;
        Map<String, Object>[] vote;

        k = (BasicDBList) hm.get("keyword");
        keyword = (null == k)? null : new String[k.size()];
        if (null != keyword) k.toArray(keyword);

        r = (BasicDBList) hm.get("ref");
        ref = (null == r)? null : new String[r.size()];
        if (null != ref) r.toArray(ref);

        v = (BasicDBList) hm.get("vote");
        vote = (null == v)? null : new Map[v.size()];
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
