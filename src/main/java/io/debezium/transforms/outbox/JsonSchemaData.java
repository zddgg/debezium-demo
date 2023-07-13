package io.debezium.transforms.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.ConnectException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonSchemaData {
    private final EventRouterConfigDefinition.JsonPayloadNullFieldBehavior jsonPayloadNullFieldBehavior;

    public JsonSchemaData() {
        this.jsonPayloadNullFieldBehavior = EventRouterConfigDefinition.JsonPayloadNullFieldBehavior.IGNORE;
    }

    public JsonSchemaData(EventRouterConfigDefinition.JsonPayloadNullFieldBehavior jsonPayloadNullFieldBehavior) {
        this.jsonPayloadNullFieldBehavior = jsonPayloadNullFieldBehavior;
    }

    public Schema toConnectSchema(String key, JsonNode node) {
        switch (node.getNodeType()) {
            case STRING:
                return Schema.OPTIONAL_STRING_SCHEMA;
            case BOOLEAN:
                return Schema.OPTIONAL_BOOLEAN_SCHEMA;
            case NUMBER:
                if (node.isInt()) {
                    return Schema.OPTIONAL_INT32_SCHEMA;
                } else {
                    if (node.isLong()) {
                        return Schema.OPTIONAL_INT64_SCHEMA;
                    }

                    return Schema.OPTIONAL_FLOAT64_SCHEMA;
                }
            case ARRAY:
                ArrayNode arrayNode = (ArrayNode) node;
                return arrayNode.isEmpty() ? null : SchemaBuilder.array(this.toConnectSchemaWithCycles(key, arrayNode)).optional().build();
            case OBJECT:
                SchemaBuilder schemaBuilder = SchemaBuilder.struct().name(key).optional();
                if (node != null) {
                    Iterator<Map.Entry<String, JsonNode>> fieldsEntries = node.fields();

                    while (fieldsEntries.hasNext()) {
                        Map.Entry<String, JsonNode> fieldEntry = (Map.Entry) fieldsEntries.next();
                        String fieldName = (String) fieldEntry.getKey();
                        Schema fieldSchema = this.toConnectSchema(key + "." + fieldName, (JsonNode) fieldEntry.getValue());
                        if (fieldSchema != null && !this.hasField(schemaBuilder, fieldName)) {
                            schemaBuilder.field(fieldName, fieldSchema);
                        }
                    }
                }

                return schemaBuilder.build();
            case NULL:
                if (this.jsonPayloadNullFieldBehavior.equals(EventRouterConfigDefinition.JsonPayloadNullFieldBehavior.OPTIONAL_BYTES)) {
                    return Schema.OPTIONAL_BYTES_SCHEMA;
                }

                return null;
            default:
                return null;
        }
    }

    private Schema toConnectSchemaWithCycles(String key, ArrayNode array) throws ConnectException {
        Schema schema = null;
        JsonNode sample = this.getFirstArrayElement(array);
        if (sample.isObject()) {
            Iterator<JsonNode> elements = array.elements();

            while (elements.hasNext()) {
                JsonNode element = (JsonNode) elements.next();
                if (element.isObject()) {
                    if (schema == null) {
                        schema = this.toConnectSchema(key, element);
                    } else {
                        schema = this.toConnectSchema(key, element);
                    }
                }
            }
        } else {
            schema = this.toConnectSchema((String) null, sample);
            if (schema == null) {
                throw new ConnectException(String.format("Array '%s' has unrecognized member schema.", array.asText()));
            }
        }

        return schema;
    }

    private JsonNode getFirstArrayElement(ArrayNode array) throws ConnectException {
        JsonNode refNode = NullNode.getInstance();
        Schema refSchema = null;
        Iterator<JsonNode> elements = array.elements();

        while (elements.hasNext()) {
            JsonNode element = (JsonNode) elements.next();
            if (!element.isNull()) {
                if (((JsonNode) refNode).isNull()) {
                    refNode = element;
                }

                if (element.getNodeType() != ((JsonNode) refNode).getNodeType()) {
                    throw new ConnectException(String.format("Field is not a homogenous array (%s x %s).", ((JsonNode) refNode).asText(), element.getNodeType().toString()));
                }

                if (((JsonNode) refNode).getNodeType() == JsonNodeType.NUMBER) {
                    if (refSchema == null) {
                        refSchema = this.toConnectSchema((String) null, (JsonNode) refNode);
                    }

                    Schema elementSchema = this.toConnectSchema((String) null, element);
                    if (refSchema != elementSchema) {
                        throw new ConnectException(String.format("Field is not a homogenous array (%s x %s), different number types (%s x %s)", ((JsonNode) refNode).asText(), element.asText(), refSchema, elementSchema));
                    }
                }
            }
        }

        return (JsonNode) refNode;
    }

    private boolean hasField(SchemaBuilder builder, String fieldName) {
        return builder.field(fieldName) != null;
    }

    public Object toConnectData(JsonNode document, Schema schema) {
        return document == null ? null : this.jsonNodeToStructInternal(document, schema);
    }

    private Struct jsonNodeToStructInternal(JsonNode document, Schema schema) {
        Struct struct = new Struct(schema);
        Iterator var4 = schema.fields().iterator();

        while (var4.hasNext()) {
            Field field = (Field) var4.next();
            if (document.has(field.name())) {
                struct.put(field.name(), this.getStructFieldValue(document.path(field.name()), field.schema()));
            }
        }

        return struct;
    }

    private Object getStructFieldValue(JsonNode node, Schema schema) {
        switch (node.getNodeType()) {
            case STRING:
                return node.asText();
            case BOOLEAN:
                return node.asBoolean();
            case NUMBER:
                if (node.isFloat()) {
                    return node.floatValue();
                } else if (node.isDouble()) {
                    return node.asDouble();
                } else if (node.isInt()) {
                    return node.asInt();
                } else {
                    if (node.isLong()) {
                        return node.asLong();
                    }

                    return node.decimalValue();
                }
            case ARRAY:
                return this.getArrayAsList((ArrayNode) node, schema);
            case OBJECT:
                return this.jsonNodeToStructInternal(node, schema);
            default:
                return null;
        }
    }

    private List getArrayAsList(ArrayNode array, Schema schema) {
        List arrayObjects = new ArrayList(array.size());
        Iterator<JsonNode> elements = array.elements();

        while (elements.hasNext()) {
            arrayObjects.add(this.getStructFieldValue((JsonNode) elements.next(), schema.valueSchema()));
        }

        return arrayObjects;
    }
}
