package com.sandboxws.chinook.bigquery.schema;

import com.google.api.services.bigquery.model.TableSchema;
import com.sandboxws.bigquery.TableSchemaBuilder;

/**
 * Album BigQuery table schema.
 *
 * @author Ahmed El.Hussaini
 */
public class AlbumTableSchema {
    public static TableSchema schema() {
        return TableSchemaBuilder.create()
                .integerField("id_album")
                .stringField("title")
                .integerField("artist_id")
                .schema();
    }
}