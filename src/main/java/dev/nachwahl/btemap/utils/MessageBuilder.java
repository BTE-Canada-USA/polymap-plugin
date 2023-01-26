package dev.nachwahl.btemap.utils;

import dev.dejvokep.boostedyaml.YamlDocument;

public class MessageBuilder {

    private String message;
    private String prefix;
    private String suffix;
    
    public MessageBuilder() {
        this("");
    }

    public MessageBuilder(YamlDocument yml, String route) {
        this(yml.getString(route));
    }

    public MessageBuilder(String original) {
        this.message = original;
        this.prefix = "";
        this.suffix = "";
    }

    public MessageBuilder replace(YamlDocument yml, String key, String valueRoute) {
        return replace(key, yml.getString(valueRoute));
    }

    public MessageBuilder replace(String key, String value) {
        this.message = this.message.replace(formatKey(key), value);
        return this;
    }

    public MessageBuilder addToEnd(YamlDocument yml, String route) {
        return addToEnd(yml.getString(route));
    }

    public MessageBuilder addToEnd(String string) {
        this.message += string;
        return this;
    }

    public MessageBuilder addToStart(YamlDocument yml, String route) {
        return addToStart(yml.getString(route));
    }

    public MessageBuilder addToStart(String string) {
        this.message = this.message + string;
        return this;
    }

    public MessageBuilder setPrefix(YamlDocument yml, String route) {
        return setPrefix(yml.getString(route));
    }

    public MessageBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public MessageBuilder setSuffix(YamlDocument yml, String route) {
        return setSuffix(yml.getString(route));
    }

    public MessageBuilder setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public String build() {
        return prefix + message + suffix;
    }

    private static String formatKey(String key) {
        String out = new String(key);

        if (!key.startsWith("{"))
            out = "{" + out;
        
        if (!key.endsWith("{"))
            out = out + "}";

        return out;
    }

}
