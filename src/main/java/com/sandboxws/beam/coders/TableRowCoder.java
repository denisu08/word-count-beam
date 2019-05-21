package com.sandboxws.beam.coders;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import org.apache.beam.sdk.coders.AtomicCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.values.TypeDescriptor;

/**
 * PostgreSQL TableRow to HashMap Coder.
 *
 * @author Ahmed El.Hussaini
 */
@SuppressWarnings({ "serial", "unchecked" })
public class TableRowCoder extends AtomicCoder<HashMap<String, Object>> {
    private static final TableRowCoder INSTANCE = new TableRowCoder();
    private static final TypeDescriptor<HashMap<String, Object>> TYPE_DESCRIPTOR =
            new TypeDescriptor<HashMap<String, Object>>() { };

    public static TableRowCoder of() {
        return INSTANCE;
    }

    private TableRowCoder() {
    }

    @Override
    public void encode(HashMap<String, Object> value, OutputStream outStream) throws IOException {
        encode(value, outStream, Context.NESTED);
    }

    @Override
    public void encode(HashMap<String, Object> row, OutputStream outStream, Context context) throws IOException {
        Gson gson = new Gson();
        StringUtf8Coder.of().encode(gson.toJson(row), outStream, context);
    }

    @Override
    public HashMap<String, Object> decode(InputStream inStream) throws IOException {
        return decode(inStream, Context.NESTED);
    }

    @Override
    public HashMap<String, Object> decode(InputStream inStream, Context context) throws IOException {
        String strValue = StringUtf8Coder.of().decode(inStream, context);
        Gson gson = new Gson();
        return gson.fromJson(strValue, HashMap.class);
    }

    @Override
    protected long getEncodedElementByteSize(HashMap<String, Object> row) throws Exception {
        Gson gson = new Gson();
        return StringUtf8Coder.of().getEncodedElementByteSize(gson.toJson(row));
    }

    @Override
    public void verifyDeterministic() throws NonDeterministicException {
        throw new NonDeterministicException(this,
                "HashMap<String, Object> can hold arbitrary instances, which may be non-deterministic.");
    }

    @Override
    public TypeDescriptor<HashMap<String, Object>> getEncodedTypeDescriptor() {
        return TYPE_DESCRIPTOR;
    }
}