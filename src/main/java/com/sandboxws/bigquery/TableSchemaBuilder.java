package com.sandboxws.bigquery;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import java.util.ArrayList;
import java.util.List;

/**
 * BigQuery table schema builder.
 *
 */
public class TableSchemaBuilder {
    public static final String BOOLEAN = "BOOLEAN";
    public static final String DATE = "DATE";
    public static final String FLOAT = "FLOAT";
    public static final String GEOGRAPHY = "GEOGRAPHY";
    public static final String INTEGER = "INTEGER";
    public static final String NULLABLE = "NULLABLE";
    public static final String RECORD = "RECORD";
    public static final String REPEATED = "REPEATED";
    public static final String REQUIRED = "REQUIRED";
    public static final String STRING = "STRING";
    public static final String TIMESTAMP = "TIMESTAMP";

    List<TableFieldSchema> schemaFields = new ArrayList<TableFieldSchema>();

    public static TableSchemaBuilder create() {
        return new TableSchemaBuilder();
    }

    public TableFieldSchema fieldSchema(String type, String name, String mode, String description) {
        TableFieldSchema field = new TableFieldSchema();
        field.setType(type);
        field.setName(name);
        field.setMode(mode);
        field.setDescription(description);

        return field;
    }

    public TableSchema schema() {
        TableSchema tableSchema = new TableSchema();
        tableSchema.setFields(schemaFields);

        return tableSchema;
    }

    public TableSchemaBuilder booleanField(String name, String mode, String description) {
        schemaFields.add(fieldSchema(BOOLEAN, name, mode, description));
        return this;
    }

    public TableSchemaBuilder booleanField(String name, String mode) {
        return booleanField(name, mode, "");
    }

    public TableSchemaBuilder booleanField(String name) {
        return booleanField(name, NULLABLE);
    }

    public TableSchemaBuilder dateField(String name, String mode, String description) {
        schemaFields.add(fieldSchema(DATE, name, mode, description));
        return this;
    }

    public TableSchemaBuilder dateField(String name, String mode) {
        return dateField(name, mode, "");
    }

    public TableSchemaBuilder dateField(String name) {
        return dateField(name, NULLABLE);
    }

    public TableSchemaBuilder floatField(String name, String mode, String description) {
        schemaFields.add(fieldSchema(FLOAT, name, mode, description));
        return this;
    }

    public TableSchemaBuilder floatField(String name, String mode) {
        return floatField(name, mode, "");
    }

    public TableSchemaBuilder floatField(String name) {
        return floatField(name, NULLABLE);
    }

    public TableSchemaBuilder geographyField(String name, String mode, String description) {
        schemaFields.add(fieldSchema(GEOGRAPHY, name, mode, description));
        return this;
    }

    public TableSchemaBuilder geographyField(String name, String mode) {
        return geographyField(name, mode, "");
    }

    public TableSchemaBuilder geographyField(String name) {
        return geographyField(name, NULLABLE);
    }

    public TableSchemaBuilder integerField(String name, String mode, String description) {
        schemaFields.add(fieldSchema(INTEGER, name, mode, description));
        return this;
    }

    public TableSchemaBuilder integerField(String name, String mode) {
        return integerField(name, mode, "");
    }

    public TableSchemaBuilder integerField(String name) {
        return integerField(name, NULLABLE);
    }

    public TableSchemaBuilder stringField(String name, String mode, String description) {
        schemaFields.add(fieldSchema(STRING, name, mode, description));
        return this;
    }

    public TableSchemaBuilder stringField(String name, String mode) {
        return stringField(name, mode, "");
    }

    public TableSchemaBuilder stringField(String name) {
        return stringField(name, NULLABLE);
    }

    public TableSchemaBuilder timestampField(String name, String mode, String description) {
        schemaFields.add(fieldSchema(TIMESTAMP, name, mode, description));
        return this;
    }

    public TableSchemaBuilder timestampField(String name, String mode) {
        return timestampField(name, mode, "");
    }

    public TableSchemaBuilder timestampField(String name) {
        return timestampField(name, NULLABLE);
    }

    public TableSchemaBuilder repeatedRecord(String name, List<TableFieldSchema> fields) {
        TableFieldSchema tableFieldSchema = new TableFieldSchema();
        tableFieldSchema.setType(RECORD);
        tableFieldSchema.setName(name);
        tableFieldSchema.setMode(REPEATED);
        tableFieldSchema.setFields(fields);
        schemaFields.add(tableFieldSchema);

        return this;
    }
}