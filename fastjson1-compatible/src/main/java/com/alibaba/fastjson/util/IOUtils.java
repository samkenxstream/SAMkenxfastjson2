/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.fastjson.util;

import com.alibaba.fastjson.JSONException;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;

/**
 * @author wenshao[szujobs@hotmail.com]
 */
public class IOUtils {
    public static final Charset UTF8 = StandardCharsets.UTF_8;
    public static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    public static final boolean[] firstIdentifierFlags = new boolean[256];
    public static final boolean[] identifierFlags = new boolean[256];
    public static final byte[] specicalFlags_doubleQuotes = new byte[161];
    public static final byte[] specicalFlags_singleQuotes = new byte[161];
    public static final boolean[] specicalFlags_doubleQuotesFlags = new boolean[161];
    public static final boolean[] specicalFlags_singleQuotesFlags = new boolean[161];
    public static final char[] replaceChars = new char[93];
    public static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    static {
        for (char c = 0; c < firstIdentifierFlags.length; ++c) {
            if (c >= 'A' && c <= 'Z') {
                firstIdentifierFlags[c] = true;
            } else if (c >= 'a' && c <= 'z') {
                firstIdentifierFlags[c] = true;
            } else if (c == '_' || c == '$') {
                firstIdentifierFlags[c] = true;
            }
        }

        for (char c = 0; c < identifierFlags.length; ++c) {
            if (c >= 'A' && c <= 'Z') {
                identifierFlags[c] = true;
            } else if (c >= 'a' && c <= 'z') {
                identifierFlags[c] = true;
            } else if (c == '_') {
                identifierFlags[c] = true;
            } else if (c >= '0' && c <= '9') {
                identifierFlags[c] = true;
            }
        }
    }

    static {
        specicalFlags_doubleQuotes['\0'] = 4;
        specicalFlags_doubleQuotes['\1'] = 4;
        specicalFlags_doubleQuotes['\2'] = 4;
        specicalFlags_doubleQuotes['\3'] = 4;
        specicalFlags_doubleQuotes['\4'] = 4;
        specicalFlags_doubleQuotes['\5'] = 4;
        specicalFlags_doubleQuotes['\6'] = 4;
        specicalFlags_doubleQuotes['\7'] = 4;
        specicalFlags_doubleQuotes['\b'] = 1; // 8
        specicalFlags_doubleQuotes['\t'] = 1; // 9
        specicalFlags_doubleQuotes['\n'] = 1; // 10
        specicalFlags_doubleQuotes['\u000B'] = 4; // 11
        specicalFlags_doubleQuotes['\f'] = 1; // 12
        specicalFlags_doubleQuotes['\r'] = 1; // 13
        specicalFlags_doubleQuotes['\"'] = 1; // 34
        specicalFlags_doubleQuotes['\\'] = 1; // 92

        specicalFlags_singleQuotes['\0'] = 4;
        specicalFlags_singleQuotes['\1'] = 4;
        specicalFlags_singleQuotes['\2'] = 4;
        specicalFlags_singleQuotes['\3'] = 4;
        specicalFlags_singleQuotes['\4'] = 4;
        specicalFlags_singleQuotes['\5'] = 4;
        specicalFlags_singleQuotes['\6'] = 4;
        specicalFlags_singleQuotes['\7'] = 4;
        specicalFlags_singleQuotes['\b'] = 1; // 8
        specicalFlags_singleQuotes['\t'] = 1; // 9
        specicalFlags_singleQuotes['\n'] = 1; // 10
        specicalFlags_singleQuotes['\u000B'] = 4; // 11
        specicalFlags_singleQuotes['\f'] = 1; // 12
        specicalFlags_singleQuotes['\r'] = 1; // 13
        specicalFlags_singleQuotes['\\'] = 1; // 92
        specicalFlags_singleQuotes['\''] = 1; // 39

        for (int i = 14; i <= 31; ++i) {
            specicalFlags_doubleQuotes[i] = 4;
            specicalFlags_singleQuotes[i] = 4;
        }

        for (int i = 127; i < 160; ++i) {
            specicalFlags_doubleQuotes[i] = 4;
            specicalFlags_singleQuotes[i] = 4;
        }

        for (int i = 0; i < 161; ++i) {
            specicalFlags_doubleQuotesFlags[i] = specicalFlags_doubleQuotes[i] != 0;
            specicalFlags_singleQuotesFlags[i] = specicalFlags_singleQuotes[i] != 0;
        }

        replaceChars['\0'] = '0';
        replaceChars['\1'] = '1';
        replaceChars['\2'] = '2';
        replaceChars['\3'] = '3';
        replaceChars['\4'] = '4';
        replaceChars['\5'] = '5';
        replaceChars['\6'] = '6';
        replaceChars['\7'] = '7';
        replaceChars['\b'] = 'b'; // 8
        replaceChars['\t'] = 't'; // 9
        replaceChars['\n'] = 'n'; // 10
        replaceChars['\u000B'] = 'v'; // 11
        replaceChars['\f'] = 'f'; // 12
        replaceChars['\r'] = 'r'; // 13
        replaceChars['\"'] = '"'; // 34
        replaceChars['\''] = '\''; // 39
        replaceChars['/'] = '/'; // 47
        replaceChars['\\'] = '\\'; // 92
    }

    public static void decode(CharsetDecoder charsetDecoder, ByteBuffer byteBuf, CharBuffer charByte) {
        try {
            CoderResult cr = charsetDecoder.decode(byteBuf, charByte, true);

            if (!cr.isUnderflow()) {
                cr.throwException();
            }

            cr = charsetDecoder.flush(charByte);

            if (!cr.isUnderflow()) {
                cr.throwException();
            }
        } catch (CharacterCodingException x) {
            // Substitution is always enabled,
            // so this shouldn't happen
            throw new JSONException("utf8 decode error, " + x.getMessage(), x);
        }
    }

    public static byte[] decodeBase64(String s) {
        return com.alibaba.fastjson2.util.IOUtils.decodeBase64(s);
    }

    public static void close(Closeable x) {
        if (x != null) {
            try {
                x.close();
            } catch (Exception ignored) {
                // ignored
            }
        }
    }

    public static void getChars(byte b, int index, char[] buf) {
        com.alibaba.fastjson2.util.IOUtils.getChars(b, index, buf);
    }

    public static void getChars(int i, int index, char[] buf) {
        com.alibaba.fastjson2.util.IOUtils.getChars(i, index, buf);
    }

    public static void getChars(long i, int index, char[] buf) {
        com.alibaba.fastjson2.util.IOUtils.getChars(i, index, buf);
    }

    public static int stringSize(int x) {
        return com.alibaba.fastjson2.util.IOUtils.stringSize(x);
    }

    public static int stringSize(long x) {
        return com.alibaba.fastjson2.util.IOUtils.stringSize(x);
    }

    public static int decodeUTF8(byte[] sa, int sp, int len, char[] da) {
        return com.alibaba.fastjson2.util.IOUtils.decodeUTF8(sa, sp, len, da);
    }

    public static boolean isIdent(char ch) {
        return ch < identifierFlags.length && identifierFlags[ch];
    }

    public static boolean isValidJsonpQueryParam(String value) {
        if (value == null || value.length() == 0) {
            return false;
        }

        for (int i = 0, len = value.length(); i < len; ++i) {
            char ch = value.charAt(i);
            if (ch != '.' && !IOUtils.isIdent(ch)) {
                return false;
            }
        }

        return true;
    }
}
