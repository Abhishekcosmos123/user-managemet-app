package com.example.usermanagement.util;

import com.example.usermanagement.model.UserRecord;
import com.google.cloud.datastore.*;

import java.util.ArrayList;
import java.util.List;

public class DatastoreUtil {
    private static final String KIND = "User";
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public static Key userKey(String id) {
        return datastore.newKeyFactory().setKind(KIND).newKey(id);
    }

    public static void saveUser(UserRecord u) {
        Key key = userKey(u.getEmail()); // use email as key name
        FullEntity<IncompleteKey> builder = Entity.newBuilder(datastore.newKeyFactory().setKind(KIND).newKey())
                .set("name", u.getName() == null ? "" : u.getName())
                .set("dob", u.getDob() == null ? "" : u.getDob())
                .set("email", u.getEmail() == null ? "" : u.getEmail())
                .set("password", u.getPassword() == null ? "" : u.getPassword())
                .set("phone", u.getPhone() == null ? "" : u.getPhone())
                .set("gender", u.getGender() == null ? "" : u.getGender())
                .set("address", u.getAddress() == null ? "" : u.getAddress())
                .build();

        // Use key with name = email
        FullEntity<Key> ent = Entity.newBuilder(userKey(u.getEmail()))
                .set("name", u.getName() == null ? "" : u.getName())
                .set("dob", u.getDob() == null ? "" : u.getDob())
                .set("email", u.getEmail() == null ? "" : u.getEmail())
                .set("password", u.getPassword() == null ? "" : u.getPassword())
                .set("phone", u.getPhone() == null ? "" : u.getPhone())
                .set("gender", u.getGender() == null ? "" : u.getGender())
                .set("address", u.getAddress() == null ? "" : u.getAddress())
                .build();

        datastore.put(ent);
    }

    public static List<UserRecord> listUsers() {
        Query<Entity> query = Query.newEntityQueryBuilder().setKind(KIND).build();
        QueryResults<Entity> results = datastore.run(query);
        List<UserRecord> out = new ArrayList<>();
        while (results.hasNext()) {
            Entity e = results.next();
            UserRecord u = new UserRecord();
            u.setId(String.valueOf(e.getKey().getName() == null ? e.getKey().getId() : e.getKey().getName()));
            u.setName(e.contains("name") ? e.getString("name") : "");
            u.setDob(e.contains("dob") ? e.getString("dob") : "");
            u.setEmail(e.contains("email") ? e.getString("email") : "");
            u.setPassword(e.contains("password") ? e.getString("password") : "");
            u.setPhone(e.contains("phone") ? e.getString("phone") : "");
            u.setGender(e.contains("gender") ? e.getString("gender") : "");
            u.setAddress(e.contains("address") ? e.getString("address") : "");
            out.add(u);
        }
        return out;
    }

    public static void deleteUserById(String id) {
        Key key = userKey(id);
        datastore.delete(key);
    }

    public static UserRecord getUserByEmail(String email) {
        Key key = userKey(email);
        Entity e = datastore.get(key);
        if (e == null) return null;
        UserRecord u = new UserRecord();
        u.setId(e.getKey().getName());
        u.setName(e.contains("name") ? e.getString("name") : "");
        u.setDob(e.contains("dob") ? e.getString("dob") : "");
        u.setEmail(e.contains("email") ? e.getString("email") : "");
        u.setPassword(e.contains("password") ? e.getString("password") : "");
        u.setPhone(e.contains("phone") ? e.getString("phone") : "");
        u.setGender(e.contains("gender") ? e.getString("gender") : "");
        u.setAddress(e.contains("address") ? e.getString("address") : "");
        return u;
    }
}

