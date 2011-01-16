package mongo;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import config.Config;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.types.ObjectId;


public class MongoController {

    private static MongoController instance;

    private Mongo m;
    private DB repo;
//    private GridFS gfs;
    private DBCollection themes;
    private DBCollection objects;
    private DBCollection votes;
    private DBCollection views;
    private DBCollection annotations;
    private DBCollection category_views;
    private DBCollection topic_views;
    private DBCollection object_views;

    public static MongoController getInstance() throws IOException {
        if (null == MongoController.instance) {
            MongoController.instance = new MongoController();
        }
        return MongoController.instance;
    }

    private MongoController() throws IOException {
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
//            gfs = new GridFS(repo);

            themes      = repo.getCollection("theme");
            objects     = repo.getCollection("object");
            votes       = repo.getCollection("vote");
            views       = repo.getCollection("view");
            annotations = repo.getCollection("annotation");
            category_views = repo.getCollection("category_view");
            topic_views = repo.getCollection("topic_view");
            object_views = repo.getCollection("object_view");

            // create index for votes
            DBObject vote_idx = new BasicDBObject();
            vote_idx.put("oid", 1);
            vote_idx.put("voter", 1);
            votes.ensureIndex(vote_idx);

            // create index for views
            DBObject view_idx = new BasicDBObject();
            view_idx.put("oid", 1);
            view_idx.put("viewer", 1);
            views.ensureIndex(view_idx);

            // create index for annotations
            DBObject annotate_idx = new BasicDBObject();
            annotate_idx.put("oid", 1);
            annotate_idx.put("whom", 1);
            annotations.ensureIndex(annotate_idx);

        } catch (Exception ex) {
            Logger.getLogger(MongoController.class.getName()).log(Level.SEVERE, null, ex);

            throw new IOException("mongo is dead.");
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
        
        hm.put("_id", hm.get("_id").toString());

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
        o.put("rating", new Double(.0));
        o.put("view_count", new Double(0));
        o.put("submit", submit_by);
        // TODO: remove this redundant field
        //       object_id already contains a timestamp
        o.put("create", new Date());
        o.put("comment", "");

        objects.insert(o);
    }

    public void updateObject(String _id, Map<String, Object> object) {

        object.put("_id", new ObjectId(_id));

        objects.update(
            new BasicDBObject("_id", new ObjectId(_id)),
            new BasicDBObject(object)
        );
    }

