package code.ponfee.commons.schema;

import code.ponfee.commons.exception.CheckedException;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * 原文格式：As a string
 * 
 * @author Ponfee
 */
@JSONType(serializer = PlainStructure.FastjsonSerializer.class, deserializer = PlainStructure.FastjsonSerializer.class)
@JsonSerialize(using = PlainStructure.JacksonSerializer.class)
@JsonDeserialize(using = PlainStructure.JacksondDeserializer.class)
public final class PlainStructure implements DataStructure, CharSequence {
    private static final long serialVersionUID = 1L;

    private final String plain;

    public PlainStructure(String plain) {
        // if plain is null, shoudle make the PlainStructure object is null;
        this.plain = Objects.requireNonNull(plain);
    }

    @Override
    public NormalStructure toNormal() {
        try {
            return (NormalStructure) DataStructures.NORMAL.parse(this.plain);
        } catch (Exception e) {
            throw new CheckedException("Convert to normal structure fail: " + this.plain, e);
        }
    }

    @Override
    public TableStructure toTable() {
        try {
            return (TableStructure) DataStructures.TABLE.parse(this.plain);
        } catch (Exception e) {
            throw new CheckedException("Convert to table structure fail: " + this.plain, e);
        }
    }

    @Override
    public PlainStructure toPlain() {
        return this;
    }

    @Override
    public String toString() {
        return this.plain;
    }

    @Override
    public int length() {
        return this.plain.length();
    }

    @Override
    public char charAt(int index) {
        return this.plain.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.plain.subSequence(start, end);
    }

    // -----------------------------------------------------custom fastjson serialize/deserialize
    public static class FastjsonSerializer implements ObjectSerializer, ObjectDeserializer {
        @Override
        public void write(JSONSerializer serializer, Object value, 
                          Object fieldName,Type fieldType, int features) {
            serializer.write(value == null ? null : value.toString());
        }

        @Override @SuppressWarnings("unchecked")
        public PlainStructure deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            if (type != PlainStructure.class) {
                throw new UnsupportedOperationException(
                    "Only supported deserialize PlainStructure, cannot supported: " + type
                );
            }

            String value = parser.getLexer().stringVal();
            // 解决报错问题：not close json text, token : string
            parser.getLexer().nextToken(JSONToken.LITERAL_STRING);
            return value == null ? null : new PlainStructure(value);
        }

        @Override
        public int getFastMatchToken() {
            return 0;
        }
    }

    // -----------------------------------------------------custom jackson serialize/deserialize
    public static class JacksonSerializer extends JsonSerializer<PlainStructure> {
        @Override
        public void serialize(PlainStructure value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value == null ? null : value.toString());
        }
    }

    public static class JacksondDeserializer extends JsonDeserializer<PlainStructure> {
        @Override
        public PlainStructure deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getText();
            return value == null ? null : new PlainStructure(value);
        }
    }

}
