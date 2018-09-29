package me.crz.minecraft.kingdoms.utils.config;

import java.lang.reflect.Field;

import org.json.JSONArray;

import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class Data {
    @ConfigOption
    public static JSONObject first = new JSONObject();
    @ConfigOption
    public static boolean muteChat = false;
    @ConfigOption
    public static List<String> socialSpy;
    @ConfigOption
    public static JSONObject vlock = new JSONObject();
    @ConfigOption
    public static long residentCacheDuration = 120;

    public Data() {
        super();
        for (int i = 1; i <= 5; ++i) {
            first.put("village-" + i, "");
        }
        for (int i = 0; i <= 10; ++i) {
            first.put("kingdom-" + i, "");
        }
    }

    public boolean load(File folder) {
        try {
            File file = new File(folder, "data.json");
            if (!file.exists()) {
                file.createNewFile();
                JSONObject object = new JSONObject();
                for (Field field : this.getClass().getDeclaredFields()) {
                    if (field.isAnnotationPresent(ConfigOption.class)) {
                        object.put(field.getName(), field.get(this));
                    }
                }
                FileUtils.writeStringToFile(file, object.toString(), "UTF-8");
            } else {
                JSONObject object = new JSONObject(new Scanner(file).useDelimiter("\\Z").next());
                for (Field field : this.getClass().getDeclaredFields()) {
                    if (field.isAnnotationPresent(ConfigOption.class) && object.has(field.getName())) {
                        if (field.getType().isAssignableFrom(JSONObject.class)) {
                            field.set(this, new JSONObject(object.get(field.getName()).toString()));
                        } else if (field.getType().isAssignableFrom(List.class)) {
                            field.set(this, this.toList((JSONArray) object.get(field.getName())));
                        } else {
                            field.set(this, object.get(field.getName()));
                        }
                    }
                }
            }
            this.save(folder);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<Object> toList(JSONArray array) {
        List<Object> temp = new ArrayList<>();
        array.forEach(temp::add);
        return temp;
    }

    public void save(File folder) {
        try {
            File file = new File(folder, "data.json");
            JSONObject object = new JSONObject();
            for (Field field : this.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(ConfigOption.class)) {
                    object.put(field.getName(), field.get(this));
                }
            }
            FileUtils.writeStringToFile(file, object.toString(), "UTF-8");
        } catch (Exception ex) {
        }
    }
}