    public List<Map<String, Object>> dumpObject() {
        return queryObject((DBObject) null);
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

    public List<Map<String, Object>> queryObject(DBObject object) {

        DBCursor c = objects.find(object);
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

        hm.put("_id", hm.get("_id").toString());

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

    public void saveVote(String oid, Double rating, String voter) {
        BasicDBObject o = new BasicDBObject();

        o.put("oid", oid);
        o.put("voter", voter);
        o.put("rating", rating);

        votes.insert(o);
    }

    public void updateVote(String _id, Map<String, Object> vote) {

        vote.put("_id", new ObjectId(_id));

        votes.update(
            new BasicDBObject("_id", new ObjectId(_id)),
            new BasicDBObject(vote)
        );
    }

    public List<Map<String, Object>> dumpVote() {
        return queryVote(null);
    }

    public List<Map<String, Object>> queryVote(Map<String, Object> vote) {

        if (null != vote) {
            String _id = (String) vote.get("_id");
            if (null != _id) vote.put("_id", new ObjectId(_id));
        }

        DBCursor c = (null == vote)
                     ? votes.find()
                     : votes.find(new BasicDBObject(vote));
        if (0 == c.count()) return null;

        List<Map<String, Object>> l = new LinkedList<Map<String, Object>>();
        while (c.hasNext()) {
            l.add(voteToMap(c.next()));
        }

        return l;
    }

    public Map<String, Object> getVote(Map<String, Object> vote) {

        String _id = (String) vote.get("_id");
        if (null != _id) vote.put("_id", new ObjectId(_id));

        DBObject o = votes.findOne(new BasicDBObject(vote));

        if (null == o) return null;

        return voteToMap(o);
    }

    private Map<String, Object> voteToMap(DBObject o) {

        Map<String, Object> hm = o.toMap();

        hm.put("_id", hm.get("_id").toString());

        return hm;
    }

    public void saveView(String oid, String viewer) {
        BasicDBObject o = new BasicDBObject();

        o.put("oid", oid);
        o.put("viewer", viewer);

        views.insert(o);
    }

    public List<Map<String, Object>> dumpView() {
        return queryView(null);
    }

    public List<Map<String, Object>> queryView(Map<String, Object> view) {

        if (null != view) {
            String _id = (String) view.get("_id");
            if (null != _id) view.put("_id", new ObjectId(_id));
        }

        DBCursor c = (null == view)
                     ? views.find()
                     : views.find(new BasicDBObject(view));
        if (0 == c.count()) return null;

        List<Map<String, Object>> l = new LinkedList<Map<String, Object>>();
        while (c.hasNext()) {
            l.add(viewToMap(c.next()));
        }

        return l;
    }

    public Map<String, Object> getView(Map<String, Object> view) {

        String _id = (String) view.get("_id");
        if (null != _id) view.put("_id", new ObjectId(_id));

        DBObject o = views.findOne(new BasicDBObject(view));

        if (null == o) return null;

        return viewToMap(o);
    }

    private Map<String, Object> viewToMap(DBObject o) {

        Map<String, Object> hm = o.toMap();

        hm.put("_id", hm.get("_id").toString());

        return hm;
    }

    public long getViewCount(Map<String, Object> view) {
        
        if (null != view) {
            String _id = (String) view.get("_id");
            if (null != _id) view.put("_id", new ObjectId(_id));
        }

        long c = (null == view)
                 ? views.getCount()
                 : views.getCount(new BasicDBObject(view));

        return c;
    }

    public void saveAnnotation(String oid, String[] keyword, String whom) {
        BasicDBObject o = new BasicDBObject();

        o.put("oid", oid);
        o.put("whom", whom);
        o.put("keyword", keyword);

        annotations.insert(o);
    }

    public void updateAnnotation(String _id, Map<String, Object> annotation) {

        annotation.put("_id", new ObjectId(_id));

        annotations.update(
            new BasicDBObject("_id", new ObjectId(_id)),
            new BasicDBObject(annotation)
        );
    }

    public List<Map<String, Object>> dumpAnnotation() {
        return queryAnnotation(null);
    }

    public List<Map<String, Object>> queryAnnotation(Map<String, Object> annotation) {

        if (null != annotation) {
            String _id = (String) annotation.get("_id");
            if (null != _id) annotation.put("_id", new ObjectId(_id));
        }

        DBCursor c = (null == annotation)
                     ? annotations.find()
                     : annotations.find(new BasicDBObject(annotation));
        if (0 == c.count()) return null;

        List<Map<String, Object>> l = new LinkedList<Map<String, Object>>();
        while (c.hasNext()) {
            l.add(annotationToMap(c.next()));
        }

        return l;
    }

    public Map<String, Object> getAnnotation(Map<String, Object> annotation) {

        String _id = (String) annotation.get("_id");
        if (null != _id) annotation.put("_id", new ObjectId(_id));

        DBObject o = annotations.findOne(new BasicDBObject(annotation));

        if (null == o) return null;

        return annotationToMap(o);
    }

    private Map<String, Object> annotationToMap(DBObject o) {

        Map<String, Object> hm = o.toMap();

        hm.put("_id", hm.get("_id").toString());

        BasicDBList k = (BasicDBList) hm.get("keyword");
        String[] keyword = (null == k)? null : new String[k.size()];
        if (null != keyword) k.toArray(keyword);

        hm.put("keyword", keyword);

        return hm;
    }

    public List<String> distinctSubmit() {
        return objects.distinct("submit");
    }

    public void saveCategoryView(String category, String user) {
        BasicDBObject o = new BasicDBObject();

        o.put("category", category);
        o.put("user", user);

        category_views.insert(o);
    }

    public Map<String, Object> getCategoryView(Map<String, Object> view) {
        String _id = (String) view.get("_id");
        if (null != _id) view.put("_id", new ObjectId(_id));

        DBObject o = category_views.findOne(new BasicDBObject(view));

        if (null == o) return null;

        return categoryViewToMap(o);
    }

    private Map<String, Object> categoryViewToMap(DBObject o) {
        Map<String, Object> hm = o.toMap();

        hm.put("_id", hm.get("_id").toString());

        return hm;
    }

    public List<Map<String, Object>> getCategoryViews() {

        DBCursor c = category_views.find();
        if (0 == c.count()) return null;

        List<Map<String, Object>> l = new LinkedList<Map<String, Object>>();
        while (c.hasNext()) {
            l.add(categoryViewToMap(c.next()));
        }

        return l;
    }

    public void saveTopicView(String topic, String user) {
        BasicDBObject o = new BasicDBObject();

        o.put("topic", topic);
        o.put("user", user);

        topic_views.insert(o);
    }

    public Map<String, Object> getTopicView(Map<String, Object> view) {
        String _id = (String) view.get("_id");
        if (null != _id) view.put("_id", new ObjectId(_id));

        DBObject o = topic_views.findOne(new BasicDBObject(view));

        if (null == o) return null;

        return topicViewToMap(o);
    }

    private Map<String, Object> topicViewToMap(DBObject o) {
        Map<String, Object> hm = o.toMap();

        hm.put("_id", hm.get("_id").toString());

        return hm;
    }

    public List<Map<String, Object>> getTopicViews() {

        DBCursor c = topic_views.find();
        if (0 == c.count()) return null;

        List<Map<String, Object>> l = new LinkedList<Map<String, Object>>();
        while (c.hasNext()) {
            l.add(topicViewToMap(c.next()));
        }

        return l;
    }

    public void saveObjectView(String object_id, String user) {
        BasicDBObject o = new BasicDBObject();

        o.put("object_id", object_id);
        o.put("user", user);

        object_views.insert(o);
    }

    public Map<String, Object> getObjectView(Map<String, Object> view) {
        String _id = (String) view.get("_id");
        if (null != _id) view.put("_id", new ObjectId(_id));

        DBObject o = object_views.findOne(new BasicDBObject(view));

        if (null == o) return null;

        return objectViewToMap(o);
    }

    private Map<String, Object> objectViewToMap(DBObject o) {
        Map<String, Object> hm = o.toMap();

        hm.put("_id", hm.get("_id").toString());

        return hm;
    }


/*
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
 */
}
