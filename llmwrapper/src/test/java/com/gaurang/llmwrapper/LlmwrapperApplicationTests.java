package com.gaurang.llmwrapper;

import com.gaurang.llmwrapper.dto.ChatMessage;
import com.gaurang.llmwrapper.service.InputPreprocessor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LlmwrapperApplicationTests {

    private final InputPreprocessor preprocessor = new InputPreprocessor();

    @Test
    void preprocessorStripsControlCharsAndCollapsesWhitespace() {
        List<ChatMessage> cleaned = preprocessor.clean(List.of(
                new ChatMessage("user", "hello    world\n\n\n\nhi")
        ));
        assertEquals(1, cleaned.size());
        assertEquals("hello world\n\nhi", cleaned.get(0).content());
    }

    @Test
    void preprocessorRequiresUserAsLastMessage() {
        assertThrows(Exception.class, () -> preprocessor.clean(List.of(
                new ChatMessage("system", "be helpful")
        )));
    }
}
