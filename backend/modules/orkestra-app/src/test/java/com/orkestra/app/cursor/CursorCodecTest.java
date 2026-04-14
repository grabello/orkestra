package com.orkestra.app.cursor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CursorCodecTest {

    private CursorCodec cursorCodec;

    @BeforeEach
    void setUp() {
        cursorCodec = new CursorCodec(new ObjectMapper());
    }

    @Test
    void shouldEncodeAndDecodeCursor() {
        WorkflowCursor cursor = new WorkflowCursor("1", "TENANT#1", "WF#invoice");

        String encoded = cursorCodec.encode(cursor);
        WorkflowCursor decoded = cursorCodec.decode(encoded);

        assertEquals(cursor, decoded);
    }

    @Test
    void shouldEncodeNullCursor() {
        String encoded = cursorCodec.encode(null);
        assertNull(encoded);
    }

    @Test
    void shouldDecodeNullCursor() {
        WorkflowCursor decoded = cursorCodec.decode(null);
        assertEquals(new WorkflowCursor(null, null, null), decoded);
    }

    @Test
    void shouldNotEncodePkNull() {
        String encoded = cursorCodec.encode(new WorkflowCursor("1", null, "sk"));
        assertNull(encoded);
    }

    @Test
    void shouldNotEncodeSkNull() {
        String encoded = cursorCodec.encode(new WorkflowCursor("1", "pk", null));
        assertNull(encoded);
    }

}
