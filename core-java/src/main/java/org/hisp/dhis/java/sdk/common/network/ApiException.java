/*
 * Copyright (c) 2015, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.java.sdk.common.network;

import java.io.IOException;

public class ApiException extends RuntimeException {

    public static ApiException networkError(String url, IOException exception) {
        return new ApiException(exception.getMessage(), url,
                null, Kind.NETWORK, exception);
    }

    public static ApiException conversionError(String url, Response response,
                                               Throwable exception) {
        return new ApiException(exception.getMessage(), url,
                response, Kind.CONVERSION, exception);
    }

    public static ApiException httpError(String url, Response response) {
        String message = response.getStatus() + " " + response.getReason();
        return new ApiException(message, url, response, Kind.HTTP, null);
    }

    public static ApiException unexpectedError(String url, Throwable exception) {
        return new ApiException(exception.getMessage(), url, null, Kind.UNEXPECTED,
                exception);
    }

    /**
     * Identifies the event kind which triggered a {@link ApiException}.
     */
    public enum Kind {
        /**
         * An {@link IOException} occurred while communicating to the server.
         */
        NETWORK,
        /**
         * An exception was thrown while (de)serializing a body.
         */
        CONVERSION,
        /**
         * A non-200 HTTP status code was received from the server.
         */
        HTTP,
        /**
         * An internal error occurred while attempting to execute a request. It is best practice to
         * re-throw this exception so your application crashes.
         */
        UNEXPECTED
    }

    private final String url;
    private final Response response;
    private final Kind kind;

    ApiException(String message, String url, Response response,
                 Kind kind, Throwable exception) {
        super(message, exception);
        this.url = url;
        this.response = response;
        this.kind = kind;
    }

    /**
     * The request URL which produced the error.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Response object containing status code, headers, body, etc.
     */
    public Response getResponse() {
        return response;
    }

    /**
     * The event kind which triggered this error.
     */
    public Kind getKind() {
        return kind;
    }
}
