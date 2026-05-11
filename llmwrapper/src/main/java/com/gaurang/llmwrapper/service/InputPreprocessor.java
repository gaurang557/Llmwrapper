package com.gaurang.llmwrapper.service;

import com.gaurang.llmwrapper.dto.ChatMessage;
import com.gaurang.llmwrapper.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class InputPreprocessor {

    private static final int MAX_MESSAGES = 50;
    private static final int MAX_CHARS_PER_MESSAGE = 16_000;
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");
    private static final Pattern ZERO_WIDTH = Pattern.compile("[\\u200B-\\u200D\\uFEFF]");
    private static final Pattern MULTI_BLANK = Pattern.compile("[ \t]{2,}");
    private static final Pattern MULTI_NEWLINE = Pattern.compile("\n{3,}");

    public List<ChatMessage> clean(List<ChatMessage> input) {
        if (input == null || input.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "messages must not be empty");
        }
        if (input.size() > MAX_MESSAGES) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "too many messages (max " + MAX_MESSAGES + ")");
        }

        List<ChatMessage> cleaned = new ArrayList<>(input.size());
        for (ChatMessage m : input) {
            String content = m.content() == null ? "" : m.content();
            content = CONTROL_CHARS.matcher(content).replaceAll("");
            content = ZERO_WIDTH.matcher(content).replaceAll("");
            content = content.replace("\r\n", "\n").replace("\r", "\n");
            content = MULTI_BLANK.matcher(content).replaceAll(" ");
            content = MULTI_NEWLINE.matcher(content).replaceAll("\n\n");
            content = content.strip();
            if (content.isEmpty()) {
                continue;
            }
            if (content.length() > MAX_CHARS_PER_MESSAGE) {
                content = content.substring(0, MAX_CHARS_PER_MESSAGE);
            }
            cleaned.add(new ChatMessage(m.role(), content));
        }

        if (cleaned.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no usable content in messages");
        }
        if (!"user".equals(cleaned.get(cleaned.size() - 1).role())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "last message must be from 'user'");
        }
        return cleaned;
    }
}
