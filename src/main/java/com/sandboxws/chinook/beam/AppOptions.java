package com.sandboxws.chinook.beam;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;

/**
 * App specific Apache Beam pipeline options.
 *
 */
public interface AppOptions extends DataflowPipelineOptions {
    @Description("PostgreSQL Host")
    @Default.String("localhost")
    String getPgHost();
    void setPgHost(String value);

    @Description("PostgresSQL port")
    @Default.Integer(5432)
    int getPgPort();
    void setPgPort(int value);

    @Description("PostgreSQL Username")
    @Default.String("postgres")
    String getPgUsername();
    void setPgUsername(String value);

    @Description("PostgreSQL Password")
    @Default.String("postgres")
    String getPgPassword();
    void setPgPassword(String value);

    @Description("PostgreSQL Database")
    @Default.String("postgres")
    String getPgDatabase();
    void setPgDatabase(String value);

    @Description("BigQuery output table")
    String getOutputTable();
    void setOutputTable(String value);

    @Description("MySQL Host")
    @Default.String("localhost")
    String getMyHost();
    void setMyHost(String value);

    @Description("MySQL port")
    @Default.Integer(3306)
    int getMyPort();
    void setMyPort(int value);

    @Description("MySQL Username")
    @Default.String("root")
    String getMyUsername();
    void setMyUsername(String value);

    @Description("MySQL Password")
    @Default.String("")
    String getMyPassword();
    void setMyPassword(String value);

    @Description("MySQL Database")
    @Default.String("test")
    String getMyDatabase();
    void setMyDatabase(String value);
}