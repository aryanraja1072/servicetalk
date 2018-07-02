/*
 * Copyright © 2018 Apple Inc. and the ServiceTalk project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicetalk.http.api;

import io.servicetalk.buffer.api.Buffer;

import static io.servicetalk.http.api.DefaultHttpProtocolVersion.httpVersionToBuffer;
import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * Provides constant instances of {@link HttpProtocolVersion}, as well as a mechanism for creating new instances if the
 * existing constants are not sufficient.
 */
public enum HttpProtocolVersions implements HttpProtocolVersion {
    HTTP_1_0(1, 0),
    HTTP_1_1(1, 1);

    private final int major;
    private final int minor;
    private final Buffer httpVersion;
    private final String httpVersionString;

    HttpProtocolVersions(int major, int minor) {
        this.major = major;
        this.minor = minor;
        httpVersion = httpVersionToBuffer(major, minor);
        httpVersionString = httpVersion.toString(US_ASCII);
    }

    /**
     * Get a {@link HttpProtocolVersion} for the specified {@code major} and {@code minor}. If the {@code major} and
     * {@code minor} match those of this {@code enum}, that {@code enum} value will be returned, otherwise a new
     * instance will be returned.
     *
     * @param major the <strong>&lt;major&gt;</strong> portion of the
     *              <a href="https://tools.ietf.org/html/rfc7230.html#section-2.6">http protocol version</a>.
     * @param minor the <strong>&lt;minor&gt;</strong> portion of the
     *              <a href="https://tools.ietf.org/html/rfc7230.html#section-2.6">http protocol version</a>.
     * @return a {@link HttpProtocolVersion}.
     */
    public static HttpProtocolVersion getProtocolVersion(final int major, final int minor) {
        if (major == 1) {
            if (minor == 0) {
                return HTTP_1_0;
            }
            if (minor == 1) {
                return HTTP_1_1;
            }
        }
        return new DefaultHttpProtocolVersion(major, minor);
    }

    /**
     * Create a new {@link HttpProtocolVersion} from an unknown
     * <a href="https://tools.ietf.org/html/rfc7230.html#section-2.6">HTTP protocol version</a>.
     * @param httpVersion an unknown
     *       <a href="https://tools.ietf.org/html/rfc7230.html#section-2.6">HTTP protocol version</a>
     * @return a new {@link HttpProtocolVersion}.
     */
    public static HttpProtocolVersion newProtocolVersion(final Buffer httpVersion) {
        return new UnknownHttpProtocolVersion(httpVersion);
    }

    @Override
    public int getMajorVersion() {
        return major;
    }

    @Override
    public int getMinorVersion() {
        return minor;
    }

    @Override
    public void writeHttpVersionTo(final Buffer buffer) {
        buffer.writeBytes(httpVersion, httpVersion.getReaderIndex(), httpVersion.getReadableBytes());
    }

    @Override
    public String toString() {
        return httpVersionString;
    }

    private static final class UnknownHttpProtocolVersion implements HttpProtocolVersion {
        private final int major;
        private final int minor;
        private final Buffer httpVersion;

        UnknownHttpProtocolVersion(Buffer httpVersion) {
            // We could delay the parsing of major/minor but this is currently used during decode to validate the
            // correct form of the request.
            if (httpVersion.getReadableBytes() < 8) {
                throw new IllegalArgumentException("httpVersion is not of the correct format, too small");
            }
            int i = httpVersion.indexOf(httpVersion.getReaderIndex(), httpVersion.getReaderIndex() + 6, (byte) '.');
            if (i < 0) {
                throw new IllegalArgumentException("httpVersion is not of the correct format, no minor version");
            }
            this.major = parseInt(httpVersion.toString(httpVersion.getReaderIndex() + 5,
                    i - httpVersion.getReaderIndex(), US_ASCII));
            this.minor = parseInt(httpVersion.toString(httpVersion.getReaderIndex() + i + 1,
                    httpVersion.getWriterIndex() - (httpVersion.getReaderIndex() + i + 1), US_ASCII));
            this.httpVersion = httpVersion;
        }

        @Override
        public int getMajorVersion() {
            return major;
        }

        @Override
        public int getMinorVersion() {
            return minor;
        }

        @Override
        public void writeHttpVersionTo(final Buffer buffer) {
            buffer.writeBytes(httpVersion, httpVersion.getReaderIndex(), httpVersion.getReadableBytes());
        }

        @Override
        public String toString() {
            return httpVersion.toString(US_ASCII);
        }
    }
}
