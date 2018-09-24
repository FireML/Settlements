package me.crz.minecraft.kingdoms.utils.config;

import java.lang.reflect.Field;
import org.json.JSONArray;
import java.util.Scanner;
import org.apache.commons.io.FileUtils;
import java.lang.annotation.Annotation;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class Config
{
    @ConfigOption
    public static String host;
    @ConfigOption
    public static int port;
    @ConfigOption
    public static String databaseUser;
    @ConfigOption
    public static String database;
    @ConfigOption
    public static String databasePassword;
    @ConfigOption
    public static JSONObject first;
    @ConfigOption
    public static boolean muteChat;
    @ConfigOption
    public static List<String> socialSpy;
    @ConfigOption
    public static JSONObject vlock;
    
    static {
        host = "";
        port = 0;
        databaseUser = "";
        database = "kingdoms";
        databasePassword = "";
        first = new JSONObject();
        muteChat = false;
        socialSpy = new ArrayList<String>();
        vlock = new JSONObject();
    }
    
    public Config() {
        super();
        for (int i = 1; i <= 5; ++i) {
            first.put("village-" + i, (Object)"");
        }
        for (int i = 0; i <= 10; ++i) {
            first.put("kingdom-" + i, (Object)"");
        }
    }
    
    public boolean load(final File folder) {
        try {
            final File file = new File(folder, "data.json");
            if (!file.exists()) {
                file.createNewFile();
                final JSONObject object = new JSONObject();
                Field[] declaredFields;
                for (int n = (declaredFields = this.getClass().getDeclaredFields()).length, i = 0; i < n; ++i) {
                    final Field field = declaredFields[i];
                    if (field.isAnnotationPresent(ConfigOption.class)) {
                        object.put(field.getName(), field.get((Object)this));
                    }
                }
                FileUtils.writeStringToFile(file, object.toString());
            }
            else {
                final JSONObject object = new JSONObject(new Scanner(file).useDelimiter("\\Z").next());
                Field[] declaredFields2;
                for (int n = (declaredFields2 = this.getClass().getDeclaredFields()).length, i = 0; i < n; ++i) {
                    final Field field = declaredFields2[i];
                    if (field.isAnnotationPresent(ConfigOption.class) && object.has(field.getName())) {
                        if (field.getType().isAssignableFrom(JSONObject.class)) {
                            field.set(this, new JSONObject(object.get(field.getName()).toString()));
                        }
                        else if (field.getType().isAssignableFrom(List.class)) {
                            field.set(this, this.toList((JSONArray)object.get(field.getName())));
                        }
                        else {
                            field.set(this, object.get(field.getName()));
                        }
                    }
                }
            }
            this.save(folder);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private List<Object> toList(final JSONArray array) {
        final List<Object> temp = new ArrayList<Object>();
        array.forEach(s -> temp.add(s));
        return temp;
    }
    
    public void save(final File folder) {
        try {
            final File file = new File(folder, "data.json");
            final JSONObject object = new JSONObject();
            Field[] declaredFields;
            for (int length = (declaredFields = this.getClass().getDeclaredFields()).length, i = 0; i < length; ++i) {
                final Field field = declaredFields[i];
                if (field.isAnnotationPresent(ConfigOption.class)) {
                    object.put(field.getName(), field.get((Object)this));
                }
            }
            FileUtils.writeStringToFile(file, object.toString());
        }
        catch (Exception ex) {}
    }
    
    private static /* synthetic */ void lambda$0(final List list, final Object s) {
        list.add(s);
    }
}
